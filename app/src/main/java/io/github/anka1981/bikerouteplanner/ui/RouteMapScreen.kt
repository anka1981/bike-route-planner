package io.github.anka1981.bikerouteplanner.ui

import android.Manifest
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.github.anka1981.bikerouteplanner.R
import io.github.anka1981.bikerouteplanner.data.GpxMerger
import io.github.anka1981.bikerouteplanner.data.RouteState
import io.github.anka1981.bikerouteplanner.ui.i18n.LocalAppStrings
import io.github.anka1981.bikerouteplanner.util.hasLocationPermission
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File

/**
 * Zeigt die berechnete Route (Linie) plus Wegpunkt-Marker auf einer eigenen Karte in der App an,
 * ohne dass dafuer eine externe Karten-/Navigations-App noetig ist. Der eigene Standort wird,
 * sofern die Berechtigung erteilt ist, laufend aktualisiert eingeblendet, damit man sich waehrend
 * der Fahrt relativ zur geplanten Strecke einordnen kann - eine vollwertige, sprachgefuehrte
 * Turn-by-Turn-Navigation (mit automatischer Neuberechnung bei Abweichung) leistet das bewusst
 * nicht; dafuer bleibt die Direktnavigation in OsmAnd die bessere Wahl.
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
fun RouteMapScreen(
    viewModel: RouteViewModel,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    val routeState by viewModel.routeState.collectAsState()
    val waypointInputs by viewModel.waypoints.collectAsState()
    val gpxFilePath = (routeState as? RouteState.Success)?.gpxFilePath.orEmpty()
    val waypoints = remember(waypointInputs) { waypointInputs.mapNotNull { it.resolved } }

    val routePoints = remember(gpxFilePath) {
        val gpxXml = runCatching { File(gpxFilePath).readText() }.getOrNull()
        gpxXml?.let { xml ->
            GpxMerger.extractTrackPoints(xml).mapNotNull { point ->
                val lat = point.lat.toDoubleOrNull()
                val lon = point.lon.toDoubleOrNull()
                if (lat != null && lon != null) GeoPoint(lat, lon) else null
            }
        } ?: emptyList()
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var myLocationMarker by remember { mutableStateOf<Marker?>(null) }

    fun updateMyLocationMarker(location: Location) {
        val map = mapView ?: return
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        var marker = myLocationMarker
        if (marker == null) {
            marker = Marker(map).apply {
                icon = ContextCompat.getDrawable(context, android.R.drawable.presence_online)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            map.overlays.add(marker)
            myLocationMarker = marker
        }
        marker.position = geoPoint
        map.invalidate()
    }

    fun centerOnMyLocation() {
        val geoPoint = myLocationMarker?.position ?: return
        mapView?.controller?.animateTo(geoPoint)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Ergebnis wird ueber den LaunchedEffect-Neustart unten wirksam. */ }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    DisposableEffect(context) {
        val locationManager = context.getSystemService(LocationManager::class.java)
        val listener = LocationListener { location -> updateMyLocationMarker(location) }
        if (hasLocationPermission(context) && locationManager != null) {
            for (provider in locationManager.getProviders(true)) {
                try {
                    locationManager.requestLocationUpdates(provider, 3000L, 5f, listener, Looper.getMainLooper())
                } catch (e: SecurityException) {
                    // Berechtigung inzwischen entzogen - Position bleibt einfach unaktualisiert.
                }
            }
        }
        onDispose {
            locationManager?.removeUpdates(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.routeMapTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(onClick = { centerOnMyLocation() }) {
                        Icon(Icons.Filled.MyLocation, contentDescription = strings.centerOnCurrentLocationDesc)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val newMapView = MapView(ctx)
                    mapView = newMapView
                    newMapView.apply {
                        setTileSource(openTopoMapTileSource)
                        setMultiTouchControls(true)
                        overlays.add(CopyrightOverlay(ctx).apply {
                            setCopyrightNotice("© OpenStreetMap contributors, SRTM | © OpenTopoMap (CC-BY-SA)")
                        })

                        if (routePoints.isNotEmpty()) {
                            val polyline = Polyline(this).apply {
                                setPoints(routePoints)
                                outlinePaint.color = Color.parseColor("#D32F2F")
                                outlinePaint.strokeWidth = 10f
                            }
                            overlays.add(polyline)
                        }

                        waypoints.forEachIndexed { index, waypoint ->
                            val marker = Marker(this).apply {
                                position = GeoPoint(waypoint.lat, waypoint.lon)
                                title = waypoint.label
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                when (index) {
                                    0 -> icon = ContextCompat.getDrawable(ctx, R.drawable.ic_marker_start)
                                    waypoints.lastIndex -> icon = ContextCompat.getDrawable(ctx, R.drawable.ic_marker_finish)
                                }
                            }
                            overlays.add(marker)
                        }

                        if (routePoints.isNotEmpty()) {
                            addOnFirstLayoutListener { _, _, _, _, _ ->
                                val boundingBox = BoundingBox.fromGeoPoints(routePoints)
                                zoomToBoundingBox(boundingBox, false, 64)
                            }
                        } else {
                            val fallbackCenter = waypoints.firstOrNull()
                                ?.let { GeoPoint(it.lat, it.lon) }
                                ?: GeoPoint(51.1657, 10.4515) // geografische Mitte Deutschlands
                            controller.setZoom(14.0)
                            controller.setCenter(fallbackCenter)
                        }
                    }
                },
                // Ohne onDetach() behaelt osmdroid seinen Tile-Cache (Bitmaps) fuer diese MapView
                // im Speicher, auch nachdem der Screen verlassen wurde - wiederholtes Oeffnen der
                // Kartenansicht wuerde so zunehmend Speicher binden und die App verlangsamen.
                onRelease = { it.onDetach() }
            )
        }
    }
}
