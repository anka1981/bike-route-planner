package io.github.anka1981.bikerouteplanner.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val ROUTE = "route"
    const val PREFERENCES = "preferences"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val HELP = "help"
    const val ROUTE_MAP = "routeMap"
    const val MAP_PICKER = "mapPicker/{waypointId}?lat={lat}&lon={lon}"

    fun mapPicker(waypointId: String, lat: Double?, lon: Double?): String {
        val encodedId = URLEncoder.encode(waypointId, "UTF-8")
        return "mapPicker/$encodedId?lat=${lat ?: ""}&lon=${lon ?: ""}"
    }
}

@Composable
fun AppNavHost(viewModel: RouteViewModel) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.ROUTE) {
        composable(Routes.ROUTE) {
            RouteScreen(
                viewModel = viewModel,
                onOpenPreferences = { navController.navigate(Routes.PREFERENCES) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onPickOnMap = { waypointId, lat, lon ->
                    navController.navigate(Routes.mapPicker(waypointId, lat, lon))
                },
                onShowRouteMap = { navController.navigate(Routes.ROUTE_MAP) }
            )
        }
        composable(Routes.ROUTE_MAP) {
            RouteMapScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.MAP_PICKER,
            arguments = listOf(
                navArgument("waypointId") { type = NavType.StringType },
                navArgument("lat") { type = NavType.StringType; defaultValue = "" },
                navArgument("lon") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val waypointId = URLDecoder.decode(
                backStackEntry.arguments?.getString("waypointId").orEmpty(),
                "UTF-8"
            )
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()
            MapPickerScreen(
                viewModel = viewModel,
                initialLat = lat,
                initialLon = lon,
                onConfirm = { pickedLat, pickedLon ->
                    viewModel.useMapPickedLocation(waypointId, pickedLat, pickedLon)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PREFERENCES) {
            PreferencesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
                onOpenHelp = { navController.navigate(Routes.HELP) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.HELP) {
            HelpScreen(onBack = { navController.popBackStack() })
        }
    }
}
