package io.github.anka1981.bikerouteplanner.data

import kotlinx.serialization.Serializable
import java.util.UUID

/** Ein aufgelöster Punkt (Start, Zwischenstopp oder Ziel) mit Koordinaten. */
@Serializable
data class Waypoint(
    val label: String,
    val lat: Double,
    val lon: Double
)

/** Ein vom Nutzer gespeicherter Favorit, z.B. "Zuhause" oder ein Kontakt aus dem Adressbuch. */
@Serializable
data class FavoriteAddress(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val lat: Double,
    val lon: Double
)

fun FavoriteAddress.toWaypoint() = Waypoint(label, lat, lon)

/** Eine Zeile in der Wegpunkt-Liste der Route-Eingabe, bevor sie geokodiert wurde. */
data class WaypointInput(
    val id: String = UUID.randomUUID().toString(),
    val query: String = "",
    val resolved: Waypoint? = null,
    val candidates: List<Waypoint> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)

/** Entspricht 1:1 den bbbike "Wegeeinstellungen" (pref_* Parametern der Routing-API). */
@Serializable
data class RoutePreferences(
    val speedKmh: Int = 15,
    val category: String = "",
    val quality: String = "",
    /** "" (egal), "GR1" (bevorzugen) oder "GR2" (stark bevorzugen). */
    val greenRoute: String = "",
    val allowFerry: Boolean = true,
    val avoidUnlit: Boolean = false,
    val avoidTrafficLights: Boolean = false,
    val includeUnknownStreets: Boolean = false,
    /** "" (nichts weiter), "trailer" (Anhänger) oder "childseat" (Kindersitz mit Kind). */
    val specialVehicle: String = ""
)

/** Ein benanntes, gespeichertes Set von Wegeeinstellungen (z.B. "Rennrad", "Gemütliche Tour"). */
@Serializable
data class RouteProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val preferences: RoutePreferences = RoutePreferences()
)

val defaultRouteProfile = RouteProfile(id = "default", name = "Standard")

data class AppSettings(
    val appId: String = "guest",
    val citySlug: String = "bbbike",
    /** Sprache der bbbike-Routing-Antwort (Straßennamen etc.): de, en, es, fr, ru. */
    val routingLanguage: String = "de",
    /** Sprache der App-Oberfläche selbst, siehe ui/i18n/AppStrings.kt. */
    val uiLanguage: String = "de",
    /** "system", "light", "dark" oder "colorful", siehe ui/theme/AppColorTheme. */
    val colorTheme: String = "system",
    /**
     * Ob die Ereignisse (temporaere Sperrungen) bei www.bbbike.de abgefragt werden duerfen.
     * Diese Abfrage laeuft unverschluesselt per http, weil die Seite kein https anbietet.
     */
    val loadRouteEvents: Boolean = true,
    /**
     * Entfernt beim naechsten Senden einer Route die zuvor an OsmAnd gesendete Route wieder aus
     * dessen Track-Liste (siehe OsmAndAidlHelper), damit sich dort keine Fragmente ansammeln.
     */
    val removePreviousOsmAndTrack: Boolean = false
)

/** Fahrzeit laut bbbike fuer eine Geschwindigkeitsstufe. */
data class SpeedTime(
    val speedKmh: Int,
    val hours: Double,
    /** true fuer die in den Wegeeinstellungen gewaehlte Geschwindigkeit. */
    val isPreferred: Boolean
)

/** Von bbbike gelieferte Kennzahlen einer Route (bei mehreren Etappen aufsummiert). */
data class RouteStats(
    val lengthMeters: Double,
    /** null, wenn bbbike fuer mindestens eine Etappe keine Ampelzahl geliefert hat. */
    val trafficLights: Int?,
    val speedTimes: List<SpeedTime>
)

/** Eine bbbike-Meldung ("Ereignis"), die die Route beruehrt: Sperrung, Baustelle, Markt. */
data class RouteEvent(
    /** Kennung fuer die Ausweichroute ("temp-blocking-<n>"); null, wenn bbbike keine lieferte. */
    val id: String?,
    val text: String,
    /** true bei wiederkehrenden Meldungen, z.B. Wochenmarkt oder naechtliche Sperrung. */
    val recurring: Boolean,
    /** bbbike-Art der Meldung, z.B. "gesperrt". */
    val type: String?
)

sealed class RouteState {
    data object Idle : RouteState()
    data object Loading : RouteState()
    data class Success(
        val gpxFilePath: String,
        val legCount: Int,
        val pointCount: Int,
        val stats: RouteStats,
        val events: List<RouteEvent>,
        /** Anzahl der Ereignisse, um die diese Route herumgefuehrt wurde. */
        val avoidedEventCount: Int
    ) : RouteState()

    data class Error(val message: String) : RouteState()
}
