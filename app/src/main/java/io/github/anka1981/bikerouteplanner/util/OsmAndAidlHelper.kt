package io.github.anka1981.bikerouteplanner.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
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
            val result = aidlInterface.navigateGpx(params)
            Log.i(TAG, "navigateGpx($packageName) lieferte: $result")
            if (result) {
                bringToForeground(context, packageName)
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

    // OsmAnd ist eine grosse App mit eigener Kartenengine; ist ihr Prozess noch nicht am Laufen,
    // dauert das Hochfahren (das BIND_AUTO_CREATE hier erst anstoesst) auf manchen Geraeten
    // deutlich laenger als ein paar Sekunden. Ein zu kurzes Timeout liess den Bind-Versuch dann
    // fehlschlagen, obwohl OsmAnd kurz danach durchaus geantwortet haette - beobachtbares Symptom
    // war, dass das Senden nur klappte, wenn OsmAnd vorher schon manuell geoeffnet (und damit warm)
    // war. Ein bereits laufendes OsmAnd bindet weiterhin praktisch sofort, dieses Timeout greift
    // also nur im Kaltstart-Fall.
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
