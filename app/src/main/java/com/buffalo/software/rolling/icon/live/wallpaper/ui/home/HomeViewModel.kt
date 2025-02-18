package com.buffalo.software.rolling.icon.live.wallpaper.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buffalo.software.rolling.icon.live.wallpaper.models.AppIcon
import com.buffalo.software.rolling.icon.live.wallpaper.routes.AppRoutes
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper.isFirstLoadIcon
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper.loadSelectedIconsFromPreferences
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper.saveAppIconsFromPreferences
import com.buffalo.software.rolling.icon.live.wallpaper.utils.defaultApps
import com.buffalo.software.rolling.icon.live.wallpaper.utils.loadAppIconsConcurrently
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val application: Application) : AndroidViewModel(application) {

    var clickedRoutes: AppRoutes = AppRoutes.AppPicker

    val appIcons = mutableStateOf<List<AppIcon>?>(null)

    var loading by mutableStateOf(true)
        private set

    init {
        loadAppIcons()
    }

    fun loadAppIcons() {
        val context = application.applicationContext
        val packageManager = context.packageManager
        viewModelScope.launch(Dispatchers.IO) {
            try {
                loading = true
                // Check if this is the first load, and decide what to load
                val icons = if (isFirstLoadIcon(context)) {
                    // Load default apps first
//                    val icons = getPackageNameForAppName(context, defaultApps)
//                        .asSequence() // Use sequence to minimize intermediate collection overhead
//                        .mapNotNull { packageName ->
//                            getAppIconFromPackageName(packageManager, packageName)
//                        }
//                        .toMutableList()
                    val icons = loadAppIconsConcurrently(context, defaultApps)

                    // If there are not enough icons, load more apps from the device
//                    if (icons.size < MAX_APP_ICONS_TO_LOAD) {
//                        val additionalApps = getAppsFromDevice(packageManager, MAX_APP_ICONS_TO_LOAD - icons.size)
//                        icons.addAll(additionalApps)
//                    }

                    // Save these icons to preferences for future loads
                    saveAppIconsFromPreferences(context, icons.toMutableList())

                    icons
                } else {
                    delay(500)
                    // Load saved icons from preferences
                    loadSelectedIconsFromPreferences(context)
                }

                // Update the appIcons list on the main thread
                updateIcons(icons)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    private suspend fun updateIcons(newIcons: List<AppIcon>) {
        withContext(Dispatchers.Main) {
            appIcons.value = newIcons
        }
    }

}