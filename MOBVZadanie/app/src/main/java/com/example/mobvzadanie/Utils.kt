package com.example.mobvzadanie

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File
import java.io.FileOutputStream

class Utils {
    suspend fun saveImage(context: Context, uri: android.net.Uri, filename: String) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, filename)

        try {
            context.contentResolver.openInputStream(uri)?.use {
                inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}