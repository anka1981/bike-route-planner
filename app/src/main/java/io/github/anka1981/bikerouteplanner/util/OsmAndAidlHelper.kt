package io.github.anka1981.bikerouteplanner.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.osmand.aidlapi.IOsmAndAidlInterface
import net.osmand.aidlapi.gpx.RemoveGpxParams
import net.osmand.aidlapi.navigation.NavigateGpxParams
import kotlin.coroutines.resume

private const val TAG = "OsmAndAidlHelper"

/**
 * Startet die Navigation direkt in OsmAnd ueber dessen AIDL-Schnittstelle
 * (net.osmand.aidlapi, siehe osmand-api-Modul), ohne dass der Nutzer den Track
 * in OsmAnd erst manuell oeffnen/bestaetigen muss.
 *
 * OsmAnd wird ueber mehrere moegliche Packages probiert (Play-Store-Version,
 * Vollversion, Nightly-Build). Liefert false, wenn keine erreichbare OsmAnd-Installation
 * die Schnittstelle bereitstellt (Aufrufer soll dann auf den generischen GPX-Share
 * zurueckfallen).
 */
object OsmAndAidlHelper {

    private const val BIND_ACTION = "net.osmand.aidl.OsmandAidlServiceV2"
    private val CANDIDATE_PACKAGES = listOf("net.osmand.plus", "net.osmand", "net.osmand.dev")

    // OsmAnds eigene AIDL-Implementierung (OsmandAidlApi.navigateGpxV2) liefert intern nur dann
    // etwas anderes als false, wenn ihre MapActivity bereits existiert
    // ("mapActivity != null && NavigateGpxHelper.saveAndNavigateGpx(...)", siehe deren Quellcode).
    // Das Binden an den Dienst klappt aber unabhaengig davon, sobald OsmAnds Prozess laeuft - der
    // Aufruf liefert dann sauber (ohne Exception oder Timeout) false, wenn OsmAnds Kartenbildschirm
    // gerade nicht sichtbar ist. Deshalb OsmAnd zuerst in den Vordergrund holen und den Aufruf
    // wiederholen, bis dessen Activity tatsaechlich aufgebaut ist.
    // Mit 10 Versuchen a 600ms (~6s) gemessen: reicht, wenn OsmAnds Prozess schon lief, nicht aber
    // bei einem echten Kaltstart von dessen Kartenbildschirm - dort schlugen alle 10 Versuche fehl,
    // ein direkt darauf folgender zweiter Aufruf (OsmAnd war inzwischen hochgefahren) gelang dann
    // sofort. Grosszuegiger bemessen, damit auch ein Kaltstart innerhalb eines einzigen Aufrufs
    // durchlaeuft.
    private const val NAVIGATE_MAX_ATTEMPTS = 20
    private const val NAVIGATE_RETRY_DELAY_MS = 750L

    // Ordner, unter dem OsmAnd per navigateGpx() importierte Tracks ablegt (siehe OsmAnds
    // NavigateGpxHelper: GPX_IMPORT_DIR = GPX_INDEX_DIR + "import/"). removeGpx() braucht diesen
    // Pfad explizit im relativePath-Feld - der reine Dateiname allein sucht faelschlich im
    // GPX_INDEX_DIR-Wurzelordner ("tracks/") und findet dort nichts (siehe unten).
    private const val OSMAND_GPX_IMPORT_RELATIVE_DIR = "import/"
    private const val LAST_TRACK_PREFS_NAME = "osmand_aidl_last_track"

    // aidlInterface.navigateGpx() ist ein synchroner, blockierender Binder-Aufruf (kein suspend fun);
    // ohne den Wechsel auf Dispatchers.IO liefe der gesamte Retry-Loop (inklusive dieser Aufrufe) auf
    // dem Thread des Aufrufers - bei RouteScreen.kt ist das der Main-Thread. Ausgerechnet im
    // Kaltstart-Fall, den dieser Code abfaengt, kann OsmAnds Antwort spuerbar dauern, was dort zu
    // ANRs fuehren koennte.
    suspend fun tryNavigateGpx(
        context: Context,
        gpxContent: String,
        trackName: String,
        removePreviousTrack: Boolean
    ): Boolean =
        withContext(Dispatchers.IO) {
            for (packageName in CANDIDATE_PACKAGES) {
                if (navigateViaPackage(context, packageName, gpxContent, trackName, removePreviousTrack)) {
                    return@withContext true
                }
            }
            false
        }

    private suspend fun navigateViaPackage(
        context: Context,
        packageName: String,
        gpxContent: String,
        trackName: String,
        removePreviousTrack: Boolean
    ): Boolean {
        val bound = bindOsmAndService(context, packageName) ?: run {
            Log.w(TAG, "bindOsmAndService($packageName) lieferte keinen Binder (Timeout oder bindService()=false)")
            return false
        }
        val (binder, connection) = bound
        Log.i(TAG, "bindOsmAndService($packageName) erfolgreich, rufe navigateGpx auf")
        return try {
            val aidlInterface = IOsmAndAidlInterface.Stub.asInterface(binder)

            // Die beim letzten Mal an dieses Package gesendete Route jetzt entfernen, statt direkt
            // nach ihrem Senden: solange die neue Route noch nicht steht, navigiert OsmAnd
            // moeglicherweise noch mit der alten - sie sofort zu loeschen wuerde diese laufende
            // Navigation gefaehrden. Der Aufruf ist unkritisch, wenn er fehlschlaegt (Datei schon
            // weg, OsmAnd-Version ohne Fix fuer den frueher kaputten AIDL-Loeschbefehl, etc.) -
            // dann bleibt hoechstens ein Fragment liegen, wie ohne dieses Feature auch.
            if (removePreviousTrack) {
                getLastTrackFileName(context, packageName)?.let { previousFileName ->
                    removePreviousTrack(aidlInterface, packageName, previousFileName)
                }
            }

            val params = NavigateGpxParams(gpxContent, true, true).apply {
                setFileName(trackName)
                // NavigateGpxParams.passWholeRoute ist ein nullable Boolean (Bug im OsmAnd-
                // eigenen Code: writeToBundle() unboxt ihn ungeprueft), das ohne expliziten
                // Wert null bleibt und beim AIDL-Aufruf eine NullPointerException ausloest.
                // true = die komplette berechnete Route ab ihrem echten Startpunkt navigieren,
                // statt ab dem der aktuellen Position naechstgelegenen Punkt.
                setPassWholeRoute(true)
            }
            // Erst in den Vordergrund holen, DANN aufrufen: navigateGpx() liefert bei OsmAnd
            // false, solange dessen MapActivity nicht existiert, und die entsteht erst durch das
            // Starten der Activity. Mehrere Versuche mit kurzer Pause, weil das Erstellen der
            // Activity (insbesondere bei einem Kaltstart der Kartenansicht) etwas dauert.
            bringToForeground(context, packageName)
            var result = false
            for (attempt in 1..NAVIGATE_MAX_ATTEMPTS) {
                result = aidlInterface.navigateGpx(params)
                Log.i(TAG, "navigateGpx($packageName) Versuch $attempt lieferte: $result")
                if (result) break
                delay(NAVIGATE_RETRY_DELAY_MS)
            }
            if (result && removePreviousTrack) {
                // navigateGpx(params) ist als "inout" deklariert (siehe IOsmAndAidlInterface.aidl):
                // benennt OsmAnd die Datei um, weil bereits eine gleichnamige existiert, steht der
                // tatsaechlich verwendete Name danach hier in params.getFileName(). Diesen merken,
                // nicht den urspruenglich angefragten trackName, damit die naechste Loeschung den
                // richtigen Dateinamen trifft.
                setLastTrackFileName(context, packageName, params.getFileName() ?: trackName)
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "navigateGpx($packageName) warf Exception", e)
            false
        } finally {
            unbindQuietly(context, connection)
        }
    }

    private fun removePreviousTrack(aidlInterface: IOsmAndAidlInterface, packageName: String, fileName: String) {
        val removeParams = RemoveGpxParams(fileName).apply {
            setRelativePath(OSMAND_GPX_IMPORT_RELATIVE_DIR + fileName)
        }
        val removed = try {
            aidlInterface.removeGpx(removeParams)
        } catch (e: Exception) {
            Log.e(TAG, "removeGpx($packageName, $fileName) warf Exception", e)
            false
        }
        Log.i(TAG, "removeGpx($packageName, $fileName) lieferte: $removed")
    }

    private fun getLastTrackFileName(context: Context, packageName: String): String? =
        context.getSharedPreferences(LAST_TRACK_PREFS_NAME, Context.MODE_PRIVATE).getString(packageName, null)

    private fun setLastTrackFileName(context: Context, packageName: String, fileName: String) {
        context.getSharedPreferences(LAST_TRACK_PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(packageName, fileName)
        }
    }

    /**
     * navigateGpx() startet die Navigation nur im Hintergrund, holt OsmAnd aber nicht selbst
     * in den Vordergrund - das muss die aufrufende App uebernehmen.
     */
    private fun bringToForeground(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        try {
            context.startActivity(launchIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Konnte $packageName nicht in den Vordergrund holen", e)
        }
    }

    // Jeder Aufruf haelt seine eigene ServiceConnection-Instanz von bindService() bis zum passenden
    // unbindService() - keine geteilte, nur nach Package-Namen indizierte Map mehr. Bei einem
    // Doppeltipp (zwei parallele Aufrufe fuer dasselbe Package, z.B. weil der Button waehrend der
    // jetzt bis zu ~30s langen Wartezeit erneut gedrueckt wird) konnte das vorher dazu fuehren,
    // dass der finally-Block des einen Aufrufs die noch aktiv genutzte Verbindung des anderen
    // ungebindet, waehrend die eigene Verbindung verwaist im Speicher blieb (Leak). Da jeder
    // Aufruf jetzt seine eigene Referenz mitfuehrt, ist das strukturell ausgeschlossen.
    private suspend fun bindOsmAndService(context: Context, packageName: String): Pair<IBinder, ServiceConnection>? =
        withTimeoutOrNull(15000) {
            suspendCancellableCoroutine { cont ->
                lateinit var connection: ServiceConnection
                connection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        if (cont.isActive) {
                            if (service != null) cont.resume(service to connection) else cont.resume(null)
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        // Nichts zu tun: der Aufrufer haelt seine eigene Referenz und bindet sie
                        // in seinem finally-Block selbst ab.
                    }
                }
                val intent = Intent(BIND_ACTION).apply { setPackage(packageName) }
                val bound = try {
                    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                } catch (e: SecurityException) {
                    Log.e(TAG, "bindService($packageName) warf SecurityException", e)
                    false
                }
                Log.i(TAG, "bindService($packageName) Rueckgabewert: $bound")
                if (!bound && cont.isActive) {
                    cont.resume(null)
                }
                cont.invokeOnCancellation {
                    unbindQuietly(context, connection)
                }
            }
        }

    private fun unbindQuietly(context: Context, connection: ServiceConnection) {
        try {
            context.unbindService(connection)
        } catch (e: IllegalArgumentException) {
            // bereits ungebunden
        }
    }
}
