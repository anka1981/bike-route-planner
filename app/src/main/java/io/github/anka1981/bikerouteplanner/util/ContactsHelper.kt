package io.github.anka1981.bikerouteplanner.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

/**
 * Liest die Postadresse aus dem Dateneintrag aus, den der Postadress-Kontaktepicker
 * (ACTION_PICK auf [ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI]) zurueckgibt.
 * Dieser Picker gewaehrt fuer die ausgewaehlte Zeile eine befristete Leseberechtigung, sodass die
 * App dafuer keine eigene READ_CONTACTS-Berechtigung braucht.
 */
fun readContactAddress(context: Context, addressDataUri: Uri): String? {
    val cursor = context.contentResolver.query(
        addressDataUri,
        arrayOf(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS),
        null,
        null,
        null
    )
    cursor.use {
        if (it != null && it.moveToFirst()) {
            val columnIndex = it.getColumnIndex(
                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
            )
            if (columnIndex >= 0) return it.getString(columnIndex)
        }
    }
    return null
}
