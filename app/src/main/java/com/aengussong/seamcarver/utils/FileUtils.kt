package com.aengussong.seamcarver.utils

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

fun getFileFromUri(file: Uri, contentResolver: ContentResolver, dirToStoreTempFile: String): File? {
    val filePath: String?
    try {
        val inputStream = contentResolver.openInputStream(file)
        filePath = dirToStoreTempFile + "/" + getFileName(file, contentResolver)
        File(filePath).createNewFile()
        val outputStream = FileOutputStream(filePath)
        outputStream.write(inputStream?.readBytes())
        // close stream
        try {
            inputStream?.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
    }
    return File(filePath)
}

fun getFileName(uri: Uri, contentResolver: ContentResolver): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }
                    ?: throw IllegalArgumentException()
                result = cursor.getString(columnIndex)
            }
        } finally {
            cursor!!.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}