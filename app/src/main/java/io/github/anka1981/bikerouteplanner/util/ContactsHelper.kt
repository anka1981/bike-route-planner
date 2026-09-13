package io.github.anka1981.bikerouteplanner.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

/**
 * Liest die erste hinterlegte Postadresse eines ueber den System-Kontaktepicker
 * ausgewaehlten Kontakts aus. Erfordert die Berechtigung READ_CONTACTS.
 */
fun readContactAddress(context: Context, contactUri: Uri): String? {
    val contactId = ContentUris.parseId(contactUri)
    val cursor = context.contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS),
        "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
        arrayOf(contactId.toString(), ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE),
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
