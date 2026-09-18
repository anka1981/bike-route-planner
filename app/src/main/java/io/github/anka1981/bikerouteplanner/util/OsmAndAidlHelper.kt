package io.github.anka1981.bikerouteplanner.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import net.osmand.aidlapi.IOsmAndAidlInterface
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

    suspend fun tryNavigateGpx(context: Context, gpxContent: String, trackName: String): Boolean {
        for (packageName in CANDIDATE_PACKAGES) {
            if (navigateViaPackage(context, packageName, gpxContent, trackName)) {
                return true
            }
        }
        return false
    }

    private suspend fun navigateViaPackage(
        context: Context,
        packageName: String,
        gpxContent: String,
        trackName: String
    ): Boolean {
        val binder = bindOsmAndService(context, packageName) ?: run {
            Log.w(TAG, "bindOsmAndService($packageName) lieferte keinen Binder (Timeout oder bindService()=false)")
            return false
        }
        Log.i(TAG, "bindOsmAndService($packageName) erfolgreich, rufe navigateGpx auf")
        return try {
            val aidlInterface = IOsmAndAidlInterface.Stub.asInterface(binder)
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
            result
        } catch (e: Exception) {
            Log.e(TAG, "navigateGpx($packageName) warf Exception", e)
            false
        } finally {
            unbindQuietly(context, packageName)
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

    private val connections = mutableMapOf<String, ServiceConnection>()

    // Grosszuegige Sicherheitsmarge fuer den seltenen Fall, dass OsmAnds Prozess selbst noch nicht
    // laeuft und BIND_AUTO_CREATE ihn hier erst hochfahren muss (dessen Kartenengine kann dabei
    // laenger als ein paar Sekunden brauchen). Lief OsmAnds Prozess schon, bindet dieser Aufruf
    // ohnehin praktisch sofort.
    private suspend fun bindOsmAndService(context: Context, packageName: String): IBinder? =
        withTimeoutOrNull(15000) {
            suspendCancellableCoroutine { cont ->
                val intent = Intent(BIND_ACTION).apply { setPackage(packageName) }
                val connection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        if (cont.isActive) cont.resume(service)
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        connections.remove(packageName)
                    }
                }
                connections[packageName] = connection
                val bound = try {
                    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                } catch (e: SecurityException) {
                    Log.e(TAG, "bindService($packageName) warf SecurityException", e)
                    false
                }
                Log.i(TAG, "bindService($packageName) Rueckgabewert: $bound")
                if (!bound) {
                    connections.remove(packageName)
                    if (cont.isActive) cont.resume(null)
                }
                cont.invokeOnCancellation {
                    unbindQuietly(context, packageName)
                }
            }
        }

    private fun unbindQuietly(context: Context, packageName: String) {
        connections.remove(packageName)?.let {
            try {
                context.unbindService(it)
            } catch (e: IllegalArgumentException) {
                // bereits ungebunden
            }
        }
    }
}
