// Vendorte Kopie von https://github.com/osmandapp/OsmAnd/tree/master/OsmAnd-api
// (Stand: siehe Commit-Referenz in README dieses Ordners). Definiert die AIDL-Schnittstelle
// net.osmand.aidlapi.*, ueber die OsmAnd von Drittanbieter-Apps gesteuert werden kann
// (siehe https://osmand.net/docs/technical/osmand-api-sdk/). Bewusst als eigenes Modul
// gehalten, um vendorten Code sauber vom eigenen App-Code zu trennen.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "net.osmand.aidlapi"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            aidl.srcDirs("src")
            java.srcDirs("src")
        }
    }

    buildFeatures {
        aidl = true
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.annotation)
}
