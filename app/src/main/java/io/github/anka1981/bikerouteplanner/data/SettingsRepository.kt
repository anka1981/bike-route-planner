package io.github.anka1981.bikerouteplanner.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "bike_route_settings")

private val NON_OVERRIDE_APP_IDS = setOf("guest", DEFAULT_BBBIKE_APP_ID)

private object Keys {
    val APP_ID = stringPreferencesKey("app_id")
    val CITY_SLUG = stringPreferencesKey("city_slug")
    val ROUTING_LANGUAGE = stringPreferencesKey("routing_language")
    val UI_LANGUAGE = stringPreferencesKey("ui_language")
    val COLOR_THEME = stringPreferencesKey("color_theme")
    val LOAD_ROUTE_EVENTS = booleanPreferencesKey("load_route_events")
    val REMOVE_PREVIOUS_OSMAND_TRACK = booleanPreferencesKey("remove_previous_osmand_track")

    val PROFILES = stringPreferencesKey("route_profiles_json")
    val ACTIVE_PROFILE_ID = stringPreferencesKey("active_route_profile_id")

    val FAVORITES = stringPreferencesKey("favorite_addresses_json")
}

class SettingsRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            // Aeltere Versionen legten hier "guest" (damalige Standard-appid) bzw. die eingebaute
            // Standard-appid selbst als scheinbare Nutzereingabe ab. Beides zaehlt als "keine eigene
            // appid", damit die Standard-appid nie im Eingabefeld auftaucht.
            appId = prefs[Keys.APP_ID]?.takeUnless { it in NON_OVERRIDE_APP_IDS } ?: "",
            citySlug = prefs[Keys.CITY_SLUG] ?: "Berlin",
            routingLanguage = prefs[Keys.ROUTING_LANGUAGE] ?: "de",
            uiLanguage = prefs[Keys.UI_LANGUAGE] ?: "de",
            colorTheme = prefs[Keys.COLOR_THEME] ?: "system",
            loadRouteEvents = prefs[Keys.LOAD_ROUTE_EVENTS] ?: true,
            removePreviousOsmAndTrack = prefs[Keys.REMOVE_PREVIOUS_OSMAND_TRACK] ?: false
        )
    }

    /**
     * Profilliste und aktive Profil-ID immer gemeinsam aus demselben DataStore-Stand. Getrennt
     * gelesen konnte die gespeicherte ID vor der gespeicherten Liste ankommen, galt dann als
     * verwaist und wurde auf das Standardprofil zurueckgesetzt.
     */
    val profileSelectionFlow: Flow<Pair<List<RouteProfile>, String>> = context.dataStore.data.map { prefs ->
        val profiles = prefs[Keys.PROFILES]?.let {
            runCatching { json.decodeFromString<List<RouteProfile>>(it) }.getOrNull()
        }?.takeIf { it.isNotEmpty() } ?: listOf(defaultRouteProfile)
        profiles to (prefs[Keys.ACTIVE_PROFILE_ID] ?: defaultRouteProfile.id)
    }

    val favoritesFlow: Flow<List<FavoriteAddress>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAVORITES]?.let {
            runCatching { json.decodeFromString<List<FavoriteAddress>>(it) }.getOrNull()
        } ?: emptyList()
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_ID] = settings.appId
            prefs[Keys.CITY_SLUG] = settings.citySlug
            prefs[Keys.ROUTING_LANGUAGE] = settings.routingLanguage
            prefs[Keys.UI_LANGUAGE] = settings.uiLanguage
            prefs[Keys.COLOR_THEME] = settings.colorTheme
            prefs[Keys.LOAD_ROUTE_EVENTS] = settings.loadRouteEvents
            prefs[Keys.REMOVE_PREVIOUS_OSMAND_TRACK] = settings.removePreviousOsmAndTrack
        }
    }

    /** Speichert die Profile und - falls angegeben - im selben Schreibvorgang die aktive Profil-ID. */
    suspend fun saveProfiles(profiles: List<RouteProfile>, activeProfileId: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PROFILES] = json.encodeToString(profiles)
            if (activeProfileId != null) prefs[Keys.ACTIVE_PROFILE_ID] = activeProfileId
        }
    }

    suspend fun setActiveProfileId(id: String) {
        context.dataStore.edit { prefs -> prefs[Keys.ACTIVE_PROFILE_ID] = id }
    }

    suspend fun saveFavorites(favorites: List<FavoriteAddress>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FAVORITES] = json.encodeToString(favorites)
        }
    }
}
