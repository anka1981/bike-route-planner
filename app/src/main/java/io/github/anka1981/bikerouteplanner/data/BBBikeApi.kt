package io.github.anka1981.bikerouteplanner.data

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Client fuer die offizielle bbbike Routing-API.
 * Dokumentation: https://www.bbbike.org/api.html
 *
 * Jede Instanz deckt eine bestimmte Stadt/Region ab (Pfadsegment [city]), es gibt kein
 * weltweites Routing ueber einen einzigen Endpunkt. Die verfuegbaren Staedte sind unter
 * https://www.bbbike.org/ gelistet.
 */
interface BBBikeApi {

    @GET("api/0.2/{city}/")
    suspend fun route(
        @Path("city") city: String,
        @Query("appid") appId: String,
        @Query("startc_wgs84") start: String,
        @Query("zielc_wgs84") ziel: String,
        @Query("viac_wgs84") via: String? = null,
        @Query("startname") startName: String? = null,
        @Query("zielname") zielName: String? = null,
        @Query("pref_seen") prefSeen: Int = 1,
        @Query("pref_speed") prefSpeed: Int? = null,
        @Query("pref_cat") prefCat: String? = null,
        @Query("pref_quality") prefQuality: String? = null,
        @Query("pref_green") prefGreen: String? = null,
        @Query("pref_ferry") prefFerry: String? = null,
        @Query("pref_unlit") prefUnlit: String? = null,
        @Query("pref_ampel") prefAmpel: String? = null,
        @Query("pref_fragezeichen") prefFragezeichen: String? = null,
        @Query("pref_specialvehicle") prefSpecialvehicle: String? = null,
        @Query("lang") lang: String? = null,
        @Query("output_as") outputAs: String = "gpx-track"
    ): ResponseBody

    companion object {
        const val BASE_URL = "https://api.bbbike.org/"
    }
}
