package io.github.anka1981.bikerouteplanner.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.github.anka1981.bikerouteplanner.ui.i18n.LocalAppStrings
import io.github.anka1981.bikerouteplanner.util.getLastKnownLocation
import io.github.anka1981.bikerouteplanner.util.hasLocationPermission
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

private const val DEFAULT_ZOOM = 14.0
private val DEFAULT_CENTER = GeoPoint(51.1657, 10.4515) // geografische Mitte Deutschlands

/**
 * OSMs eigener Kachelserver (tile.openstreetmap.org / TileSourceFactory.MAPNIK) blockt gemaess
 * https://wiki.openstreetmap.org/wiki/Blocked_tiles direkte App-Nutzung explizit und empfiehlt
 * stattdessen Drittanbieter. CARTOs Voyager-Kacheln erfordern inzwischen einen API-Key,
 * Wikimedias Kachelserver ist laut eigener Fehlermeldung nur fuer Wikimedia-eigene Seiten
 * freigegeben, und das eigentlich passendere CyclOSM (radfahrerorientierte OSM-Karte) hatte im
 * Test echte 404-Luecken bei Zoomstufe 14 in laendlichen Gebieten (nicht ueberall vorgerendert).
 * OpenTopoMap erlaubt angemessene Nutzung in Hobby-/Kleinprojekten ohne Anmeldung/Key und war im
 * Test durchgehend zuverlaessig - siehe https://opentopomap.org/about#verwendung.
 */
private val openTopoMapTileSource = XYTileSource(
    "OpenTopoMap",
    0, 17, 256, ".png",
    arrayOf(
        "https://a.tile.opentopomap.org/",
        "https://b.tile.opentopomap.org/",
        "https://c.tile.opentopomap.org/"
    ),
    "© OpenStreetMap contributors, SRTM | style © OpenTopoMap (CC-BY-SA)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    viewModel: RouteViewModel,
    initialLat: Double?,
    initialLon: Double?,
    onConfirm: (Double, Double) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var pickedPoint by remember {
        mutableStateOf(
            if (initialLat != null && initialLon != null) GeoPoint(initialLat, initialLon) else null
        )
    }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentLocationMarker by remember { mutableStateOf<Marker?>(null) }
    val onPointPicked = rememberUpdatedState<(GeoPoint) -> Unit> { point ->
        pickedPoint = point
        marker?.position = point
    }

    fun centerOnCurrentLocation() {
        val location = getLastKnownLocation(context) ?: return
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        val map = mapView ?: return
        var locationMarker = currentLocationMarker
        if (locationMarker == null) {
            locationMarker = Marker(map).apply {
                icon = androidx.core.content.ContextCompat.getDrawable(
                    context,
                    android.R.drawable.presence_online
                )
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            map.overlays.add(locationMarker)
            currentLocationMarker = locationMarker
        }
        locationMarker.position = geoPoint
        map.controller.animateTo(geoPoint)
        map.invalidate()
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) {
            centerOnCurrentLocation()
        }
    }

    fun requestCenterOnCurrentLocation() {
        if (hasLocationPermission(context)) {
            centerOnCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Beim Oeffnen ohne bereits vorhandenen Punkt: zuerst aktuellen Standort versuchen (nur wenn
    // Berechtigung schon erteilt ist, kein automatischer Berechtigungsdialog), sonst auf die
    // Mitte der in den Einstellungen gewaehlten Stadt zentrieren - statt eines beliebigen Punkts
    // irgendwo in Deutschland.
    LaunchedEffect(Unit) {
        if (initialLat != null && initialLon != null) return@LaunchedEffect
        val current = if (hasLocationPermission(context)) {
            getLastKnownLocation(context)?.let { GeoPoint(it.latitude, it.longitude) }
        } else {
            null
        }
        val center = current ?: viewModel.geocodeCityCenter()?.let { GeoPoint(it.lat, it.lon) }
        if (center != null && pickedPoint == null) {
            mapView?.controller?.setCenter(center)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.mapPickerTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(onClick = { requestCenterOnCurrentLocation() }) {
                        Icon(Icons.Filled.MyLocation, contentDescription = strings.centerOnCurrentLocationDesc)
                    }
                }
            )
        },
        floatingActionButton = {
            pickedPoint?.let { point ->
                FloatingActionButton(onClick = { onConfirm(point.latitude, point.longitude) }) {
                    Icon(Icons.Filled.Check, contentDescription = strings.mapPickerConfirm)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize().align(Alignment.Center),
                factory = { ctx ->
                    val newMapView = MapView(ctx)
                    mapView = newMapView
                    newMapView.apply {
                        setTileSource(openTopoMapTileSource)
                        setMultiTouchControls(true)
                        controller.setZoom(DEFAULT_ZOOM)
                        controller.setCenter(pickedPoint ?: DEFAULT_CENTER)
                        overlays.add(CopyrightOverlay(ctx).apply {
                            setCopyrightNotice("© OpenStreetMap contributors, SRTM | © OpenTopoMap (CC-BY-SA)")
                        })

                        val newMarker = Marker(this).apply {
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        pickedPoint?.let {
                            newMarker.position = it
                            overlays.add(newMarker)
                        }
                        marker = newMarker

                        val receiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                onPointPicked.value(p)
                                if (newMarker !in overlays) {
                                    overlays.add(newMarker)
                                }
                                invalidate()
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint): Boolean = false
                        }
                        overlays.add(0, MapEventsOverlay(receiver))
                    }
                }
            )
        }
    }
}
