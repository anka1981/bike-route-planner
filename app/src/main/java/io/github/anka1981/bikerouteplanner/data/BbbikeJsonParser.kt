package io.github.anka1981.bikerouteplanner.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

/** Eine Etappe aus der bbbike-JSON-Antwort: Trackpunkte, Kennzahlen und Ereignisse. */
data class BbbikeLeg(
    val points: List<TrackPoint>,
    val stats: RouteStats,
    val events: List<RouteEvent> = emptyList()
)

/**
 * Liest die Antwort der bbbike-API im Format output_as=json. Sie enthaelt dieselben Punkte
 * wie das Format gpx-track ("LongLatPath", jeweils "lon,lat"), zusaetzlich aber die
 * Kennzahlen der Route: "Len" (Meter), "Trafficlights" (Anzahl Ampeln) und "Speed" -
 * Fahrzeit in Stunden je Geschwindigkeitsstufe, wobei "Pref" = "1" die per pref_speed
 * gewaehlte Stufe markiert. "AffectingBlockings" listet die Ereignisse, die die Route
 * beruehren (nur www.bbbike.de fuellt diese Liste).
 */
object BbbikeJsonParser {

    /** Liefert null, wenn der Text keine gueltige Routen-Antwort ist (z.B. HTML-Fehlerseite). */
    fun parseLeg(text: String): BbbikeLeg? {
        val root = runCatching { Json.parseToJsonElement(text) }.getOrNull() as? JsonObject ?: return null
        val path = root["LongLatPath"] as? JsonArray ?: return null

        val points = path.mapNotNull { element ->
            val parts = (element as? JsonPrimitive)?.contentOrNull?.split(",")
            if (parts == null || parts.size < 2) null else TrackPoint(lat = parts[1].trim(), lon = parts[0].trim())
        }

        val speedTimes = (root["Speed"] as? JsonObject).orEmpty().mapNotNull { (key, value) ->
            val speed = key.toIntOrNull() ?: return@mapNotNull null
            val entry = value as? JsonObject ?: return@mapNotNull null
            val hours = (entry["Time"] as? JsonPrimitive)?.doubleOrNull ?: return@mapNotNull null
            val preferred = (entry["Pref"] as? JsonPrimitive)?.contentOrNull == "1"
            SpeedTime(speed, hours, preferred)
        }.sortedBy { it.speedKmh }

        val stats = RouteStats(
            lengthMeters = (root["Len"] as? JsonPrimitive)?.doubleOrNull ?: 0.0,
            trafficLights = (root["Trafficlights"] as? JsonPrimitive)?.doubleOrNull?.toInt(),
            speedTimes = speedTimes
        )
        return BbbikeLeg(points, stats, parseEvents(root))
    }

    /**
     * Liest "AffectingBlockings". Jeder Eintrag hat den Meldungstext, einen "Index" - daraus
     * entsteht die Kennung "temp-blocking-<Index>", mit der bbbike eine Ausweichroute um genau
     * dieses Ereignis herum berechnet - und "Recurring" fuer wiederkehrende Meldungen.
     */
    private fun parseEvents(root: JsonObject): List<RouteEvent> =
        (root["AffectingBlockings"] as? JsonArray).orEmpty().mapNotNull { element ->
            val entry = element as? JsonObject ?: return@mapNotNull null
            val text = (entry["Text"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            val index = (entry["Index"] as? JsonPrimitive)?.doubleOrNull?.toInt()
            RouteEvent(
                id = index?.let { "temp-blocking-$it" },
                text = text,
                recurring = ((entry["Recurring"] as? JsonPrimitive)?.doubleOrNull ?: 0.0) != 0.0,
                type = (entry["Type"] as? JsonPrimitive)?.contentOrNull
            )
        }

    /**
     * Summiert die Kennzahlen mehrerer Etappen. Fahrzeiten werden nur fuer Geschwindigkeitsstufen
     * ausgewiesen, die jede Etappe geliefert hat, damit keine Teilsummen angezeigt werden.
     */
    fun combine(legs: List<RouteStats>): RouteStats {
        val commonSpeeds = legs
            .map { leg -> leg.speedTimes.map { it.speedKmh }.toSet() }
            .reduceOrNull { acc, speeds -> acc intersect speeds }
            .orEmpty()
        val speedTimes = commonSpeeds.sorted().map { speed ->
            val perLeg = legs.map { leg -> leg.speedTimes.first { it.speedKmh == speed } }
            SpeedTime(speed, perLeg.sumOf { it.hours }, perLeg.all { it.isPreferred })
        }
        val lights = legs.map { it.trafficLights }
        return RouteStats(
            lengthMeters = legs.sumOf { it.lengthMeters },
            trafficLights = lights.filterNotNull().takeIf { it.size == lights.size }?.sum(),
            speedTimes = speedTimes
        )
    }
}
