# BikeRoutePlanner

Android-App, die Fahrradrouten mit der Routensuche von [bbbike](https://www.bbbike.org/)
berechnet, sie als GPX-Track speichert und an eine Karten- oder Navigations-App weitergibt.

## Funktionen

- **Routen über beliebig viele Wegpunkte.** Start, Zwischenstopps und Ziel per Adresssuche,
  Karte, aktuellem Standort, Kontakten oder gespeicherten Favoriten. Ein Knopf dreht die
  Route für den Rückweg um.
- **Alle Wegeeinstellungen von bbbike** (Kategorie, Belag, grüne Wege, Geschwindigkeit,
  Ampeln, Fähren, unbeleuchtete Wege, Anhänger oder Kindersitz), speicherbar als benannte
  Profile.
- **Routen-Statistik:** Länge, Anzahl Ampeln und die Fahrzeit laut bbbike bei mehreren
  Geschwindigkeiten.
- **Ereignisse auf der Route:** Sperrungen, Baustellen und Märkte, die die Route berühren,
  mit der Möglichkeit, eine Ausweichroute darum herum berechnen zu lassen (nur Berlin,
  siehe unten).
- **Karte in der App** mit der berechneten Route und der eigenen, laufend aktualisierten
  Position; Navigation direkt in OsmAnd oder Weitergabe der GPX-Datei an jede Karten-App.
- Oberfläche auf Deutsch und Englisch, Farbschemata Hell, Dunkel, Farbig und System.

## Datenquellen

| Zweck | Dienst |
|---|---|
| Routenberechnung | `api.bbbike.org` (offizielle bbbike-API, verschlüsselt) |
| Ereignisse und Ausweichrouten | `www.bbbike.de` (nur Berlin) |
| Adresssuche | OpenStreetMap Nominatim |
| Kartenkacheln | OpenTopoMap, OpenStreetMap |

Die Ereignisse liefert ausschließlich `www.bbbike.de`; in der offiziellen API ist die Liste
der Sperrungen immer leer. Diese Seite ist nur über unverschlüsseltes http erreichbar,
deshalb erlaubt `app/src/main/res/xml/network_security_config.xml` Klartextverkehr gezielt
für diese eine Domain. Alles andere bleibt auf verschlüsselte Verbindungen beschränkt.

Abschalten lässt sich das in den App-Einstellungen unter „Ereignisse auf der Route laden“.
Dann nimmt die App überhaupt keine Verbindung zu `www.bbbike.de` auf, und der Ereignis-Knopf
bleibt leer.

## Bauen

Android Studio oder Gradle, JDK 17+ und Android SDK (compileSdk 37, minSdk 26):

```
./gradlew :app:assembleProdRelease
```

Es gibt die Produktvarianten `prod` und `dev` (getrennt installierbar, Suffix `.dev`).

## Drittanbieter-Code

Das Modul `osmand-api/` ist eine unveränderte Kopie des AIDL-Moduls aus
[OsmAnd](https://github.com/osmandapp/OsmAnd) und steht unter dessen Lizenz (GPLv3);
Einzelheiten in `osmand-api/README.md`.

## Lizenz

[GPLv3](LICENSE). Diese Lizenz ist durch das mitgelieferte OsmAnd-Modul vorgegeben, das
selbst unter der GPLv3 steht.
