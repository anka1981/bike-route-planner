plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.github.anka1981.bikerouteplanner"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "io.github.anka1981.bikerouteplanner"
        minSdk = 26
        targetSdk = 37
        versionCode = 11
        versionName = "1.10"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Persoenliche App ohne Play-Store-Vertrieb: nutzt bewusst denselben Debug-Key wie
            // die bisher installierten Debug-Builds, damit dieser Release-Build die bestehende
            // Installation direkt ersetzen kann statt wegen unterschiedlicher Signaturen zuerst
            // deinstalliert werden zu muessen.
            signingConfig = signingConfigs.getByName("debug")
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
