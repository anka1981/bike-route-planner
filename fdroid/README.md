# F-Droid

`io.github.anka1981.bikerouteplanner.yml` ist das Bau-Rezept fuer F-Droid. Es wird nicht
aus diesem Repository gelesen, sondern muss in F-Droids Datensammlung
[fdroiddata](https://gitlab.com/fdroid/fdroiddata) unter
`metadata/io.github.anka1981.bikerouteplanner.yml` liegen. Die Kopie hier dient dazu, den
Stand nachvollziehbar zu halten.

Beschreibungstexte und Bildschirmfotos zieht F-Droid dagegen direkt aus dem Ordner
[`fastlane/`](../fastlane) dieses Repositorys.

## Bei jeder neuen Version

1. `versionCode` und `versionName` in `app/build.gradle.kts` erhoehen.
2. Aenderungen unter `fastlane/metadata/android/<sprache>/changelogs/<versionCode>.txt`
   beschreiben.
3. Commit, dann Tag `v<versionName>` setzen und beides pushen.

Mit `UpdateCheckMode: Tags` findet F-Droid neue Versionen anhand der Tags von selbst; das
Bau-Rezept muss dafuer nicht jedes Mal geaendert werden.
