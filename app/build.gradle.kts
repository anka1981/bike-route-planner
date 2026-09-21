import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Lokale Signatur-Daten fuer Release-Builds (Keystore und Passwort liegen bewusst ausserhalb des
// Repos). Fehlt die Datei - etwa beim Nachbau durch F-Droid, das unsigniert baut und sein Ergebnis
// mit dem hier signierten APK vergleicht -, entsteht die Release-APK unsigniert.
val releaseSigningProps = Properties().apply {
    val path = System.getenv("BIKEROUTEPLANNER_SIGNING_PROPERTIES")
        ?: "${System.getProperty("user.home")}/.android/bikerouteplanner-release.properties"
    val f = file(path)
    if (f.isFile) f.inputStream().use { load(it) }
}
val hasReleaseSigning = releaseSigningProps.getProperty("storeFile") != null

android {
    namespace = "io.github.anka1981.bikerouteplanner"
    compileSdk {
        version = release(37)
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseSigningProps.getProperty("storeFile"))
                storePassword = releaseSigningProps.getProperty("storePassword")
                keyAlias = releaseSigningProps.getProperty("keyAlias")
                keyPassword = releaseSigningProps.getProperty("keyPassword")
            }
        }
    }

    // Fuer reproduzierbare Builds (F-Droid vergleicht seinen Build mit der hier signierten APK)
    // muss die Build-Tools-Version fest vorgegeben sein.
    buildToolsVersion = "36.0.0"

    // Google-Abhaengigkeitsinfo-Block aus dem APK-Signaturblock heraushalten (nicht reproduzierbar).
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    defaultConfig {
        applicationId = "io.github.anka1981.bikerouteplanner"
        minSdk = 26
        targetSdk = 37
        versionCode = 19
        versionName = "1.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Die VCS-Info-Datei (META-INF/version-control-info.textproto) enthaelt bei einem Build
            // im Git-Checkout Pfad und Revision und wuerde jeden Nachbau bei F-Droid vom lokal
            // gebauten APK unterscheiden.
            vcsInfo {
                include = false
            }
            // Eigener Release-Schluessel statt Debug-Schluessel: F-Droids APK-Pruefung stuft das
            // Debug-Zertifikat ("CN=Android Debug") als kritisch ein.
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = false
            }
        }
    }

    // "dev"-Variante bekommt eine eigene Package-ID und kann so parallel zur normalen
    // ("prod") App auf demselben Geraet installiert bleiben, waehrend riskantere neue
    // Funktionen (z.B. die osmdroid-Kartenauswahl) getestet werden.
    flavorDimensions += "channel"
    productFlavors {
        create("prod") {
            dimension = "channel"
        }
        create("dev") {
            dimension = "channel"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":osmand-api"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.osmdroid.android)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
