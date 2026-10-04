# Offizielle kotlinx.serialization-Regeln (https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md#android),
# auf das App-Paket zugeschnitten. Die Modellklassen selbst brauchen zwar keine reflection-basierte
# Namensaufloesung (der Compiler-Plugin generiert die Feldnamen bereits zur Kompilierzeit), diese
# Regeln verhindern aber, dass R8 die generierten *$$serializer-Klassen und Companion-Objekte
# komplett entfernt, auf die der Serializer-Lookup trotzdem angewiesen ist.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class io.github.anka1981.bikerouteplanner.**$$serializer { *; }
-keepclassmembers class io.github.anka1981.bikerouteplanner.** {
    *** Companion;
}
-keepclasseswithmembers class io.github.anka1981.bikerouteplanner.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# osmdroid (Kartenanzeige) bringt in seinem .aar keine eigenen proguard/consumer-Regeln mit, und
# dies ist der erste Release-Build, der ueberhaupt durch R8 laeuft. Die Bibliothek selbst nutzt
# zwar keine Reflection (gegengeprueft), aber die Bibliothek vollstaendig von Shrinking/Obfuskierung
# auszunehmen kostet bei ihrer Groesse kaum APK-Platz und schliesst jedes Restrisiko aus.
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**
