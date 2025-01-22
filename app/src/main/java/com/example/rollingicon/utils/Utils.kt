package com.example.rollingicon.utils

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.activity.result.ActivityResultLauncher
import com.example.rollingicon.models.AppIcon
import com.example.rollingicon.services.RollingIconWallpaperService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
    val bitmap = if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        bitmap
    }
    return bitmap
}

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, size)
}

fun ByteArray.toDrawable(): Drawable? {
    val inputStream = ByteArrayInputStream(this)
    return Drawable.createFromStream(inputStream, null)
}

fun getPackageNameForAppName(context: Context, appNames: List<String>): List<String> {
    val packageManager = context.packageManager
    val packageNames = mutableListOf<String>()

    val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    appNames.forEach { appName ->
        for (app in installedApps) {
            try {
                // Check if the app name matches the application label
                val appLabel = packageManager.getApplicationLabel(app)
                if (appLabel.toString().equals(appName, ignoreCase = true)) {
                    packageNames.add(app.packageName)
                    break
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // Handle exception if the app name is not found
                e.printStackTrace()
            }
        }
    }

    return packageNames
}

// Function to get an AppIcon from PackageManager using app name
fun getAppIconFromPackageName(packageManager: PackageManager, appName: String): AppIcon? {
    return try {
        val packageInfo = packageManager.getPackageInfo(appName, 0)
        val appInfo = packageInfo.applicationInfo

        // Load data asynchronously
        runBlocking {
            val deferredIcon = async(Dispatchers.Default) {
                packageManager.getApplicationIcon(appInfo).let { drawable ->
                    getBitmapFromDrawable(drawable).toByteArray()
                }
            }

            val deferredLabel = async(Dispatchers.Default) {
                packageManager.getApplicationLabel(appInfo).toString()
            }

            // Wait for both icon and label to complete
            AppIcon(
                drawable = deferredIcon.await() ?: ByteArray(0), // Handle null drawable
                packageName = appInfo.packageName,
                name = deferredLabel.await()
            )
        }
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}


suspend fun loadAppIconsConcurrently(context: Context, appNames: List<String>): List<AppIcon> = coroutineScope {
    val packageManager = context.packageManager
    val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    val appNameToInfoMap = installedApps.associateBy {
        packageManager.getApplicationLabel(it).toString()
    }

    appNames.mapNotNull { appName ->
        appNameToInfoMap[appName]?.let { appInfo ->
            async {
                try {
                    val iconDrawable = packageManager.getApplicationIcon(appInfo)
                    val iconBitmap = getBitmapFromDrawable(iconDrawable).toByteArray()
                    val appLabel = packageManager.getApplicationLabel(appInfo).toString()

                    AppIcon(
                        drawable = iconBitmap,
                        packageName = appInfo.packageName,
                        name = appLabel
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }.mapNotNull { it.await() }
}
// Function to get all installed apps on the device
suspend fun getAppsFromDevice(packageManager: PackageManager, limit: Int): List<AppIcon> {
    return withContext(Dispatchers.IO) {
        val installedPackages =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        installedPackages
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }  // Exclude system apps
            .take(limit).mapNotNull { appInfo ->
                val drawable: Drawable = packageManager.getApplicationIcon(appInfo)
                val bitmap = getBitmapFromDrawable(drawable)
                AppIcon(
                    drawable = bitmap.toByteArray(),
                    packageName = appInfo.packageName,
                    name = packageManager.getApplicationLabel(appInfo).toString()
                )
            }
    }
}

fun getInstalledApps(packageManager: PackageManager): MutableList<AppIcon> {
    val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    return runBlocking {
        installedApplications
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // Exclude system apps
            .map { appInfo ->
                async(Dispatchers.Default) {
                    // Use async to parallelize loading icon and labels
                    AppIcon(
                        drawable = appInfo.loadIcon(packageManager)?.let { icon ->
                            getBitmapFromDrawable(icon).toByteArray()
                        },
                        packageName = appInfo.packageName,
                        name = appInfo.loadLabel(packageManager).toString()
                    )
                }
            }
            .awaitAll() // Await all parallelized tasks
            .toMutableList()
    }
//    val installedApplications =
//        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
//    return installedApplications
//        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }  // Exclude system apps
//        .map {
//
//            AppIcon(
//                drawable = getBitmapFromDrawable(it.loadIcon(packageManager)).toByteArray(),
//                packageName = it.packageName,
//                name = it.loadLabel(packageManager).toString()
//            )
//        }.toMutableList()
}

fun Context.startWallpaperService(launcher: ActivityResultLauncher<Intent>) {
    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
    intent.putExtra(
        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
        ComponentName(this, RollingIconWallpaperService::class.java)
    )
    launcher.launch(intent)
}

fun Context.isRollingIconWallpaperSet(): Boolean {
    val wallpaperManager = WallpaperManager.getInstance(this)
    val currentWallpaper = wallpaperManager.wallpaperInfo
    return currentWallpaper?.packageName == packageName &&
            currentWallpaper?.serviceName == RollingIconWallpaperService::class.java.name
}


/**
 * [Linear Interpolation](https://en.wikipedia.org/wiki/Linear_interpolation) function that moves
 * amount from it's current position to start and amount
 * @param start of interval
 * @param stop of interval
 * @param fraction closed unit interval [0, 1]
 */
internal fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}

/**
 * Scale pos from start1..end1 range to start2..end2 range
 * 50 in [0-100] interval is scaled to 150 in [100-200] interval.
 */
internal fun scale(start1: Float, end1: Float, pos: Float, start2: Float, end2: Float) =
    lerp(start2, end2, calculateFraction(start1, end1, pos))

/**
 * Calculate fraction for value between a range [end] and [start] coerced into 0f-1f range
 */
internal fun calculateFraction(start: Float, end: Float, pos: Float) =
    (if (end - start == 0f) 0f else (pos - start) / (end - start)).coerceIn(0f, 1f)