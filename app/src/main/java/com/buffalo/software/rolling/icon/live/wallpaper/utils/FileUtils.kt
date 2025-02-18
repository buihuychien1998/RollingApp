package com.buffalo.software.rolling.icon.live.wallpaper.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    Log.d("Debug", "getFileNameFromUri $uri:")
    Log.d("Debug", "getFileNameFromUri ${uri.scheme}:")
    context.grantUriPermission(context.packageName,
        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return when (uri.scheme) {
        "content" -> {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            }
        }
        "file" -> File(uri.path ?: "").name
        else -> null
    }
}

fun cloneFileToCacheWithOriginalName(context: Context, uri: Uri): Uri? {
    return try {
        Log.d("Debug", "cloneFileToCacheWithOriginalName:")

        // Get file name from the URI
        val fileName = getFileNameFromUri(context, uri) ?: throw IllegalArgumentException("File name not found")
        Log.d("Debug", "Cloning file with name: $fileName")

        // Open the input stream from the URI
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Failed to open input stream for URI: $uri")

        // Create a new file in the cache directory
        val cacheFile = File(context.cacheDir, fileName)
        Log.d("Debug", "Cache file path: ${cacheFile.absolutePath}")

        // Copy the contents
        inputStream.use { input ->
            FileOutputStream(cacheFile).use { output ->
                input.copyTo(output)
            }
        }


        // Return the FileProvider URI for the cloned file
        FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheFile)
    } catch (e: Exception) {
        Log.e("Error", "Failed to clone the file: ${e.message}")
        e.printStackTrace()
        null
    }
}
fun cloneFileToCache(context: Context, uri: Uri, fileName: String): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val cacheFile = File(context.cacheDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(cacheFile).use { output ->
                input.copyTo(output)
            }
        }

        // Return FileProvider URI
        FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
fun Context.getVideoThumbnail(uri: Uri, maxWidth: Int = 400, maxHeight: Int = 400, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 90): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(this, uri)
        // Extract a frame at the first available position
        val thumbnail = retriever.frameAtTime

        // If a thumbnail was successfully extracted
        if (thumbnail != null) {
            // Resize the thumbnail while maintaining its aspect ratio
            val resizedBitmap = getResizedBitmap(thumbnail, maxWidth, maxHeight)

            // Compress the resized bitmap to the desired format and quality
            compressBitmap(resizedBitmap, format, quality)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        retriever.release()
    }
}


fun Context.getBitmapFromUri(uri: Uri): Bitmap? {
    // Open an input stream from the URI
    val inputStream: InputStream? = this.contentResolver.openInputStream(uri)

    // Return the Bitmap from the input stream if available
    return inputStream?.let {
        BitmapFactory.decodeStream(it)
    }
}

fun Context.getCompressedBitmapFromUri(
    uri: Uri,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 90,  // Default high quality to minimize loss
    maxWidth: Int = 400,
    maxHeight: Int = 400
): Bitmap? {
    // Open an input stream from the URI
    val inputStream: InputStream? = this.contentResolver.openInputStream(uri)

    // Return the Bitmap from the input stream if available
    return inputStream?.let { input ->
        val originalBitmap = BitmapFactory.decodeStream(input)

        // Resize the bitmap first while maintaining aspect ratio
        val resizedBitmap = getResizedBitmap(originalBitmap, maxWidth, maxHeight)

        // Compress the resized bitmap to the chosen format (JPEG/PNG)
        compressBitmap(resizedBitmap, format, quality)
    }
}

// Helper function to resize the bitmap while maintaining the aspect ratio
private fun getResizedBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    // Calculate the scaling factor based on the max width and height
    val ratioBitmap = width.toFloat() / height.toFloat()
    var newWidth = maxWidth
    var newHeight = maxHeight

    if (width > height) {
        newHeight = (maxWidth / ratioBitmap).toInt()
    } else if (height > width) {
        newWidth = (maxHeight * ratioBitmap).toInt()
    } else {
        // If it's square, just scale down both dimensions
        newWidth = maxWidth
        newHeight = maxHeight
    }

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

// Helper function to compress the bitmap without losing too much quality
private fun compressBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): Bitmap {
    // Create a ByteArrayOutputStream to hold the compressed bitmap data
    val byteArrayOutputStream = ByteArrayOutputStream()

    // Compress the bitmap into the output stream
    bitmap.compress(format, quality, byteArrayOutputStream)

    // Get the compressed byte array
    val compressedBitmapData = byteArrayOutputStream.toByteArray()

    // Decode the byte array into a bitmap and return it
    return BitmapFactory.decodeByteArray(compressedBitmapData, 0, compressedBitmapData.size)
}