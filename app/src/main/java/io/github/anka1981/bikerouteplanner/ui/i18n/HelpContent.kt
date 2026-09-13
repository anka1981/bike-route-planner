package io.github.anka1981.bikerouteplanner.ui.i18n

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

/** Eine einzelne erklaerte Funktion/Icon, optional mit dem tatsaechlich in der App verwendeten Icon. */
data class HelpItem(val icon: ImageVector?, val title: String, val description: String)

data class HelpSection(val heading: String, val items: List<HelpItem>)

/**
 * Vollstaendige Erklaerung aller Funktionen und Icons der App, gegliedert nach Bildschirm.
 * Folgt bewusst demselben Muster wie [aboutSources]: bilinguale Inhalte direkt hier hinterlegt,
 * statt jedes einzelne Item als eigenes Feld im [AppStrings]-Interface zu fuehren.
 */
fun helpSections(strings: AppStrings): List<HelpSection> = if (strings.languageCode == "en") {
    listOf(
        HelpSection(
            "Main screen",
            listOf(
                HelpItem(null, "City subtitle", "Shows the currently selected bbbike city/region under the title. Tap it to jump straight to Settings and change it."),
                HelpItem(Icons.Filled.Tune, "Route preferences", "Opens the bbbike route preferences (category, surface quality, green routes, speed, traffic lights, ...)."),
                HelpItem(Icons.Filled.Settings, "Settings", "Opens the app settings (city/region, bbbike appid, UI language, routing language, color theme).")
            )
        ),
        HelpSection(
            "Waypoint row (start / stop / destination)",
            listOf(
                HelpItem(Icons.Filled.Search, "Search", "Looks up the typed address via OpenStreetMap Nominatim."),
                HelpItem(Icons.Filled.LocationOn, "Current location", "Start point only: uses your current GPS position."),
                HelpItem(Icons.Filled.Map, "Pick on map", "Opens the map picker; tapping the map sets this waypoint to that point."),
                HelpItem(Icons.Filled.Contacts, "From contacts", "Uses the address stored on a contact from your address book."),
                HelpItem(Icons.Filled.Bookmarks, "Favorites", "Shows your saved favorite addresses to pick from."),
                HelpItem(Icons.Filled.BookmarkAdd, "Save as favorite", "Appears once an address is resolved; saves it as a new favorite."),
                HelpItem(Icons.Filled.BookmarkRemove, "Delete favorite", "Inside the favorites list: permanently removes that favorite."),
                HelpItem(Icons.Filled.Close, "Remove waypoint", "Removes this stop (only shown once there are more than start and destination)."),
                HelpItem(Icons.Filled.Add, "Add stop", "Adds another waypoint to the route.")
            )
        ),
        HelpSection(
            "Computing and using the route",
            listOf(
                HelpItem(null, "Calculate route", "Sends all resolved waypoints to bbbike and computes the bike route."),
                HelpItem(null, "Events on the route", "Becomes active when bbbike reports closures, roadworks, or markets along the route. The window lists them; ticked events are avoided when you tap \"Calculate alternative route\". Only available for the Berlin instance (bbbike)."),
                HelpItem(null, "Route statistics","After calculation: length, number of traffic lights, and bbbike's travel time at several speeds (your selected speed in bold)."),
                HelpItem(Icons.Filled.SwapVert, "Return trip", "Reverses the order of start, stops, and destination. Tap \"Calculate route\" afterwards to compute it."),
                HelpItem(null, "Show route on map", "Displays the computed route with its waypoints and your own, continuously updated position right inside the app."),
                HelpItem(null, "Navigate directly in OsmAnd", "Starts turn-by-turn navigation directly in OsmAnd (one-time setup required, see \"About this app\")."),
                HelpItem(null, "Send to map app", "Shares the route as a GPX file to any installed map or navigation app.")
            )
        ),
        HelpSection(
            "Map picker",
            listOf(
                HelpItem(Icons.AutoMirrored.Filled.ArrowBack, "Back", "Returns to the route screen without changing the waypoint."),
                HelpItem(Icons.Filled.MyLocation, "Center on my location", "Centers the map on your current GPS position."),
                HelpItem(null, "Tap the map", "Places a marker at the tapped point."),
                HelpItem(Icons.Filled.Check, "Use this point", "Confirms the marked point and fills it in as the waypoint's address (via reverse geocoding).")
            )
        ),
        HelpSection(
            "Route map",
            listOf(
                HelpItem(Icons.Filled.MyLocation, "Center on my location", "Jumps to your current, continuously updated position."),
                HelpItem(null, "Red line", "The computed route."),
                HelpItem(null, "Markers", "Your start, stop(s), and destination.")
            )
        ),
        HelpSection(
            "Route preferences screen",
            listOf(
                HelpItem(null, "Profile dropdown", "Switches between your saved preference profiles. The selected profile is remembered and stays active after restarting the app."),
                HelpItem(Icons.Filled.Delete, "Delete profile", "Deletes the currently selected profile (at least one profile always remains)."),
                HelpItem(null, "Rename / Save as new", "Renames the active profile, or saves the current settings as a new named profile."),
                HelpItem(null, "Category / surface / green routes / traveling with", "bbbike's own route-preference options, shown with their original bbbike labels."),
                HelpItem(null, "Speed slider", "Average speed used by bbbike to estimate travel time."),
                HelpItem(null, "Switches", "Allow ferries, avoid unlit ways, avoid traffic lights, include unknown streets.")
            )
        ),
        HelpSection(
            "Settings screen",
            listOf(
                HelpItem(null, "City / region field", "The bbbike instance to route on, with autocomplete; \"Refresh city list\" reloads it from bbbike.org."),
                HelpItem(Icons.Filled.Visibility, "Show/hide appid", "Toggles whether your bbbike appid is shown in plain text or masked."),
                HelpItem(null, "Routing language / UI language / color theme", "Language bbbike replies in, this app's own display language, and the app theme: System (follows the phone's dark mode and wallpaper colors), Light, Dark, or Colorful. The switch applies immediately to everything, including buttons, switches, menus, and the status bar."),
                HelpItem(Icons.Filled.Info, "About this app", "Version info, the OsmAnd setup guide, and all data sources this app uses.")
            )
        )
    )
} else {
    listOf(
        HelpSection(
            "Hauptbildschirm",
            listOf(
                HelpItem(null, "Stadt-Zeile", "Zeigt die aktuell gewählte bbbike-Stadt/Region unter dem Titel. Antippen springt direkt zu den Einstellungen, um sie zu ändern."),
                HelpItem(Icons.Filled.Tune, "Wegeeinstellungen", "Öffnet die bbbike-Wegeeinstellungen (Kategorie, Belag, Grüne Wege, Geschwindigkeit, Ampeln, ...)."),
                HelpItem(Icons.Filled.Settings, "Einstellungen", "Öffnet die App-Einstellungen (Stadt/Region, bbbike-appid, UI-Sprache, Routensprache, Farbschema).")
            )
        ),
        HelpSection(
            "Wegpunkt-Zeile (Start / Zwischenstopp / Ziel)",
            listOf(
                HelpItem(Icons.Filled.Search, "Suchen", "Sucht die eingegebene Adresse über OpenStreetMap Nominatim."),
                HelpItem(Icons.Filled.LocationOn, "Aktueller Standort", "Nur beim Start: verwendet die aktuelle GPS-Position."),
                HelpItem(Icons.Filled.Map, "Auf Karte auswählen", "Öffnet die Kartenauswahl; ein Tipp auf die Karte setzt diesen Wegpunkt auf den gewählten Punkt."),
                HelpItem(Icons.Filled.Contacts, "Aus Kontakten", "Übernimmt die gespeicherte Adresse eines Kontakts."),
                HelpItem(Icons.Filled.Bookmarks, "Favoriten", "Zeigt gespeicherte Favoritenadressen zur Auswahl."),
                HelpItem(Icons.Filled.BookmarkAdd, "Als Favorit speichern", "Erscheint, sobald eine Adresse aufgelöst ist; speichert sie als neuen Favoriten."),
                HelpItem(Icons.Filled.BookmarkRemove, "Favorit löschen", "In der Favoritenliste: entfernt diesen Favoriten dauerhaft."),
                HelpItem(Icons.Filled.Close, "Wegpunkt entfernen", "Entfernt diesen Zwischenstopp (nur sichtbar, wenn mehr als Start und Ziel vorhanden sind)."),
                HelpItem(Icons.Filled.Add, "Zwischenstopp hinzufügen", "Fügt einen weiteren Wegpunkt zur Route hinzu.")
            )
        ),
        HelpSection(
            "Route berechnen und nutzen",
            listOf(
                HelpItem(null, "Route berechnen", "Sendet alle aufgelösten Wegpunkte an bbbike und berechnet die Fahrradroute."),
                HelpItem(null, "Ereignisse auf der Route", "Wird aktiv, sobald bbbike Sperrungen, Baustellen oder Märkte auf der Route meldet. Das Fenster listet sie auf; angehakte Ereignisse werden mit \"Ausweichroute berechnen\" umfahren. Nur für die Berliner Instanz (bbbike) verfügbar."),
                HelpItem(null, "Routen-Statistik","Nach der Berechnung: Länge, Anzahl Ampeln und die Fahrzeit laut bbbike bei mehreren Geschwindigkeiten (die eingestellte fett)."),
                HelpItem(Icons.Filled.SwapVert, "Rückweg", "Dreht die Reihenfolge von Start, Zwischenstopps und Ziel um. Anschließend \"Route berechnen\" antippen, um sie zu berechnen."),
                HelpItem(null, "Route auf Karte anzeigen", "Zeigt die berechnete Route samt Wegpunkten und dem eigenen, laufend aktualisierten Standort direkt in der App an."),
                HelpItem(null, "Direkt in OsmAnd navigieren", "Startet die sprachgeführte Navigation direkt in OsmAnd (einmalige Einrichtung nötig, siehe \"Über diese App\")."),
                HelpItem(null, "An Karten-App senden", "Sendet die Route als GPX-Datei an eine beliebige installierte Karten-/Navigations-App.")
            )
        ),
        HelpSection(
            "Kartenauswahl",
            listOf(
                HelpItem(Icons.AutoMirrored.Filled.ArrowBack, "Zurück", "Kehrt zum Hauptbildschirm zurück, ohne den Wegpunkt zu ändern."),
                HelpItem(Icons.Filled.MyLocation, "Auf Standort zentrieren", "Zentriert die Karte auf die aktuelle GPS-Position."),
                HelpItem(null, "Karte antippen", "Setzt einen Marker an der angetippten Stelle."),
                HelpItem(Icons.Filled.Check, "Übernehmen", "Übernimmt den markierten Punkt und trägt ihn (per Rückwärts-Geokodierung) als Adresse des Wegpunkts ein.")
            )
        ),
        HelpSection(
            "Routenkarte",
            listOf(
                HelpItem(Icons.Filled.MyLocation, "Auf eigenen Standort zentrieren", "Springt zur aktuellen, laufend aktualisierten eigenen Position."),
                HelpItem(null, "Rote Linie", "Die berechnete Route."),
                HelpItem(null, "Marker", "Start, Zwischenstopp(s) und Ziel.")
            )
        ),
        HelpSection(
            "Wegeeinstellungen-Bildschirm",
            listOf(
                HelpItem(null, "Profil-Dropdown", "Wechselt zwischen den gespeicherten Wegeeinstellungs-Profilen. Das gewählte Profil wird gespeichert und bleibt auch nach einem Neustart der App aktiv."),
                HelpItem(Icons.Filled.Delete, "Profil löschen", "Löscht das aktuell gewählte Profil (mindestens ein Profil bleibt immer erhalten)."),
                HelpItem(null, "Umbenennen / Speichern als neu", "Benennt das aktive Profil um, oder speichert die aktuellen Einstellungen als neues, benanntes Profil."),
                HelpItem(null, "Kategorie / Belag / Grüne Wege / Unterwegs mit", "bbbikes eigene Wegeeinstellungen, mit den Original-Bezeichnungen von bbbike.de angezeigt."),
                HelpItem(null, "Geschwindigkeits-Regler", "Durchschnittsgeschwindigkeit, mit der bbbike die Fahrzeit schätzt."),
                HelpItem(null, "Schalter", "Fähren erlauben, unbeleuchtete Wege meiden, Ampeln vermeiden, unbekannte Straßen einbeziehen.")
            )
        ),
        HelpSection(
            "Einstellungen-Bildschirm",
            listOf(
                HelpItem(null, "Stadt/Region-Feld", "Die bbbike-Instanz, auf der geroutet wird, mit Autovervollständigung; \"Städteliste aktualisieren\" lädt sie neu von bbbike.org."),
                HelpItem(Icons.Filled.Visibility, "appid ein-/ausblenden", "Schaltet um, ob die bbbike-appid im Klartext oder maskiert angezeigt wird."),
                HelpItem(null, "Routensprache / UI-Sprache / Farbschema", "Sprache der bbbike-Antworten, Anzeigesprache der App selbst, sowie das App-Design: System (folgt Dunkelmodus und Hintergrundbild-Farben des Handys), Hell, Dunkel oder Farbig. Die Umstellung wirkt sofort auf alles, auch auf Knöpfe, Schalter, Menüs und die Statusleiste."),
                HelpItem(Icons.Filled.Info, "Über diese App", "Versionsinfo, die OsmAnd-Einrichtungsanleitung und alle von dieser App genutzten Datenquellen.")
            )
        )
    )
}
