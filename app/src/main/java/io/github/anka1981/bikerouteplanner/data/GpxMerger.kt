package io.github.anka1981.bikerouteplanner.data

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

data class TrackPoint(val lat: String, val lon: String, val ele: String? = null)

/**
 * Fuegt mehrere bbbike-Etappen (z.B. Start->Stopp1, Stopp1->Stopp2, ...) zu einer
 * einzigen durchgehenden GPX-Datei mit einem Track zusammen, damit Karten-Apps die
 * gesamte Route mit mehreren Zwischenstationen als einen zusammenhaengenden Track sehen.
 */
object GpxMerger {

    fun extractTrackPoints(gpxXml: String): List<TrackPoint> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(gpxXml))

        val points = mutableListOf<TrackPoint>()
        var eventType = parser.eventType
        var lat: String? = null
        var lon: String? = null
        var ele: String? = null
        var inEle = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "trkpt", "rtept" -> {
                            lat = parser.getAttributeValue(null, "lat")
                            lon = parser.getAttributeValue(null, "lon")
                            ele = null
                        }

                        "ele" -> inEle = true
                    }
                }

                XmlPullParser.TEXT -> {
                    if (inEle) ele = parser.text?.trim()
                }

                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "trkpt", "rtept" -> {
                            if (lat != null && lon != null) {
                                points.add(TrackPoint(lat, lon, ele))
                            }
                            lat = null
                            lon = null
                            ele = null
                        }

                        "ele" -> inEle = false
                    }
                }
            }
            eventType = parser.next()
        }
        return points
    }

    /** Haengt die Punkte mehrerer Etappen aneinander. */
    fun joinLegs(legs: List<List<TrackPoint>>): List<TrackPoint> {
        val allPoints = mutableListOf<TrackPoint>()
        for (legPoints in legs) {
            if (legPoints.isEmpty()) continue
            // Duplizierten Verbindungspunkt an der Etappen-Naht ueberspringen.
            val startIndex = if (
                allPoints.isNotEmpty() &&
                legPoints.first().lat == allPoints.last().lat &&
                legPoints.first().lon == allPoints.last().lon
            ) 1 else 0
            allPoints.addAll(legPoints.subList(startIndex, legPoints.size))
        }
        return allPoints
    }

    /** Erzeugt eine GPX-Datei mit einem einzigen Track aus den gegebenen Punkten. */
    fun toGpx(allPoints: List<TrackPoint>, trackName: String): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"BikeRoutePlanner (via bbbike.org)\" ")
        sb.append("xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
        sb.append("  <trk>\n    <name>").append(escapeXml(trackName)).append("</name>\n    <trkseg>\n")
        for (p in allPoints) {
            sb.append("      <trkpt lat=\"").append(p.lat).append("\" lon=\"").append(p.lon).append("\">")
            if (p.ele != null) sb.append("<ele>").append(p.ele).append("</ele>")
            sb.append("</trkpt>\n")
        }
        sb.append("    </trkseg>\n  </trk>\n</gpx>\n")
        return sb.toString()
    }

    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
