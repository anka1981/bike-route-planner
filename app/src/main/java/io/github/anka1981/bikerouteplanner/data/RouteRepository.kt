package io.github.anka1981.bikerouteplanner.data

import android.content.Context
import io.github.anka1981.bikerouteplanner.ui.i18n.AppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RouteComputationException(message: String) : Exception(message)

/** Ergebnis einer Routenberechnung: GPX-Datei, Trackpunkte, Kennzahlen und Ereignisse. */
data class ComputedRoute(
    val gpxFile: File,
    val pointCount: Int,
    val stats: RouteStats,
    /** bbbike-Meldungen, die diese Route beruehren; nur die Berliner Instanz liefert welche. */
    val events: List<RouteEvent>,
    /** Ereignisse, um die diese Route bewusst herumgefuehrt wurde. */
    val avoidedEventIds: List<String>
)

class RouteRepository(
    private val context: Context,
    private val api: BBBikeApi = NetworkModule.bbbikeApi,
    private val bbbikeDeApi: BbbikeDeApi = NetworkModule.bbbikeDeApi
) {

    /**
     * Berechnet eine Route ueber beliebig viele Wegpunkte, indem fuer jede Etappe
     * (Wegpunkt[i] -> Wegpunkt[i+1]) ein eigener bbbike-Request gestellt wird, weil die
     * API selbst nur einen einzelnen optionalen Via-Punkt pro Anfrage unterstuetzt.
     * Abgefragt wird das JSON-Format, weil nur dieses Laenge, Ampeln und Fahrzeiten enthaelt;
     * die Etappen-Punkte werden anschliessend zu einer GPX-Datei zusammengefuehrt.
     *
     * Die Ereignisse (temporaere Sperrungen, Baustellen, Maerkte) kennt nur www.bbbike.de.
     * Fuer die Berliner Instanz werden sie deshalb dort zusaetzlich abgefragt. Sind Ereignisse
     * zum Umfahren ausgewaehlt, liefert www.bbbike.de auch die Route selbst
     * ("custom=temp-blocking-<id>"), denn die offizielle API ignoriert diesen Parameter.
     */
    suspend fun computeRoute(
        waypoints: List<Waypoint>,
        prefs: RoutePreferences,
        settings: AppSettings,
        strings: AppStrings,
        avoidEventIds: List<String> = emptyList()
    ): ComputedRoute = withContext(Dispatchers.IO) {
        require(waypoints.size >= 2) { strings.minRouteWaypointsRequired }

        // Ereignisse - und damit auch Ausweichrouten - gibt es nur fuer Berlin, und nur wenn die
        // dafuer noetige unverschluesselte Abfrage in den Einstellungen erlaubt ist.
        val eventsAvailable = settings.loadRouteEvents &&
            settings.citySlug.lowercase(Locale.US) in BbbikeDeApi.BERLIN_SLUGS
        val avoiding = eventsAvailable && avoidEventIds.isNotEmpty()

        val legs = mutableListOf<BbbikeLeg>()
        for (i in 0 until waypoints.size - 1) {
            val from = waypoints[i]
            val to = waypoints[i + 1]

            val text = try {
                if (avoiding) {
                    bbbikeDeText(from, to, prefs, settings, avoidEventIds)
                } else {
                    apiText(from, to, prefs, settings)
                }
            } catch (e: Exception) {
                throw RouteComputationException(
                    strings.legFailed(i + 1, from.label, to.label, e.message ?: strings.unknownError)
                )
            }

            val leg = BbbikeJsonParser.parseLeg(text) ?: throw RouteComputationException(
                strings.unexpectedBbbikeResponse(i + 1, from.label, to.label, settings.citySlug, text.take(200))
            )
            legs.add(if (!avoiding && eventsAvailable) leg.copy(events = fetchEvents(from, to, prefs, settings)) else leg)
        }

        val allPoints = GpxMerger.joinLegs(legs.map { it.points })
        val merged = GpxMerger.toGpx(allPoints, buildTrackName(waypoints))

        val gpxDir = File(context.cacheDir, "gpx").apply { mkdirs() }
        val fileName = "route_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.gpx"
        val file = File(gpxDir, fileName)
        file.writeText(merged, Charsets.UTF_8)

        ComputedRoute(
            gpxFile = file,
            pointCount = allPoints.size,
            stats = BbbikeJsonParser.combine(legs.map { it.stats }),
            events = legs.flatMap { it.events }.distinctBy { it.id ?: it.text },
            avoidedEventIds = if (avoiding) avoidEventIds else emptyList()
        )
    }

    private suspend fun apiText(
        from: Waypoint,
        to: Waypoint,
        prefs: RoutePreferences,
        settings: AppSettings
    ): String = api.route(
        city = settings.citySlug,
        appId = settings.appId,
        start = "${from.lon},${from.lat}",
        ziel = "${to.lon},${to.lat}",
        startName = from.label,
        zielName = to.label,
        prefSpeed = prefs.speedKmh,
        prefCat = prefs.category.ifBlank { null },
        prefQuality = prefs.quality.ifBlank { null },
        prefGreen = prefs.greenRoute.ifBlank { null },
        prefFerry = if (prefs.allowFerry) "use" else null,
        prefUnlit = if (prefs.avoidUnlit) "NL" else null,
        prefAmpel = if (prefs.avoidTrafficLights) "yes" else null,
        prefFragezeichen = if (prefs.includeUnknownStreets) "yes" else null,
        prefSpecialvehicle = prefs.specialVehicle.ifBlank { null },
        lang = settings.routingLanguage,
        outputAs = "json"
    ).string()

    private suspend fun bbbikeDeText(
        from: Waypoint,
        to: Waypoint,
        prefs: RoutePreferences,
        settings: AppSettings,
        avoidEventIds: List<String>
    ): String = bbbikeDeApi.route(
        cgi = BbbikeDeApi.cgiFor(settings.routingLanguage),
        start = "${from.lon},${from.lat}",
        ziel = "${to.lon},${to.lat}",
        prefSpeed = prefs.speedKmh,
        prefCat = prefs.category.ifBlank { null },
        prefQuality = prefs.quality.ifBlank { null },
        prefGreen = prefs.greenRoute.ifBlank { null },
        prefFerry = if (prefs.allowFerry) "use" else null,
        prefUnlit = if (prefs.avoidUnlit) "NL" else null,
        prefAmpel = if (prefs.avoidTrafficLights) "yes" else null,
        prefFragezeichen = if (prefs.includeUnknownStreets) "yes" else null,
        prefSpecialvehicle = prefs.specialVehicle.ifBlank { null },
        avoidBlockings = avoidEventIds.takeIf { it.isNotEmpty() }
    ).string()

    /**
     * Holt nur die Ereignisse einer Etappe von www.bbbike.de. Fehler werden bewusst geschluckt:
     * Eine bereits berechnete Route soll nicht an dieser Zusatzabfrage scheitern, dann bleibt
     * der Ereignis-Knopf eben leer.
     */
    private suspend fun fetchEvents(
        from: Waypoint,
        to: Waypoint,
        prefs: RoutePreferences,
        settings: AppSettings
    ): List<RouteEvent> = try {
        BbbikeJsonParser.parseLeg(bbbikeDeText(from, to, prefs, settings, emptyList()))?.events.orEmpty()
    } catch (e: Exception) {
        emptyList()
    }

    private fun buildTrackName(waypoints: List<Waypoint>): String =
        "${waypoints.first().label} -> ${waypoints.last().label}"
}
