package com.example.rollingicon.ui.image_picker

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rollingicon.models.AppIcon
import com.example.rollingicon.utils.IconType
import com.example.rollingicon.utils.MAX_APP_ICONS_TO_LOAD
import com.example.rollingicon.utils.PreferencesHelper
import com.example.rollingicon.utils.getCompressedBitmapFromUri
import com.example.rollingicon.utils.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImagePickerViewModel(private val application: Application) : AndroidViewModel(application) {
    // List of selected media
    private val _selectedAppIcons = MutableStateFlow<MutableList<AppIcon>>(mutableListOf())
    val selectedAppIcons: StateFlow<MutableList<AppIcon>> = _selectedAppIcons

    private val _selectedImage = MutableStateFlow<MutableList<AppIcon>?>(null)
    val selectedImage: StateFlow<MutableList<AppIcon>?> = _selectedImage

    private val _selectedVideo = MutableStateFlow<MutableList<AppIcon>>(mutableListOf())
    val selectedVideo: StateFlow<MutableList<AppIcon>> = _selectedVideo

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
                (selectedImage.value ?: mutableListOf()).forEach {
                    println(it.type)
                    println(it.filePath)
                }

                val videoFromPreferences = withContext(Dispatchers.IO) {
                    PreferencesHelper.loadVideoIconsFromPreferences(context)
                }
                _selectedVideo.value = videoFromPreferences
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    // Add media to the list
    fun addMedia(uri: Uri) {
        val context = application.applicationContext
        val name = uri.lastPathSegment ?: "Unnamed"
        val filePath = uri.toString()

        val appIcon = AppIcon(
            drawable = null,
            packageName = "",
            name = name,
            type = IconType.IMAGE.name,
            filePath = filePath,
            selected = (_selectedAppIcons.value.size + (selectedImage.value ?: mutableListOf()).filter { it.selected }.size + _selectedVideo.value.filter { it.selected }.size) < MAX_APP_ICONS_TO_LOAD
        )

        val updatedList = ((selectedImage.value ?: mutableListOf()) + appIcon).toMutableList()
        _selectedImage.value = updatedList
    }

    fun toggleSelection(index: Int) {
        if (index !in (selectedImage.value ?: mutableListOf()).indices || _selectedAppIcons.value.size + (_selectedImage.value ?: mutableListOf()).filter { it.selected }.size + _selectedVideo.value.filter { it.selected }.size >= MAX_APP_ICONS_TO_LOAD) return // Ensure index is valid
        // Print current state
        val updatedList = (selectedImage.value ?: mutableListOf()).toMutableList()

        // Update the selected status of the app icon at the given index
        updatedList[index] = updatedList[index].copy(selected = !updatedList[index].selected)
        println("${updatedList[index].selected}")

        // Update the state with the new mutable list
        (_selectedImage.value ?: mutableListOf()).clear()
        _selectedImage.value = updatedList
    }

    fun deleteItem(index: Int) {
        println("_selectedImage.value index $index")
        println("_selectedImage.value v ${_selectedImage.value?.size}")
        // Ensure the list is not null and the index is valid
        val currentList = _selectedImage.value ?: return
        if (index !in currentList.indices) return

        // Create a new list without modifying the original list directly
        val updatedMedia = currentList.toMutableList().apply {
            removeAt(index)
        }

        // Update the state with the new list
        _selectedImage.value = updatedMedia

        println("_selectedImage.value size after deletion: ${_selectedImage.value?.size}")
    }

    // Clear all selected media
    fun clearSelection() {
        _selectedImage.value = (_selectedImage.value ?: mutableListOf()).map { it.copy(selected = false) }.toMutableList()
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
                    if (it.type == IconType.IMAGE.name && it.drawable == null) {
                        println("saveSelectedIcons im here ${it.filePath}")
                        it.drawable =
                            context.getCompressedBitmapFromUri(Uri.parse(it.filePath))?.toByteArray()
                    }
                }
                PreferencesHelper.saveImageIconsFromPreferences(context, updatedList)
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