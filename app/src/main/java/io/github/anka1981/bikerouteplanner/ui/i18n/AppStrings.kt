package io.github.anka1981.bikerouteplanner.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

/**
 * Uebersetzungs-Katalog der App-Oberflaeche (getrennt von der bbbike-"Routing-Antwortsprache",
 * die die Fahrradroute selbst betrifft, siehe AppSettings.routingLanguage).
 *
 * Um eine weitere Sprache zu ergaenzen: ein neues `object` analog zu [GermanStrings]/
 * [EnglishStrings] anlegen, das dieses Interface implementiert, und in [availableUiLanguages]
 * sowie [stringsFor] eintragen.
 */
interface AppStrings {
    val languageCode: String
    val languageDisplayName: String

    val back: String

    // RouteScreen
    val appTitle: String
    fun activeCityLabel(city: String): String
    val preferencesIconDesc: String
    val settingsIconDesc: String
    val waypointStart: String
    val waypointDestination: String
    val waypointVia: String
    val addWaypoint: String
    val searchIconDesc: String
    val useCurrentLocationDesc: String
    val pickOnMapDesc: String
    val pickContactDesc: String
    val useFavoriteDesc: String
    val removeFavoriteDesc: String
    val saveFavoriteDesc: String
    val removeWaypointDesc: String
    val searching: String
    val noResultsFound: String
    val currentLocationLabel: String
    val currentLocationUnavailable: String
    fun mapPickedLocationLabel(lat: Double, lon: Double): String
    val mapPickerTitle: String
    val mapPickerHint: String
    val mapPickerConfirm: String
    val centerOnCurrentLocationDesc: String
    val selectAllWaypointsError: String
    val unknownError: String
    val contactAddressNotFound: String
    val computeRoute: String
    val reverseRouteButton: String
    val computingRoute: String
    val startingOsmAndNavigation: String
    val osmAndUnavailable: String
    val navigateInOsmAnd: String
    val showRouteOnMap: String
    val routeMapTitle: String
    val sendToMapApp: String
    fun searchFailed(message: String): String
    fun routeSummary(legCount: Int, pointCount: Int): String
    fun routeDistance(km: Double): String
    fun trafficLightCount(count: Int): String
    val travelTimeHeading: String
    fun speedLabel(speedKmh: Int, isPreferred: Boolean): String
    fun eventsButton(count: Int): String
    fun eventsDialogTitle(count: Int): String
    val eventsDialogIntro: String
    val eventRecurring: String
    val eventsClose: String
    val computeAlternativeRoute: String
    fun alternativeRouteNote(count: Int): String

    // RouteRepository
    val minRouteWaypointsRequired: String
    fun legFailed(index: Int, from: String, to: String, message: String): String
    fun unexpectedBbbikeResponse(index: Int, from: String, to: String, citySlug: String, snippet: String): String

    // PreferencesScreen
    val preferencesTitle: String
    val preferencesIntro: String
    val profileLabel: String
    val deleteProfileDesc: String
    val newProfileNameLabel: String
    val save: String
    val renameProfile: String
    val nameForNewProfileLabel: String
    val saveAsNew: String
    val categoryLabel: String
    val qualityLabel: String
    val greenRouteLabel: String
    val allowFerryLabel: String
    val avoidUnlitLabel: String
    val avoidTrafficLightsLabel: String
    val includeUnknownStreetsLabel: String
    val specialVehicleLabel: String
    fun speedLabel(kmh: Int): String

    /** Originalbezeichnungen von bbbike.de fuer pref_cat (Code zu Anzeigetext). */
    val categoryOptions: List<Pair<String, String>>

    /** Originalbezeichnungen von bbbike.de fuer pref_quality (Code zu Anzeigetext). */
    val qualityOptions: List<Pair<String, String>>

    /** Originalbezeichnungen von bbbike.de fuer pref_green (Code zu Anzeigetext). */
    val greenRouteOptions: List<Pair<String, String>>

    /** Originalbezeichnungen von bbbike.de fuer pref_specialvehicle (Code zu Anzeigetext). */
    val specialVehicleOptions: List<Pair<String, String>>

    // SettingsScreen
    val settingsTitle: String
    val citySlugLabel: String
    val citySlugHelp: String
    val citySearchLabel: String
    val refreshCitiesButton: String
    val loadingCities: String
    fun citiesLoaded(count: Int): String
    fun cityUpdateFailed(message: String): String
    val appIdLabel: String
    val appIdShowDesc: String
    val appIdHideDesc: String
    val appIdHelp: String
    val routingLanguageLabel: String
    val uiLanguageLabel: String
    val colorThemeLabel: String
    val colorThemeOptions: List<Pair<String, String>>
    val routeEventsLabel: String
    val routeEventsHelp: String
    val osmAndCleanupLabel: String
    val osmAndCleanupHelp: String
    val aboutButton: String
    val helpButton: String
    val helpTitle: String

    // AboutScreen
    val aboutTitle: String
    val versionLabel: String
    val routeMapInfo: String
    val osmAndSetupHeading: String
    val osmAndSetupSteps: List<String>
    val sourcesHeading: String
    val licenseNote: String
}

object GermanStrings : AppStrings {
    override val languageCode = "de"
    override val languageDisplayName = "Deutsch"

    override val back = "Zurück"

    override val appTitle = "Fahrradroute (bbbike)"
    override fun activeCityLabel(city: String) = "Stadt: $city (antippen zum Ändern)"
    override val preferencesIconDesc = "Wegeeinstellungen"
    override val settingsIconDesc = "Einstellungen"
    override val waypointStart = "Start"
    override val waypointDestination = "Ziel"
    override val waypointVia = "Zwischenstopp"
    override val addWaypoint = "Zwischenstopp hinzufügen"
    override val searchIconDesc = "Suchen"
    override val useCurrentLocationDesc = "Aktuelle GPS-Position als Start verwenden"
    override val pickOnMapDesc = "Auf Karte auswählen"
    override val pickContactDesc = "Aus Kontakten wählen"
    override val useFavoriteDesc = "Favorit verwenden"
    override val removeFavoriteDesc = "Favorit löschen"
    override val saveFavoriteDesc = "Als Favorit speichern"
    override val removeWaypointDesc = "Wegpunkt entfernen"
    override val searching = "Suche läuft..."
    override val noResultsFound = "Keine Treffer gefunden."
    override val currentLocationLabel = "Aktueller Standort"
    override val currentLocationUnavailable = "Aktueller Standort nicht verfügbar. GPS/Standort aktiviert?"
    override fun mapPickedLocationLabel(lat: Double, lon: Double) =
        "Punkt (%.5f, %.5f)".format(lat, lon)
    override val mapPickerTitle = "Punkt auf Karte auswählen"
    override val mapPickerHint = "Karte antippen, um den Punkt zu setzen"
    override val mapPickerConfirm = "Übernehmen"
    override val centerOnCurrentLocationDesc = "Auf aktuellen Standort zentrieren"
    override val selectAllWaypointsError =
        "Bitte alle Wegpunkte auswählen (Start, Ziel, ggf. Zwischenstopps)."
    override val unknownError = "Unbekannter Fehler."
    override val contactAddressNotFound =
        "Keine Adresse gefunden (Kontakt hat keine Adresse gespeichert, oder die Kontakte-Berechtigung wurde nicht erteilt)."
    override val computeRoute = "Route berechnen"
    override val reverseRouteButton = "Rückweg"
    override val computingRoute = "Route wird berechnet..."
    override val startingOsmAndNavigation = "Starte Navigation in OsmAnd..."
    override val osmAndUnavailable =
        "Direkte Navigation aktuell nicht möglich (OsmAnds interne App-Freigabe lässt sich " +
            "von dieser App aus nicht zuverlässig aktivieren). Bitte stattdessen " +
            "\"An Karten-App senden\" nutzen und dort OsmAnd auswählen."
    override val navigateInOsmAnd = "Direkt in OsmAnd navigieren"
    override val showRouteOnMap = "Route auf Karte anzeigen"
    override val routeMapTitle = "Route"
    override val sendToMapApp = "An Karten-App senden"
    override fun searchFailed(message: String) = "Suche fehlgeschlagen: $message"
    override fun routeSummary(legCount: Int, pointCount: Int) =
        "Route mit $legCount Etappe(n) und $pointCount Punkten bereit."
    override fun routeDistance(km: Double) = "Länge: ${String.format(Locale.GERMANY, "%.1f", km)} km"
    override fun trafficLightCount(count: Int) = "Ampeln: $count"
    override val travelTimeHeading = "Fahrzeit laut bbbike"
    override fun speedLabel(speedKmh: Int, isPreferred: Boolean) =
        "$speedKmh km/h" + if (isPreferred) " (eingestellt)" else ""
    override fun eventsButton(count: Int) =
        if (count == 0) "Keine Ereignisse auf der Route" else "Ereignisse auf der Route ($count)"
    override fun eventsDialogTitle(count: Int) = "Ereignisse auf der Route ($count)"
    override val eventsDialogIntro =
        "Meldungen von bbbike zu dieser Route. Angehakte Ereignisse werden bei der " +
            "Ausweichroute umfahren."
    override val eventRecurring = "wiederkehrend"
    override val eventsClose = "Schließen"
    override val computeAlternativeRoute = "Ausweichroute berechnen"
    override fun alternativeRouteNote(count: Int) =
        if (count == 1) "Ausweichroute um 1 Ereignis berechnet."
        else "Ausweichroute um $count Ereignisse berechnet."

    override val minRouteWaypointsRequired = "Es werden mindestens Start und Ziel benötigt."
    override fun legFailed(index: Int, from: String, to: String, message: String) =
        "Etappe $index ($from -> $to) fehlgeschlagen: $message"
    override fun unexpectedBbbikeResponse(
        index: Int,
        from: String,
        to: String,
        citySlug: String,
        snippet: String
    ) = "Etappe $index ($from -> $to): unerwartete Antwort von bbbike " +
        "(evtl. ungültige appid oder Stadt '$citySlug'): $snippet"

    override val preferencesTitle = "Wegeeinstellungen"
    override val preferencesIntro =
        "Diese Optionen entsprechen den Original-Einstellungen der bbbike-Routing-API. " +
            "Details siehe bbbike.org."
    override val profileLabel = "Profil"
    override val deleteProfileDesc = "Profil löschen"
    override val newProfileNameLabel = "Neuer Profilname"
    override val save = "Speichern"
    override val renameProfile = "Profil umbenennen"
    override val nameForNewProfileLabel = "Name für neues Profil"
    override val saveAsNew = "Speichern"
    override val categoryLabel = "Profil / Kategorie"
    override val qualityLabel = "Wegequalität"
    override val greenRouteLabel = "Grüne Wege"
    override val allowFerryLabel = "Fähren benutzen"
    override val avoidUnlitLabel = "Unbeleuchtete Wege vermeiden"
    override val avoidTrafficLightsLabel = "Ampeln vermeiden"
    override val includeUnknownStreetsLabel = "Unbekannte Straßen mit einbeziehen"
    override val specialVehicleLabel = "Unterwegs mit"
    override fun speedLabel(kmh: Int) = "Geschwindigkeit: $kmh km/h"
    override val categoryOptions = listOf(
        "" to "egal",
        "N1" to "Nebenstraßen bevorzugen",
        "N2" to "nur Nebenstraßen benutzen",
        "H1" to "Hauptstraßen bevorzugen",
        "H2" to "nur Hauptstraßen benutzen",
        "N_RW" to "Hauptstraßen ohne Radwege/Busspuren meiden",
        "N_RW1" to "Hauptstraßen ohne Radwege meiden"
    )
    override val qualityOptions = listOf(
        "" to "egal",
        "Q2" to "Kopfsteinpflaster und schlechte Fahrbahnen vermeiden",
        "Q0" to "nur sehr gute Beläge bevorzugen (rennradtauglich)"
    )
    override val greenRouteOptions = listOf(
        "" to "egal",
        "GR1" to "bevorzugen",
        "GR2" to "stark bevorzugen"
    )
    override val specialVehicleOptions = listOf(
        "" to "nichts weiter",
        "trailer" to "Anhänger",
        "childseat" to "Kindersitz mit Kind"
    )

    override val settingsTitle = "Einstellungen"
    override val citySlugLabel = "Stadt / Region (bbbike-Instanz)"
    override val citySlugHelp =
        "bbbike deckt jede Stadt über eine eigene Instanz ab (z.B. \"Berlin\", " +
            "\"Hamburg\", \"München\"). Vollständige Liste: bbbike.org. Für Berlin wird " +
            "\"bbbike\" (die historische Originalinstanz) empfohlen: sie liefert aktuellere " +
            "und schnellere Ergebnisse als die separat gelistete \"Berlin\"-Instanz."
    override val citySearchLabel = "Stadt suchen"
    override val refreshCitiesButton = "Städteliste von bbbike.org aktualisieren"
    override val loadingCities = "Städteliste wird von bbbike.org geladen..."
    override fun citiesLoaded(count: Int) = "$count Städte geladen."
    override fun cityUpdateFailed(message: String) = "Aktualisierung fehlgeschlagen: $message"
    override val appIdLabel = "Eigene bbbike appid (optional)"
    override val appIdShowDesc = "appid anzeigen"
    override val appIdHideDesc = "appid verbergen"
    override val appIdHelp =
        "Leer lassen, um die in der App eingebaute Standard-appid zu verwenden. Eine hier " +
            "eingetragene eigene appid überschreibt sie. Eine eigene appid gibt es auf " +
            "Anfrage bei bbbike.org (siehe bbbike.org/api.html)."
    override val routingLanguageLabel = "Sprache der Routing-Antwort (de, en, es, fr, ru)"
    override val uiLanguageLabel = "App-Sprache"
    override val routeEventsLabel = "Ereignisse auf der Route laden"
    override val routeEventsHelp =
        "Fragt bei www.bbbike.de Sperrungen, Baustellen und Märkte zur Route ab und ermöglicht " +
            "die Ausweichroute. Nur für Berlin. Achtung: Diese Abfrage läuft unverschlüsselt " +
            "über http, weil die Seite kein https anbietet. Start und Ziel sind dabei im Netz " +
            "mitlesbar, und die Antwort ließe sich unterwegs verändern. Ausgeschaltet nimmt die " +
            "App keine Verbindung zu www.bbbike.de auf."
    override val osmAndCleanupLabel = "Vorherige Route in OsmAnd entfernen"
    override val osmAndCleanupHelp =
        "Entfernt beim nächsten Senden einer Route die zuvor an OsmAnd gesendete Route wieder " +
            "aus dessen Track-Liste, damit sich dort keine Fragmente ansammeln. Sinnvoll, wenn " +
            "OsmAnd ausschließlich zur Navigation dieser App genutzt wird. Die zuletzt gesendete " +
            "Route bleibt bis zum nächsten Senden in OsmAnd bestehen, damit die laufende " +
            "Navigation nicht unterbrochen wird."
    override val colorThemeLabel = "App-Design"
    override val colorThemeOptions = listOf(
        "system" to "System",
        "light" to "Hell",
        "dark" to "Dunkel",
        "colorful" to "Farbig"
    )
    override val aboutButton = "Über diese App"
    override val helpButton = "Hilfe"
    override val helpTitle = "Hilfe"

    override val aboutTitle = "Über diese App"
    override val versionLabel = "Version"
    override val routeMapInfo =
        "\"Route auf Karte anzeigen\" zeigt die berechnete Strecke direkt in dieser App an " +
            "(mit laufend aktualisiertem eigenen Standort), ganz ohne externe Karten-App. " +
            "Das ist eine reine Kartenansicht zur Orientierung - keine sprachgeführte " +
            "Turn-by-Turn-Navigation mit automatischer Neuberechnung bei Abweichung; dafür " +
            "bleibt die Direktnavigation in OsmAnd die bessere Wahl."
    override val osmAndSetupHeading = "OsmAnd für Direktnavigation einrichten"
    override val osmAndSetupSteps = listOf(
        "OsmAnd installieren, falls noch nicht geschehen.",
        "In dieser App eine Route berechnen und \"Direkt in OsmAnd navigieren\" antippen.",
        "Erscheint die Meldung \"Direkte Navigation aktuell nicht möglich\": das Handy einmal " +
            "neu starten (Android muss diese App erst für OsmAnd sichtbar machen) und den " +
            "vorigen Schritt wiederholen.",
        "In OsmAnd: Menü → Einstellungen → Erweiterungen öffnen und ganz nach unten scrollen. " +
            "Dort erscheint \"BikeRoutePlanner\" als Drittanbieter-Anwendung.",
        "Diesen Eintrag antippen, um die App in OsmAnd freizuschalten.",
        "Ab jetzt startet \"Direkt in OsmAnd navigieren\" die Navigation automatisch und holt " +
            "OsmAnd in den Vordergrund."
    )
    override val sourcesHeading = "Quellen & Dokumentation"
    override val licenseNote =
        "Diese App ist ein privates, nicht-kommerzielles Projekt und steht in keiner " +
            "Verbindung zu bbbike.org, OsmAnd oder OpenStreetMap."
}

object EnglishStrings : AppStrings {
    override val languageCode = "en"
    override val languageDisplayName = "English"

    override val back = "Back"

    override val appTitle = "Bike route (bbbike)"
    override fun activeCityLabel(city: String) = "City: $city (tap to change)"
    override val preferencesIconDesc = "Route preferences"
    override val settingsIconDesc = "Settings"
    override val waypointStart = "Start"
    override val waypointDestination = "Destination"
    override val waypointVia = "Stop"
    override val addWaypoint = "Add stop"
    override val searchIconDesc = "Search"
    override val useCurrentLocationDesc = "Use current GPS position as start"
    override val pickOnMapDesc = "Pick on map"
    override val pickContactDesc = "Choose from contacts"
    override val useFavoriteDesc = "Use favorite"
    override val removeFavoriteDesc = "Delete favorite"
    override val saveFavoriteDesc = "Save as favorite"
    override val removeWaypointDesc = "Remove waypoint"
    override val searching = "Searching..."
    override val noResultsFound = "No results found."
    override val currentLocationLabel = "Current location"
    override val currentLocationUnavailable = "Current location unavailable. Is GPS/location enabled?"
    override fun mapPickedLocationLabel(lat: Double, lon: Double) =
        "Point (%.5f, %.5f)".format(lat, lon)
    override val mapPickerTitle = "Pick a point on the map"
    override val mapPickerHint = "Tap the map to set the point"
    override val mapPickerConfirm = "Use this point"
    override val centerOnCurrentLocationDesc = "Center on current location"
    override val selectAllWaypointsError =
        "Please resolve all waypoints (start, destination, and any stops)."
    override val unknownError = "Unknown error."
    override val contactAddressNotFound =
        "No address found (the contact has no saved address, or contacts permission was not granted)."
    override val computeRoute = "Calculate route"
    override val reverseRouteButton = "Return trip"
    override val computingRoute = "Calculating route..."
    override val startingOsmAndNavigation = "Starting navigation in OsmAnd..."
    override val osmAndUnavailable =
        "Direct navigation isn't currently possible (OsmAnd's internal app approval can't be " +
            "reliably enabled from this app). Please use \"Send to map app\" instead and " +
            "choose OsmAnd there."
    override val navigateInOsmAnd = "Navigate directly in OsmAnd"
    override val showRouteOnMap = "Show route on map"
    override val routeMapTitle = "Route"
    override val sendToMapApp = "Send to map app"
    override fun searchFailed(message: String) = "Search failed: $message"
    override fun routeSummary(legCount: Int, pointCount: Int) =
        "Route with $legCount leg(s) and $pointCount points ready."
    override fun routeDistance(km: Double) = "Length: ${String.format(Locale.US, "%.1f", km)} km"
    override fun trafficLightCount(count: Int) = "Traffic lights: $count"
    override val travelTimeHeading = "Travel time according to bbbike"
    override fun speedLabel(speedKmh: Int, isPreferred: Boolean) =
        "$speedKmh km/h" + if (isPreferred) " (selected)" else ""
    override fun eventsButton(count: Int) =
        if (count == 0) "No events on the route" else "Events on the route ($count)"
    override fun eventsDialogTitle(count: Int) = "Events on the route ($count)"
    override val eventsDialogIntro =
        "Reports from bbbike about this route. Ticked events are avoided by the alternative route."
    override val eventRecurring = "recurring"
    override val eventsClose = "Close"
    override val computeAlternativeRoute = "Calculate alternative route"
    override fun alternativeRouteNote(count: Int) =
        if (count == 1) "Alternative route around 1 event calculated."
        else "Alternative route around $count events calculated."

    override val minRouteWaypointsRequired = "At least a start and destination are required."
    override fun legFailed(index: Int, from: String, to: String, message: String) =
        "Leg $index ($from -> $to) failed: $message"
    override fun unexpectedBbbikeResponse(
        index: Int,
        from: String,
        to: String,
        citySlug: String,
        snippet: String
    ) = "Leg $index ($from -> $to): unexpected response from bbbike " +
        "(possibly an invalid appid or city '$citySlug'): $snippet"

    override val preferencesTitle = "Route preferences"
    override val preferencesIntro =
        "These options correspond directly to the original bbbike routing API settings. " +
            "Details at bbbike.org."
    override val profileLabel = "Profile"
    override val deleteProfileDesc = "Delete profile"
    override val newProfileNameLabel = "New profile name"
    override val save = "Save"
    override val renameProfile = "Rename profile"
    override val nameForNewProfileLabel = "Name for new profile"
    override val saveAsNew = "Save"
    override val categoryLabel = "Profile / category"
    override val qualityLabel = "Path quality"
    override val greenRouteLabel = "Green paths"
    override val allowFerryLabel = "Use ferries"
    override val avoidUnlitLabel = "Avoid unlit paths"
    override val avoidTrafficLightsLabel = "Avoid traffic lights"
    override val includeUnknownStreetsLabel = "Include unknown streets"
    override val specialVehicleLabel = "Traveling with"
    override fun speedLabel(kmh: Int) = "Speed: $kmh km/h"
    override val categoryOptions = listOf(
        "" to "no preference",
        "N1" to "prefer minor/side streets",
        "N2" to "use only minor/side streets",
        "H1" to "prefer main roads",
        "H2" to "use only main roads",
        "N_RW" to "avoid main roads without bike paths/bus lanes",
        "N_RW1" to "avoid main roads without bike paths"
    )
    override val qualityOptions = listOf(
        "" to "no preference",
        "Q2" to "avoid cobblestones and bad road surfaces",
        "Q0" to "only very good surfaces (racing-bike suitable)"
    )
    override val greenRouteOptions = listOf(
        "" to "no preference",
        "GR1" to "prefer",
        "GR2" to "strongly prefer"
    )
    override val specialVehicleOptions = listOf(
        "" to "nothing special",
        "trailer" to "Trailer",
        "childseat" to "Child seat with child"
    )

    override val settingsTitle = "Settings"
    override val citySlugLabel = "City / region (bbbike instance)"
    override val citySlugHelp =
        "bbbike covers each city through its own instance (e.g. \"Berlin\", " +
            "\"Hamburg\", \"München\"). Full list: bbbike.org. For Berlin, \"bbbike\" (the " +
            "historical original instance) is recommended: it delivers more current and " +
            "faster results than the separately listed \"Berlin\" instance."
    override val citySearchLabel = "Search city"
    override val refreshCitiesButton = "Refresh city list from bbbike.org"
    override val loadingCities = "Loading city list from bbbike.org..."
    override fun citiesLoaded(count: Int) = "$count cities loaded."
    override fun cityUpdateFailed(message: String) = "Update failed: $message"
    override val appIdLabel = "Own bbbike appid (optional)"
    override val appIdShowDesc = "show appid"
    override val appIdHideDesc = "hide appid"
    override val appIdHelp =
        "Leave empty to use the default appid built into the app. An own appid entered here " +
            "overrides it. You can request your own appid from bbbike.org (see " +
            "bbbike.org/api.html)."
    override val routingLanguageLabel = "Routing response language (de, en, es, fr, ru)"
    override val uiLanguageLabel = "App language"
    override val routeEventsLabel = "Load events on the route"
    override val routeEventsHelp =
        "Asks www.bbbike.de for closures, roadworks, and markets along the route and enables " +
            "the alternative route. Berlin only. Caution: this request uses unencrypted http, " +
            "because the site offers no https. Start and destination are readable on the " +
            "network, and the response could be tampered with on the way. When switched off, " +
            "the app never connects to www.bbbike.de."
    override val osmAndCleanupLabel = "Remove previous route from OsmAnd"
    override val osmAndCleanupHelp =
        "The next time a route is sent, removes the previously sent route from OsmAnd's track " +
            "list again, so fragments don't pile up there. Useful if OsmAnd is only used for " +
            "navigating this app's routes. The most recently sent route stays in OsmAnd until " +
            "the next one is sent, so the ongoing navigation isn't interrupted."
    override val colorThemeLabel = "App theme"
    override val colorThemeOptions = listOf(
        "system" to "System",
        "light" to "Light",
        "dark" to "Dark",
        "colorful" to "Colorful"
    )
    override val aboutButton = "About this app"
    override val helpButton = "Help"
    override val helpTitle = "Help"

    override val aboutTitle = "About this app"
    override val versionLabel = "Version"
    override val routeMapInfo =
        "\"Show route on map\" displays the computed route right inside this app (with a " +
            "continuously updated position of your own), no external map app needed. It's a " +
            "plain map view for orientation, not voice-guided turn-by-turn navigation with " +
            "automatic rerouting - for that, direct navigation in OsmAnd remains the better " +
            "choice."
    override val osmAndSetupHeading = "Setting up OsmAnd for direct navigation"
    override val osmAndSetupSteps = listOf(
        "Install OsmAnd if you haven't already.",
        "In this app, compute a route and tap \"Navigate directly in OsmAnd\".",
        "If you see \"Direct navigation isn't currently possible\": restart your phone once " +
            "(Android needs to make this app visible to OsmAnd) and repeat the previous step.",
        "In OsmAnd: open Menu → Settings → Plugins and scroll all the way down. " +
            "\"BikeRoutePlanner\" should appear there as a third-party application.",
        "Tap that entry to enable this app in OsmAnd.",
        "From now on, \"Navigate directly in OsmAnd\" will start navigation automatically and " +
            "bring OsmAnd to the front."
    )
    override val sourcesHeading = "Sources & documentation"
    override val licenseNote =
        "This app is a private, non-commercial project and is not affiliated with " +
            "bbbike.org, OsmAnd, or OpenStreetMap."
}

val availableUiLanguages: List<AppStrings> = listOf(GermanStrings, EnglishStrings)

fun stringsFor(languageCode: String): AppStrings =
    availableUiLanguages.find { it.languageCode == languageCode } ?: GermanStrings

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { GermanStrings }

data class SourceEntry(val title: String, val url: String, val description: String)

fun aboutSources(strings: AppStrings): List<SourceEntry> = if (strings.languageCode == "en") {
    listOf(
        SourceEntry(
            "bbbike.org Routing API",
            "https://www.bbbike.org/api.html",
            "Route calculation and all route preference (pref_*) parameters."
        ),
        SourceEntry(
            "bbbike.org city list",
            "https://www.bbbike.org/",
            "List of supported cities/regions, used to populate the city picker."
        ),
        SourceEntry(
            "OpenStreetMap Nominatim",
            "https://operations.osmfoundation.org/policies/nominatim/",
            "Geocoding of addresses entered as free text into coordinates, and reverse " +
                "geocoding of points picked on the map."
        ),
        SourceEntry(
            "OpenTopoMap",
            "https://opentopomap.org/about",
            "Map tiles shown in the on-map point picker and the in-app route map (osmdroid), " +
                "based on OpenStreetMap and SRTM data, used under OpenTopoMap's usage policy " +
                "for reasonable, non-commercial hobby use."
        ),
        SourceEntry(
            "OsmAnd API / AIDL interface",
            "https://osmand.net/docs/technical/osmand-api-sdk/",
            "Used to start navigation directly in OsmAnd. The bundled osmand-api module " +
                "is a vendored copy of osmandapp/OsmAnd's OsmAnd-api module (see its README). " +
                "OsmAnd gates this on an internal per-app approval that this app currently " +
                "cannot reliably enable, so navigation falls back to the generic \"Send to " +
                "map app\" share instead."
        )
    )
} else {
    listOf(
        SourceEntry(
            "bbbike.org Routing-API",
            "https://www.bbbike.org/api.html",
            "Routenberechnung und alle Wegeeinstellungen (pref_*-Parameter)."
        ),
        SourceEntry(
            "bbbike.org Städteliste",
            "https://www.bbbike.org/",
            "Liste der unterstützten Städte/Regionen, Basis für die Stadtauswahl."
        ),
        SourceEntry(
            "OpenStreetMap Nominatim",
            "https://operations.osmfoundation.org/policies/nominatim/",
            "Geokodierung frei eingegebener Adressen in Koordinaten, sowie Rückwärts-" +
                "Geokodierung von auf der Karte ausgewählten Punkten."
        ),
        SourceEntry(
            "OpenTopoMap",
            "https://opentopomap.org/about",
            "Kartenkacheln für die Punktauswahl und die Routenkarte in der App (osmdroid), " +
                "basierend auf OpenStreetMap- und SRTM-Daten, genutzt gemäß OpenTopoMaps " +
                "Richtlinie für angemessene, nicht-kommerzielle Hobby-Nutzung."
        ),
        SourceEntry(
            "OsmAnd API / AIDL-Schnittstelle",
            "https://osmand.net/docs/technical/osmand-api-sdk/",
            "Ermöglicht den direkten Navigationsstart in OsmAnd. Das mitgelieferte " +
                "osmand-api-Modul ist eine vendorte Kopie aus osmandapp/OsmAnd (siehe dessen README). " +
                "OsmAnd verlangt dafür eine interne App-Freigabe, die sich von dieser App aus " +
                "nicht zuverlässig aktivieren lässt - die Navigation läuft daher stattdessen " +
                "über den generischen \"An Karten-App senden\"-Weg."
        )
    )
}
