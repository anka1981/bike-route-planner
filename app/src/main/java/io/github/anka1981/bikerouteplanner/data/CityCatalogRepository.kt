package io.github.anka1981.bikerouteplanner.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

private val CITY_CATALOG_KEY = stringPreferencesKey("bbbike_city_catalog_json")

/**
 * Haelt die Liste der von bbbike.org unterstuetzten Staedte/Regionen. Startet mit der im Code
 * mitgelieferten Momentaufnahme (defaultBbbikeCities) und kann bei Bedarf durch Abruf der
 * bbbike.org-Startseite aktualisiert werden, falls dort neue Staedte hinzugekommen sind.
 */
class CityCatalogRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder().build()

    val citiesFlow: Flow<List<CityOption>> = context.dataStore.data.map { prefs ->
        prefs[CITY_CATALOG_KEY]?.let {
            runCatching { json.decodeFromString<List<CityOption>>(it) }.getOrNull()
        }?.takeIf { it.isNotEmpty() } ?: defaultBbbikeCities
    }

    /** Laedt die aktuelle Staedteliste von bbbike.org und ersetzt den lokalen Cache. */
    suspend fun refreshFromBbbike(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("https://www.bbbike.org/").build()
            val html = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("HTTP ${response.code}")
                    )
                }
                response.body?.string().orEmpty()
            }

            val pattern = Regex("""class="tagcloud\d+"><a class="C_[^"]*" href="([^"]+)/">([^<]+)</a>""")
            val scraped = pattern.findAll(html)
                .map { CityOption(it.groupValues[1], it.groupValues[2]) }
                .toList()

            // "bbbike" (die historische Berlin-Originalinstanz) taucht in bbbike.orgs oeffentlicher
            // Staedteliste nicht auf, liefert aber nachweislich aktuellere/schnellere Ergebnisse als
            // die dort gelistete separate "Berlin"-Instanz - daher bei jedem Refresh wieder ergaenzen.
            if (scraped.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Keine Staedte gefunden."))
            }

            val bbbikeInstance = CityOption("bbbike", "Berlin (bbbike-Originalinstanz, empfohlen)")
            val cities = (listOf(bbbikeInstance) + scraped).distinctBy { it.slug }

            context.dataStore.edit { prefs ->
                prefs[CITY_CATALOG_KEY] = json.encodeToString(cities)
            }
            Result.success(cities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
