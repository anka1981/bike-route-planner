package io.github.anka1981.bikerouteplanner.data

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Geokodierung ueber den OpenStreetMap Nominatim-Dienst, um Adress-Text in Koordinaten
 * umzuwandeln (die bbbike-API selbst erwartet bereits Koordinaten, keine Adress-Strings).
 * Nutzungsrichtlinie: https://operations.osmfoundation.org/policies/nominatim/
 */
interface NominatimApi {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("limit") limit: Int = 5,
        @Query("accept-language") acceptLanguage: String = "de"
    ): List<NominatimResult>

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "jsonv2",
        @Query("accept-language") acceptLanguage: String = "de"
    ): NominatimResult

    companion object {
        const val BASE_URL = "https://nominatim.openstreetmap.org/"
    }
}

@Serializable
data class NominatimResult(
    val lat: String,
    val lon: String,
    val display_name: String
)
