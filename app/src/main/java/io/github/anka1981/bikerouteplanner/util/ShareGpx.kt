package io.github.anka1981.bikerouteplanner.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Teilt eine GPX-Datei ueber den Android-Share-Chooser. Absichtlich kein fester
 * Ziel-App-Intent (z.B. auf OsmAnd oder Locus Map beschraenkt): der Nutzer waehlt selbst,
 * welche installierte Karten-App die Datei zur Ansicht/Navigation entgegennehmen soll.
 */
fun shareGpxFile(context: Context, file: File, chooserTitle: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/gpx+xml"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
