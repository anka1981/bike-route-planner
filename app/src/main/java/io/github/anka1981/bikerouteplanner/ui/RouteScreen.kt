package io.github.anka1981.bikerouteplanner.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import io.github.anka1981.bikerouteplanner.data.FavoriteAddress
import io.github.anka1981.bikerouteplanner.data.RouteEvent
import io.github.anka1981.bikerouteplanner.data.RouteState
import io.github.anka1981.bikerouteplanner.data.RouteStats
import io.github.anka1981.bikerouteplanner.data.Waypoint
import io.github.anka1981.bikerouteplanner.data.WaypointInput
import io.github.anka1981.bikerouteplanner.ui.i18n.AppStrings
import io.github.anka1981.bikerouteplanner.ui.i18n.LocalAppStrings
import io.github.anka1981.bikerouteplanner.util.OsmAndAidlHelper
import io.github.anka1981.bikerouteplanner.util.readContactAddress
import io.github.anka1981.bikerouteplanner.util.shareGpxFile
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

private enum class WaypointRole { START, VIA, DESTINATION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    viewModel: RouteViewModel,
    onOpenPreferences: () -> Unit,
    onOpenSettings: () -> Unit,
    onPickOnMap: (waypointId: String, lat: Double?, lon: Double?) -> Unit,
    onShowRouteMap: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val waypoints by viewModel.waypoints.collectAsState()
    val routeState by viewModel.routeState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var osmAndStatus by remember { mutableStateOf<String?>(null) }
    var showEventsDialog by remember { mutableStateOf(false) }
    var selectedEventIds by remember { mutableStateOf(emptySet<String>()) }

    var pendingLocationWaypointId by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        val waypointId = pendingLocationWaypointId
        pendingLocationWaypointId = null
        if (granted && waypointId != null) {
            viewModel.useCurrentLocation(waypointId)
        }
    }

    fun requestCurrentLocation(waypointId: String) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            viewModel.useCurrentLocation(waypointId)
        } else {
            pendingLocationWaypointId = waypointId
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    var pendingContactWaypointId by remember { mutableStateOf<String?>(null) }

    val pickContactLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        val waypointId = pendingContactWaypointId
        pendingContactWaypointId = null
        if (uri != null && waypointId != null) {
            val address = readContactAddress(context, uri)
            if (address != null) {
                viewModel.searchWaypointWithText(waypointId, address)
            } else {
                viewModel.reportWaypointError(waypointId, strings.contactAddressNotFound)
            }
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickContactLauncher.launch(null)
        } else {
            pendingContactWaypointId = null
        }
    }

    fun requestContactForWaypoint(waypointId: String) {
        pendingContactWaypointId = waypointId
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            pickContactLauncher.launch(null)
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(strings.appTitle)
                        TextButton(onClick = onOpenSettings, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                            Text(
                                strings.activeCityLabel(settings.citySlug),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenPreferences) {
                        Icon(Icons.Filled.Tune, contentDescription = strings.preferencesIconDesc)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = strings.settingsIconDesc)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(waypoints, key = { it.id }) { input ->
                    val role = waypointRole(waypoints, input)
                    WaypointRow(
                        role = role,
                        label = waypointRoleLabel(strings, role),
                        input = input,
                        canRemove = waypoints.size > 2,
                        favorites = favorites,
                        strings = strings,
                        onQueryChange = { viewModel.updateQuery(input.id, it) },
                        onSearch = { viewModel.searchWaypoint(input.id) },
                        onSelectCandidate = { viewModel.selectCandidate(input.id, it) },
                        onRemove = { viewModel.removeWaypoint(input.id) },
                        onUseCurrentLocation = { requestCurrentLocation(input.id) },
                        onPickOnMap = {
                            onPickOnMap(input.id, input.resolved?.lat, input.resolved?.lon)
                        },
                        onPickContact = { requestContactForWaypoint(input.id) },
                        onUseFavorite = { viewModel.useFavorite(input.id, it) },
                        onSaveFavorite = { input.resolved?.let(viewModel::addFavorite) },
                        onRemoveFavorite = { viewModel.removeFavorite(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            TextButton(onClick = { viewModel.addWaypoint() }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.height(0.dp))
                Text(strings.addWaypoint)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.computeRoute() },
                    modifier = Modifier.weight(1f),
                    enabled = routeState !is RouteState.Loading
                ) {
                    Text(strings.computeRoute)
                }
                OutlinedButton(
                    onClick = { viewModel.reverseWaypoints() },
                    enabled = routeState !is RouteState.Loading
                ) {
                    Icon(Icons.Filled.SwapVert, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.reverseRouteButton)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = routeState) {
                is RouteState.Loading -> Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text(strings.computingRoute)
                }

                is RouteState.Success -> Column {
                    Text(
                        strings.routeSummary(state.legCount, state.pointCount),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RouteStatsCard(state.stats, strings)
                    if (state.avoidedEventCount > 0) {
                        Text(
                            strings.alternativeRouteNote(state.avoidedEventCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            selectedEventIds = state.events.mapNotNull { it.id }.toSet()
                            showEventsDialog = true
                        },
                        enabled = state.events.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.eventsButton(state.events.size))
                    }
                    if (showEventsDialog) {
                        RouteEventsDialog(
                            events = state.events,
                            selectedIds = selectedEventIds,
                            strings = strings,
                            onToggle = { id ->
                                selectedEventIds =
                                    if (id in selectedEventIds) selectedEventIds - id else selectedEventIds + id
                            },
                            onDismiss = { showEventsDialog = false },
                            onComputeAlternative = {
                                showEventsDialog = false
                                viewModel.computeRoute(selectedEventIds.toList())
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onShowRouteMap,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.showRouteOnMap)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            osmAndStatus = strings.startingOsmAndNavigation
                            coroutineScope.launch {
                                val gpxContent = File(state.gpxFilePath).readText()
                                val started = OsmAndAidlHelper.tryNavigateGpx(
                                    context,
                                    gpxContent,
                                    "BikeRoutePlanner-Route"
                                )
                                osmAndStatus = if (started) null else strings.osmAndUnavailable
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.navigateInOsmAnd)
                    }
                    osmAndStatus?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            shareGpxFile(context, File(state.gpxFilePath), strings.sendToMapApp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.sendToMapApp)
                    }
                }

                is RouteState.Error -> Text(
                    state.message,
                    color = MaterialTheme.colorScheme.error
                )

                RouteState.Idle -> {}
            }
        }
    }
}

/**
 * Fenster mit den bbbike-Ereignissen zur Route. Angehakte Ereignisse werden bei
 * "Ausweichroute berechnen" umfahren; Ereignisse ohne Kennung kann bbbike nicht umfahren
 * und sind deshalb nur als Hinweis aufgeführt.
 */
@Composable
private fun RouteEventsDialog(
    events: List<RouteEvent>,
    selectedIds: Set<String>,
    strings: AppStrings,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onComputeAlternative: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.eventsDialogTitle(events.size)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(strings.eventsDialogIntro, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                events.forEach { event ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(
                            checked = event.id != null && event.id in selectedIds,
                            onCheckedChange = { event.id?.let(onToggle) },
                            enabled = event.id != null
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.text, style = MaterialTheme.typography.bodyMedium)
                            if (event.recurring) {
                                Text(
                                    strings.eventRecurring,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onComputeAlternative, enabled = selectedIds.isNotEmpty()) {
                Text(strings.computeAlternativeRoute)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.eventsClose) }
        }
    )
}

/** Kennzahlen der Route laut bbbike: Länge, Ampeln und Fahrzeit je Geschwindigkeitsstufe. */
@Composable
private fun RouteStatsCard(stats: RouteStats, strings: AppStrings) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(strings.routeDistance(stats.lengthMeters / 1000.0), style = MaterialTheme.typography.titleMedium)
            stats.trafficLights?.let {
                Text(strings.trafficLightCount(it), style = MaterialTheme.typography.bodyMedium)
            }
            if (stats.speedTimes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(strings.travelTimeHeading, style = MaterialTheme.typography.labelLarge)
                stats.speedTimes.forEach { speedTime ->
                    val weight = if (speedTime.isPreferred) FontWeight.Bold else FontWeight.Normal
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            strings.speedLabel(speedTime.speedKmh, speedTime.isPreferred),
                            modifier = Modifier.weight(1f),
                            fontWeight = weight
                        )
                        Text(formatDuration(speedTime.hours), fontWeight = weight)
                    }
                }
            }
        }
    }
}

private fun formatDuration(hours: Double): String {
    val totalMinutes = (hours * 60).roundToInt()
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return if (h == 0) "$m min" else "$h h ${m.toString().padStart(2, '0')} min"
}

private fun waypointRole(all: List<WaypointInput>, input: WaypointInput): WaypointRole = when {
    all.first().id == input.id -> WaypointRole.START
    all.last().id == input.id -> WaypointRole.DESTINATION
    else -> WaypointRole.VIA
}

private fun waypointRoleLabel(strings: AppStrings, role: WaypointRole): String = when (role) {
    WaypointRole.START -> strings.waypointStart
    WaypointRole.DESTINATION -> strings.waypointDestination
    WaypointRole.VIA -> strings.waypointVia
}

@Composable
private fun WaypointRow(
    role: WaypointRole,
    label: String,
    input: WaypointInput,
    canRemove: Boolean,
    favorites: List<FavoriteAddress>,
    strings: AppStrings,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectCandidate: (Waypoint) -> Unit,
    onRemove: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onPickOnMap: () -> Unit,
    onPickContact: () -> Unit,
    onUseFavorite: (FavoriteAddress) -> Unit,
    onSaveFavorite: () -> Unit,
    onRemoveFavorite: (String) -> Unit
) {
    var favoritesMenuExpanded by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(
                value = input.query,
                onValueChange = onQueryChange,
                label = { Text(label) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = strings.searchIconDesc)
            }
        }

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            if (role == WaypointRole.START) {
                IconButton(onClick = onUseCurrentLocation) {
                    Icon(Icons.Filled.LocationOn, contentDescription = strings.useCurrentLocationDesc)
                }
            }
            IconButton(onClick = onPickOnMap) {
                Icon(Icons.Filled.Map, contentDescription = strings.pickOnMapDesc)
            }
            IconButton(onClick = onPickContact) {
                Icon(Icons.Filled.Contacts, contentDescription = strings.pickContactDesc)
            }
            Column {
                IconButton(onClick = { favoritesMenuExpanded = true }, enabled = favorites.isNotEmpty()) {
                    Icon(Icons.Filled.Bookmarks, contentDescription = strings.useFavoriteDesc)
                }
                DropdownMenu(
                    expanded = favoritesMenuExpanded,
                    onDismissRequest = { favoritesMenuExpanded = false }
                ) {
                    favorites.forEachIndexed { index, favorite ->
                        if (index > 0) {
                            HorizontalDivider()
                        }
                        DropdownMenuItem(
                            text = { Text(favorite.label) },
                            onClick = {
                                onUseFavorite(favorite)
                                favoritesMenuExpanded = false
                            },
                            trailingIcon = {
                                IconButton(onClick = { onRemoveFavorite(favorite.id) }) {
                                    Icon(Icons.Filled.BookmarkRemove, contentDescription = strings.removeFavoriteDesc)
                                }
                            }
                        )
                    }
                }
            }
            if (input.resolved != null) {
                IconButton(onClick = onSaveFavorite) {
                    Icon(Icons.Filled.BookmarkAdd, contentDescription = strings.saveFavoriteDesc)
                }
            }
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = strings.removeWaypointDesc)
                }
            }
        }

        if (input.isSearching) {
            Text(strings.searching, style = MaterialTheme.typography.bodySmall)
        }
        input.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        input.resolved?.let {
            Text(
                "✓ ${it.label}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (input.candidates.isNotEmpty()) {
            Column {
                input.candidates.forEach { candidate ->
                    TextButton(onClick = { onSelectCandidate(candidate) }) {
                        Text(candidate.label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
