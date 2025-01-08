package com.example.rollingicon

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun Drawable.toByteArray(): ByteArray {
    val bitmap = (this as BitmapDrawable).bitmap
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
    val bmp = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bmp)
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    drawable.draw(canvas)
    return bmp
}
fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, size)
}

fun ByteArray.toDrawable(): Drawable? {
    val inputStream = ByteArrayInputStream(this)
    return Drawable.createFromStream(inputStream, null)
}

fun getInstalledApps(packageManager: PackageManager): List<AppIcon> {
    val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    return installedApplications
        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }  // Exclude system apps
        .map {

            AppIcon(
                drawable = getBitmapFromDrawable(it.loadIcon(packageManager)).toByteArray(),
                packageName = it.packageName,
                name = it.loadLabel(packageManager).toString()
            )
        }
}

object Utils {
}