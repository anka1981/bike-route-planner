package io.github.anka1981.bikerouteplanner.data

class GeocodingRepository(
    private val api: NominatimApi = NetworkModule.nominatimApi
) {
    suspend fun search(query: String): List<Waypoint> {
        if (query.isBlank()) return emptyList()
        return api.search(query).map { result ->
            Waypoint(
                label = result.display_name,
                lat = result.lat.toDouble(),
                lon = result.lon.toDouble()
            )
        }
    }

    suspend fun reverse(lat: Double, lon: Double): Waypoint {
        val result = api.reverse(lat, lon)
        return Waypoint(
            label = result.display_name,
            lat = result.lat.toDouble(),
            lon = result.lon.toDouble()
        )
    }
}
