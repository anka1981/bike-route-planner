# osmand-api (vendored)

Unveränderte Kopie von [`OsmAnd-api`](https://github.com/osmandapp/OsmAnd/tree/master/OsmAnd-api)
aus dem OsmAnd-Repository, Commit `462978c80d6f450f1ffa8e56667729f6b915ba34` (2026-09-08).

Definiert die AIDL-Schnittstelle `net.osmand.aidlapi.*`, über die OsmAnd von
Drittanbieter-Apps ferngesteuert werden kann (u.a. `navigateGpx`, siehe
[OsmAnd API Doku](https://osmand.net/docs/technical/osmand-api-sdk/)). Es handelt sich
um Osmands *interne* Implementierung, keine stabile öffentliche Schnittstelle mit
Versionsgarantie – bei Kompatibilitätsproblemen mit neueren OsmAnd-Versionen hier
gegen einen aktuelleren Commit-Stand aus dem Original-Repo aktualisieren.

Enthält nur das `src/`-Verzeichnis des Original-Moduls; Build-Konfiguration
(`build.gradle.kts`, `AndroidManifest.xml`) wurde für dieses Projekt neu geschrieben,
da das Original auf Osmands eigene Multi-Modul-Gradle-Konfiguration verweist.
