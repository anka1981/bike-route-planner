package io.github.anka1981.bikerouteplanner.data

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Zugriff auf die Berliner bbbike-Webseite www.bbbike.de.
 *
 * Die offizielle API unter api.bbbike.org liefert dieselben Routen, kennt aber die
 * "Ereignisse" (temporaere Sperrungen, Baustellen, Maerkte) nicht - dort ist
 * "AffectingBlockings" immer leer. Nur diese CGI meldet sie und kann per
 * "custom=temp-blocking-<id>" eine Ausweichroute um ausgewaehlte Ereignisse herum berechnen.
 *
 * Die Seite ist ausschliesslich per http erreichbar, siehe res/xml/network_security_config.xml.
 */
interface BbbikeDeApi {

    @GET("cgi-bin/{cgi}")
    suspend fun route(
        @Path("cgi") cgi: String,
        @Query("startc_wgs84") start: String,
        @Query("zielc_wgs84") ziel: String,
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
        /** Je Ereignis ein "temp-blocking-<id>"; die Route wird dann darum herumgefuehrt. */
        @Query("custom") avoidBlockings: List<String>? = null,
        @Query("output_as") outputAs: String = "json"
    ): ResponseBody

    companion object {
        const val BASE_URL = "http://www.bbbike.de/"

        /** Deutschsprachige Variante; die Sprache haengt am CGI, nicht an einem Parameter. */
        const val CGI_GERMAN = "bbbike.cgi"
        const val CGI_ENGLISH = "bbbike.en.cgi"

        /** Stadt-Slugs, fuer die www.bbbike.de zustaendig ist (die Berliner Instanz). */
        val BERLIN_SLUGS = setOf("bbbike", "berlin")

        fun cgiFor(routingLanguage: String): String =
            if (routingLanguage.equals("en", ignoreCase = true)) CGI_ENGLISH else CGI_GERMAN
    }
}
