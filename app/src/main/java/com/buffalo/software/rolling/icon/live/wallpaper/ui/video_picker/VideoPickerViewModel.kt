package com.buffalo.software.rolling.icon.live.wallpaper.ui.video_picker

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buffalo.software.rolling.icon.live.wallpaper.models.AppIcon
import com.buffalo.software.rolling.icon.live.wallpaper.utils.IconType
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper
import com.buffalo.software.rolling.icon.live.wallpaper.utils.TIME_DELAY
import com.buffalo.software.rolling.icon.live.wallpaper.utils.getVideoThumbnail
import com.buffalo.software.rolling.icon.live.wallpaper.utils.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

class VideoPickerViewModel(private val application: Application) : AndroidViewModel(application) {
    private val mutex = Mutex()

    // List of selected media
    private val _selectedAppIcons = MutableStateFlow<MutableList<AppIcon>>(mutableListOf())
    val selectedAppIcons: StateFlow<MutableList<AppIcon>> = _selectedAppIcons

    private val _selectedImage = MutableStateFlow<MutableList<AppIcon>>(mutableListOf())
    val selectedImage: StateFlow<MutableList<AppIcon>> = _selectedImage

    private val _selectedVideo = MutableStateFlow<MutableList<AppIcon>?>(null)
    val selectedVideo: StateFlow<MutableList<AppIcon>?> = _selectedVideo

    private val _initialSelectedApps = MutableStateFlow<MutableList<AppIcon>>(mutableListOf())
    val initialSelectedApps: StateFlow<MutableList<AppIcon>> = _initialSelectedApps

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        loadAppIcons()
    }

    fun loadAppIcons() {
        val context = application.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(TIME_DELAY)
                _loading.value = true
                // Load selected icons from SharedPreferences
                val iconsFromPreferences = withContext(Dispatchers.IO) {
                    PreferencesHelper.loadAppIconsFromPreferences(context)
                }

                _selectedAppIcons.value = iconsFromPreferences

                val imageFromPreferences = withContext(Dispatchers.IO) {
                    PreferencesHelper.loadImageIconsFromPreferences(context)
                }
                _selectedImage.value = imageFromPreferences
                _initialSelectedApps.value = imageFromPreferences.toMutableList()

                val videoFromPreferences = withContext(Dispatchers.IO) {
                    PreferencesHelper.loadVideoIconsFromPreferences(context)
                }
                _selectedVideo.value = videoFromPreferences
                _initialSelectedApps.value = videoFromPreferences.toMutableList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    // Add media to the list
    fun addMedia(appIcons: List<AppIcon>) {
        val updatedList = ((_selectedVideo.value ?: mutableListOf()) + appIcons).toMutableList()
        _selectedVideo.value = updatedList
    }

    fun toggleSelection(index: Int) {
        if (index !in (_selectedVideo.value ?: mutableListOf()).indices) return // Ensure index is valid

        // Make a copy of the current list with the updated item
        val updatedMedia = (_selectedVideo.value ?: mutableListOf()).toMutableList()
        updatedMedia[index] = updatedMedia[index].copy(selected = !updatedMedia[index].selected)

        // Replace the entire list with the updated one
        (_selectedVideo.value ?: mutableListOf()).clear()
        _selectedVideo.value = updatedMedia
    }

    fun deleteItem(index: Int) {
        if (index !in (_selectedVideo.value ?: mutableListOf()).indices) return // Ensure index is valid

        // Make a copy of the current list with the updated item
        val updatedMedia = (_selectedVideo.value ?: mutableListOf()).toMutableList()
        updatedMedia.removeAt(index)

        // Replace the entire list with the updated one
        (_selectedVideo.value ?: mutableListOf()).clear()
        _selectedVideo.value = updatedMedia
    }

    // Clear all selected media
    fun clearSelection() {
        _selectedVideo.value = (_selectedVideo.value ?: mutableListOf()).map { it.copy(selected = false) }.toMutableList()
    }

    fun saveSelectedIcons(
        updatedList: MutableList<AppIcon>, onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {}
    ) {
        val context = application.applicationContext

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.value = true // Show loading indicator

                updatedList.forEach {
                    if (it.type == IconType.VIDEO.name && it.drawable == null) {
                        println("saveSelectedIcons im here ${it.filePath}")
                        it.drawable = context.getVideoThumbnail(Uri.parse(it.filePath))?.toByteArray()
                    }
                }
                PreferencesHelper.saveVideoIconsFromPreferences(context, updatedList)

                // Save selected icons to SharedPreferences
                withContext(Dispatchers.Main) {
                    onSuccess() // Trigger success callback
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure(e) // Trigger failure callback
                Log.e("AppPicker", "Error saving selected icons: ${e.message}")
            } finally {
                _loading.value = false // Hide loading indicator
            }
        }
    }
}