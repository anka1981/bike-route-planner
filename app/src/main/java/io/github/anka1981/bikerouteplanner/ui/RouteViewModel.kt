package io.github.anka1981.bikerouteplanner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.anka1981.bikerouteplanner.data.AppSettings
import io.github.anka1981.bikerouteplanner.data.CityCatalogRepository
import io.github.anka1981.bikerouteplanner.data.CityOption
import io.github.anka1981.bikerouteplanner.data.FavoriteAddress
import io.github.anka1981.bikerouteplanner.data.GeocodingRepository
import io.github.anka1981.bikerouteplanner.data.defaultBbbikeCities
import io.github.anka1981.bikerouteplanner.data.RouteProfile
import io.github.anka1981.bikerouteplanner.data.RoutePreferences
import io.github.anka1981.bikerouteplanner.data.RouteRepository
import io.github.anka1981.bikerouteplanner.data.RouteState
import io.github.anka1981.bikerouteplanner.data.SettingsRepository
import io.github.anka1981.bikerouteplanner.data.Waypoint
import io.github.anka1981.bikerouteplanner.data.WaypointInput
import io.github.anka1981.bikerouteplanner.data.defaultRouteProfile
import io.github.anka1981.bikerouteplanner.data.toWaypoint
import io.github.anka1981.bikerouteplanner.ui.i18n.AppStrings
import io.github.anka1981.bikerouteplanner.ui.i18n.GermanStrings
import io.github.anka1981.bikerouteplanner.ui.i18n.stringsFor
import io.github.anka1981.bikerouteplanner.util.getLastKnownLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val geocodingRepository = GeocodingRepository()
    private val routeRepository = RouteRepository(application)
    private val cityCatalogRepository = CityCatalogRepository(application)

    private val _waypoints = MutableStateFlow(
        listOf(WaypointInput(query = ""), WaypointInput(query = ""))
    )
    val waypoints: StateFlow<List<WaypointInput>> = _waypoints.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /** true, sobald die gespeicherten Einstellungen (u.a. das Farbschema) gelesen wurden. */
    private val _settingsLoaded = MutableStateFlow(false)
    val settingsLoaded: StateFlow<Boolean> = _settingsLoaded.asStateFlow()

    private val _profiles = MutableStateFlow(listOf(defaultRouteProfile))
    val profiles: StateFlow<List<RouteProfile>> = _profiles.asStateFlow()

    private val _activeProfileId = MutableStateFlow(defaultRouteProfile.id)
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    /** Wegeeinstellungen des aktuell aktiven Profils. */
    private val _preferences = MutableStateFlow(RoutePreferences())
    val preferences: StateFlow<RoutePreferences> = _preferences.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteAddress>>(emptyList())
    val favorites: StateFlow<List<FavoriteAddress>> = _favorites.asStateFlow()

    private val _routeState = MutableStateFlow<RouteState>(RouteState.Idle)
    val routeState: StateFlow<RouteState> = _routeState.asStateFlow()

    private val _cities = MutableStateFlow(defaultBbbikeCities)
    val cities: StateFlow<List<CityOption>> = _cities.asStateFlow()

    private val _cityRefreshStatus = MutableStateFlow<String?>(null)
    val cityRefreshStatus: StateFlow<String?> = _cityRefreshStatus.asStateFlow()

    private var currentStrings: AppStrings = GermanStrings

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect {
                _settings.value = it
                currentStrings = stringsFor(it.uiLanguage)
                _settingsLoaded.value = true
            }
        }
        viewModelScope.launch {
            settingsRepository.profileSelectionFlow.collect { (profiles, activeId) ->
                _profiles.value = profiles
                _activeProfileId.value = activeId
                recomputeActivePreferences()
            }
        }
        viewModelScope.launch {
            settingsRepository.favoritesFlow.collect { _favorites.value = it }
        }
        viewModelScope.launch {
            cityCatalogRepository.citiesFlow.collect { _cities.value = it }
        }
    }

    /** Ermittelt Koordinaten fuer die Mitte der aktuell in den Einstellungen gewaehlten Stadt,
     * damit die Kartenauswahl dort statt an einem beliebigen Punkt in Deutschland startet. */
    suspend fun geocodeCityCenter(): Waypoint? {
        val citySlug = _settings.value.citySlug
        val cityLabel = _cities.value.find { it.slug == citySlug }?.label ?: citySlug
        if (cityLabel.isBlank()) return null
        return try {
            geocodingRepository.search(cityLabel).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    fun refreshCities() {
        viewModelScope.launch {
            _cityRefreshStatus.value = currentStrings.loadingCities
            val result = cityCatalogRepository.refreshFromBbbike()
            _cityRefreshStatus.value = result.fold(
                onSuccess = { count -> currentStrings.citiesLoaded(count) },
                onFailure = { e -> currentStrings.cityUpdateFailed(e.message ?: currentStrings.unknownError) }
            )
        }
    }

    private fun recomputeActivePreferences() {
        val match = _profiles.value.find { it.id == _activeProfileId.value }
        if (match != null) {
            _preferences.value = match.preferences
            return
        }
        // Die aktive Profil-ID zeigt auf kein vorhandenes Profil mehr (z.B. nach einem
        // inkompatiblen Datenformat-Wechsel) - auf das erste vorhandene Profil zurueckfallen
        // und das auch dauerhaft so speichern, damit die Anzeige nicht auf einer verwaisten ID stehen bleibt.
        val fallback = _profiles.value.firstOrNull() ?: defaultRouteProfile
        _preferences.value = fallback.preferences
        if (_activeProfileId.value != fallback.id) {
            _activeProfileId.value = fallback.id
            viewModelScope.launch { settingsRepository.setActiveProfileId(fallback.id) }
        }
    }

    fun addWaypoint() {
        _waypoints.value = _waypoints.value + WaypointInput(query = "")
    }

    /** Dreht Start, Zwischenstopps und Ziel um (Rueckweg). Berechnet die Route nicht automatisch. */
    fun reverseWaypoints() {
        _waypoints.value = _waypoints.value.reversed()
        resetRouteState()
    }

    fun removeWaypoint(id: String) {
        if (_waypoints.value.size <= 2) return
        _waypoints.value = _waypoints.value.filterNot { it.id == id }
    }

    fun updateQuery(id: String, text: String) {
        _waypoints.value = _waypoints.value.map {
            if (it.id == id) it.copy(query = text, resolved = null, candidates = emptyList(), error = null) else it
        }
    }

    fun searchWaypoint(id: String) {
        val input = _waypoints.value.find { it.id == id } ?: return
        if (input.query.isBlank()) return
        setWaypoint(id) { it.copy(isSearching = true, error = null) }
        viewModelScope.launch {
            try {
                val results = geocodingRepository.search(input.query)
                setWaypoint(id) {
                    if (results.isEmpty()) {
                        it.copy(isSearching = false, error = currentStrings.noResultsFound)
                    } else if (results.size == 1) {
                        it.copy(isSearching = false, resolved = results.first(), candidates = emptyList())
                    } else {
                        it.copy(isSearching = false, candidates = results)
                    }
                }
            } catch (e: Exception) {
                setWaypoint(id) {
                    it.copy(isSearching = false, error = currentStrings.searchFailed(e.message ?: currentStrings.unknownError))
                }
            }
        }
    }

    /** Traegt einen extern beschafften Adress-Text (z.B. aus einem Kontakt) ein und sucht ihn sofort. */
    fun searchWaypointWithText(id: String, text: String) {
        updateQuery(id, text)
        searchWaypoint(id)
    }

    fun reportWaypointError(id: String, message: String) {
        setWaypoint(id) { it.copy(error = message) }
    }

    fun selectCandidate(id: String, waypoint: Waypoint) {
        setWaypoint(id) {
            it.copy(resolved = waypoint, candidates = emptyList(), query = waypoint.label)
        }
    }

    fun useCurrentLocation(id: String) {
        val location = getLastKnownLocation(getApplication()) ?: run {
            setWaypoint(id) { it.copy(error = currentStrings.currentLocationUnavailable) }
            return
        }
        val waypoint = Waypoint(currentStrings.currentLocationLabel, location.latitude, location.longitude)
        setWaypoint(id) {
            it.copy(query = waypoint.label, resolved = waypoint, candidates = emptyList(), error = null)
        }
    }

    fun useMapPickedLocation(id: String, lat: Double, lon: Double) {
        val placeholderLabel = currentStrings.mapPickedLocationLabel(lat, lon)
        setWaypoint(id) {
            it.copy(
                query = placeholderLabel,
                resolved = Waypoint(placeholderLabel, lat, lon),
                candidates = emptyList(),
                error = null,
                isSearching = true
            )
        }
        viewModelScope.launch {
            val waypoint = try {
                geocodingRepository.reverse(lat, lon)
            } catch (e: Exception) {
                null
            }
            if (waypoint != null) {
                setWaypoint(id) { it.copy(query = waypoint.label, resolved = waypoint, isSearching = false) }
            } else {
                setWaypoint(id) { it.copy(isSearching = false) }
            }
        }
    }

    fun useFavorite(id: String, favorite: FavoriteAddress) {
        setWaypoint(id) {
            it.copy(query = favorite.label, resolved = favorite.toWaypoint(), candidates = emptyList(), error = null)
        }
    }

    fun addFavorite(waypoint: Waypoint) {
        val updated = _favorites.value + FavoriteAddress(label = waypoint.label, lat = waypoint.lat, lon = waypoint.lon)
        _favorites.value = updated
        viewModelScope.launch { settingsRepository.saveFavorites(updated) }
    }

    fun removeFavorite(id: String) {
        val updated = _favorites.value.filterNot { it.id == id }
        _favorites.value = updated
        viewModelScope.launch { settingsRepository.saveFavorites(updated) }
    }

    private fun setWaypoint(id: String, transform: (WaypointInput) -> WaypointInput) {
        _waypoints.value = _waypoints.value.map { if (it.id == id) transform(it) else it }
    }

    /** Aendert die Wegeeinstellungen des aktuell aktiven Profils. */
    fun updatePreferences(newPreferences: RoutePreferences) {
        val updatedProfiles = _profiles.value.map {
            if (it.id == _activeProfileId.value) it.copy(preferences = newPreferences) else it
        }
        _profiles.value = updatedProfiles
        _preferences.value = newPreferences
        viewModelScope.launch { settingsRepository.saveProfiles(updatedProfiles) }
    }

    fun selectProfile(id: String) {
        _activeProfileId.value = id
        recomputeActivePreferences()
        viewModelScope.launch { settingsRepository.setActiveProfileId(id) }
    }

    fun saveCurrentAsNewProfile(name: String) {
        val newProfile = RouteProfile(name = name, preferences = _preferences.value)
        val updated = _profiles.value + newProfile
        _profiles.value = updated
        _activeProfileId.value = newProfile.id
        viewModelScope.launch { settingsRepository.saveProfiles(updated, activeProfileId = newProfile.id) }
    }

    fun renameActiveProfile(newName: String) {
        val updated = _profiles.value.map {
            if (it.id == _activeProfileId.value) it.copy(name = newName) else it
        }
        _profiles.value = updated
        viewModelScope.launch { settingsRepository.saveProfiles(updated) }
    }

    fun deleteActiveProfile() {
        if (_profiles.value.size <= 1) return
        val updated = _profiles.value.filterNot { it.id == _activeProfileId.value }
        _profiles.value = updated
        val newActiveId = updated.first().id
        _activeProfileId.value = newActiveId
        recomputeActivePreferences()
        viewModelScope.launch { settingsRepository.saveProfiles(updated, activeProfileId = newActiveId) }
    }

    fun updateSettings(settings: AppSettings) {
        _settings.value = settings
        viewModelScope.launch { settingsRepository.updateSettings(settings) }
    }

    /** Berechnet die Route; mit [avoidEventIds] als Ausweichroute um diese Ereignisse herum. */
    fun computeRoute(avoidEventIds: List<String> = emptyList()) {
        val resolved = _waypoints.value.mapNotNull { it.resolved }
        if (resolved.size < 2 || resolved.size != _waypoints.value.size) {
            _routeState.value = RouteState.Error(currentStrings.selectAllWaypointsError)
            return
        }
        _routeState.value = RouteState.Loading
        viewModelScope.launch {
            try {
                val route = routeRepository.computeRoute(
                    resolved,
                    _preferences.value,
                    _settings.value,
                    currentStrings,
                    avoidEventIds
                )
                _routeState.value = RouteState.Success(
                    gpxFilePath = route.gpxFile.absolutePath,
                    legCount = resolved.size - 1,
                    pointCount = route.pointCount,
                    stats = route.stats,
                    events = route.events,
                    avoidedEventCount = route.avoidedEventIds.size
                )
            } catch (e: Exception) {
                _routeState.value = RouteState.Error(e.message ?: currentStrings.unknownError)
            }
        }
    }

    fun resetRouteState() {
        _routeState.value = RouteState.Idle
    }
}
