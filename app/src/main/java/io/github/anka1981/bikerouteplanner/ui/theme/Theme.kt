package io.github.anka1981.bikerouteplanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppColorTheme(val code: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    COLORFUL("colorful");

    companion object {
        fun fromCode(code: String): AppColorTheme = entries.find { it.code == code } ?: SYSTEM
    }
}

// Alle Farbrollen sind explizit gesetzt: Nicht gesetzte Rollen (Container, Surface-Container,
// Outline, ...) fallen sonst auf die lila Material-Standardpalette zurueck - betroffen waeren
// z.B. FloatingActionButton, Slider-Spur, Switches, Dropdown-Menues und Karten.

private val DarkColorScheme = darkColorScheme(
    primary = BikeGreen80,
    onPrimary = Color(0xFF0A3910),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8E6C9),
    inversePrimary = BikeGreen40,
    secondary = BikeGreenGrey80,
    onSecondary = Color(0xFF213524),
    secondaryContainer = Color(0xFF384B39),
    onSecondaryContainer = Color(0xFFD4E8D2),
    tertiary = BikeAccent80,
    onTertiary = Color(0xFF003732),
    tertiaryContainer = Color(0xFF00504A),
    onTertiaryContainer = Color(0xFFCCF0EB),
    background = Color(0xFF101510),
    onBackground = Color(0xFFE0E4DB),
    surface = Color(0xFF101510),
    onSurface = Color(0xFFE0E4DB),
    surfaceVariant = Color(0xFF424940),
    onSurfaceVariant = Color(0xFFC2C9BD),
    surfaceTint = BikeGreen80,
    inverseSurface = Color(0xFFE0E4DB),
    inverseOnSurface = Color(0xFF2D322C),
    outline = Color(0xFF8C9388),
    outlineVariant = Color(0xFF424940),
    surfaceBright = Color(0xFF363A34),
    surfaceDim = Color(0xFF101510),
    surfaceContainerLowest = Color(0xFF0B0F0A),
    surfaceContainerLow = Color(0xFF181D17),
    surfaceContainer = Color(0xFF1C211B),
    surfaceContainerHigh = Color(0xFF272B25),
    surfaceContainerHighest = Color(0xFF323630)
)

private val LightColorScheme = lightColorScheme(
    primary = BikeGreen40,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB7F1B0),
    onPrimaryContainer = Color(0xFF002204),
    inversePrimary = BikeGreen80,
    secondary = BikeGreenGrey40,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4E8C2),
    onSecondaryContainer = Color(0xFF0F2000),
    tertiary = BikeAccent40,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFA7F2E3),
    onTertiaryContainer = Color(0xFF00201B),
    background = Color(0xFFF7FBF1),
    onBackground = Color(0xFF181D17),
    surface = Color(0xFFF7FBF1),
    onSurface = Color(0xFF181D17),
    surfaceVariant = Color(0xFFDEE5D8),
    onSurfaceVariant = Color(0xFF424940),
    surfaceTint = BikeGreen40,
    inverseSurface = Color(0xFF2D322C),
    inverseOnSurface = Color(0xFFEEF2E9),
    outline = Color(0xFF72796F),
    outlineVariant = Color(0xFFC2C9BD),
    surfaceBright = Color(0xFFF7FBF1),
    surfaceDim = Color(0xFFD7DBD2),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5EC),
    surfaceContainer = Color(0xFFEBEFE6),
    surfaceContainerHigh = Color(0xFFE6E9E0),
    surfaceContainerHighest = Color(0xFFE0E4DB)
)

private val ColorfulColorScheme = lightColorScheme(
    primary = ColorfulTeal,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8EBDD),
    onPrimaryContainer = Color(0xFF00201A),
    inversePrimary = Color(0xFF96D3C2),
    secondary = ColorfulGrass,
    onSecondary = Color(0xFF1B2A0A),
    secondaryContainer = Color(0xFFDCEBC3),
    onSecondaryContainer = Color(0xFF1E2C05),
    tertiary = ColorfulSky,
    onTertiary = Color(0xFF0B3246),
    tertiaryContainer = Color(0xFFD4ECF8),
    onTertiaryContainer = Color(0xFF0B3246),
    background = ColorfulBackground,
    onBackground = Color(0xFF1F1B16),
    surface = ColorfulBackground,
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFEFE3D3),
    onSurfaceVariant = Color(0xFF4F4539),
    surfaceTint = ColorfulTeal,
    inverseSurface = Color(0xFF34302A),
    inverseOnSurface = Color(0xFFF8EFE7),
    outline = Color(0xFF81756A),
    outlineVariant = Color(0xFFD3C4B4),
    surfaceBright = ColorfulBackground,
    surfaceDim = Color(0xFFE2DCD0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F0E4),
    surfaceContainer = Color(0xFFF2EADB),
    surfaceContainerHigh = Color(0xFFECE3D2),
    surfaceContainerHighest = Color(0xFFE6DCC8)
)

@Composable
fun BikeRoutePlannerTheme(
    colorTheme: AppColorTheme = AppColorTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = when (colorTheme) {
        AppColorTheme.COLORFUL -> ColorfulColorScheme
        AppColorTheme.LIGHT -> LightColorScheme
        AppColorTheme.DARK -> DarkColorScheme
        AppColorTheme.SYSTEM -> when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            systemDark -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    // enableEdgeToEdge() richtet die Statusleisten-Symbole nach dem System-Dunkelmodus aus; bei
    // abweichendem App-Design (z.B. "Hell" auf dunklem System) waeren sie sonst unsichtbar.
    val view = LocalView.current
    if (!view.isInEditMode) {
        val lightBars = colorScheme.background.luminance() > 0.5f
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = lightBars
                isAppearanceLightNavigationBars = lightBars
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
