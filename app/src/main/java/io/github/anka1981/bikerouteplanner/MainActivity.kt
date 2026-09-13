package io.github.anka1981.bikerouteplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.anka1981.bikerouteplanner.ui.AppNavHost
import io.github.anka1981.bikerouteplanner.ui.RouteViewModel
import io.github.anka1981.bikerouteplanner.ui.i18n.LocalAppStrings
import io.github.anka1981.bikerouteplanner.ui.i18n.stringsFor
import io.github.anka1981.bikerouteplanner.ui.theme.AppColorTheme
import io.github.anka1981.bikerouteplanner.ui.theme.BikeRoutePlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        // Aussagekraeftiger, eindeutiger User-Agent statt der reinen Package-ID - gefordert von
        // https://wiki.openstreetmap.org/wiki/Blocked_tiles ("proper, unique User-Agent").
        Configuration.getInstance().userAgentValue = "BikeRoutePlanner-AndroidApp/1.0 (privater Gebrauch; $packageName)"
        Configuration.getInstance().osmdroidBasePath = getExternalFilesDir(null)
        // Kachel-Cache bewusst mehrere Wochen halten, damit bereits angesehene Kartenausschnitte
        // nicht wiederholt neu heruntergeladen werden (Rate-Limiting-Vorgabe derselben Richtlinie).
        Configuration.getInstance().expirationExtendedDuration = TimeUnit.DAYS.toMillis(30)
        enableEdgeToEdge()
        setContent {
            BikeRoutePlannerApp()
        }
    }
}

@Composable
fun BikeRoutePlannerApp() {
    val viewModel: RouteViewModel = viewModel()
    val settings by viewModel.settings.collectAsState()
    val settingsLoaded by viewModel.settingsLoaded.collectAsState()
    // Erst zeichnen, wenn die gespeicherten Einstellungen gelesen sind (wenige Millisekunden),
    // sonst blitzt beim Start kurz das System-Farbschema auf, bevor das gewaehlte greift.
    if (!settingsLoaded) return
    BikeRoutePlannerTheme(colorTheme = AppColorTheme.fromCode(settings.colorTheme)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalAppStrings provides stringsFor(settings.uiLanguage)) {
                AppNavHost(viewModel = viewModel)
            }
        }
    }
}
