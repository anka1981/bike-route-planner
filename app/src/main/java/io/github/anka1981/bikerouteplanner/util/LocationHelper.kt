package io.github.anka1981.bikerouteplanner.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

/** Liefert die zuletzt bekannte Position ueber den plattformeigenen LocationManager. */
fun getLastKnownLocation(context: Context): Location? {
    if (!hasLocationPermission(context)) return null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        ?: return null

    var best: Location? = null
    for (provider in manager.getProviders(true)) {
        val location = try {
            manager.getLastKnownLocation(provider)
        } catch (e: SecurityException) {
            null
        }
        if (location != null && (best == null || location.accuracy < best!!.accuracy)) {
            best = location
        }
    }
    return best
}
