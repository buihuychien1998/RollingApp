package com.example.rollingicon.ui.app_picker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rollingicon.models.AppIcon
import com.example.rollingicon.utils.PreferencesHelper
import com.example.rollingicon.utils.PreferencesHelper.loadAppIconsFromPreferences
import com.example.rollingicon.utils.PreferencesHelper.loadImageIconsFromPreferences
import com.example.rollingicon.utils.PreferencesHelper.loadVideoIconsFromPreferences
import com.example.rollingicon.utils.TIME_DELAY
import com.example.rollingicon.utils.getInstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppPickerViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _allInstalledApps = MutableStateFlow<MutableList<AppIcon>?>(null)
    val allInstalledApps: StateFlow<MutableList<AppIcon>?> = _allInstalledApps

    private val _selectedAppIcons = MutableStateFlow<MutableList<AppIcon>>(mutableListOf())
    val selectedAppIcons: StateFlow<MutableList<AppIcon>> = _selectedAppIcons

    private val _selectedImage = MutableStateFlow<MutableList<AppIcon>>(mutableListOf())
    val selectedImage: StateFlow<MutableList<AppIcon>> = _selectedImage

    private val _selectedVideo = MutableStateFlow<MutableList<AppIcon>>(mutableListOf())
    val selectedVideo: StateFlow<MutableList<AppIcon>> = _selectedVideo

    private val _initialSelectedApps = MutableStateFlow<MutableList<AppIcon>>(mutableListOf())
    val initialSelectedApps: StateFlow<MutableList<AppIcon>> = _initialSelectedApps

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredApps: StateFlow<List<AppIcon>?> =
        _searchQuery.combine(_allInstalledApps) { query, apps ->
            if (query.isEmpty()) {
                apps // Return null if query is empty
            } else {
                apps?.filter { it.name.contains(query, ignoreCase = true) }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        loadAppData()
    }

    fun loadAppData() {
        val context = application.applicationContext
        viewModelScope.launch {
            delay(TIME_DELAY)
            _loading.value = true

            // Perform all I/O operations concurrently
            val (iconsFromPreferences, installedApps, imageFromPreferences, videoFromPreferences) = withContext(Dispatchers.IO) {
                val iconsDeferred = async { loadAppIconsFromPreferences(context) }
                val appsDeferred = async {
                    val packageManager = context.packageManager
                    getInstalledApps(packageManager)
                }
                val imageDeferred = async { loadImageIconsFromPreferences(context) }
                val videoDeferred = async { loadVideoIconsFromPreferences(context) }

                // Await all deferred results
                listOf(
                    iconsDeferred.await(),
                    appsDeferred.await(),
                    imageDeferred.await(),
                    videoDeferred.await()
                )
            }

            // Update UI state
            _selectedAppIcons.value = iconsFromPreferences
            _initialSelectedApps.value = iconsFromPreferences.toMutableList()
            _allInstalledApps.value = installedApps
            _selectedImage.value = imageFromPreferences
            _selectedVideo.value = videoFromPreferences

            _loading.value = false
        }
    }

    fun addIcon(appIcon: AppIcon) {
        if (!_selectedAppIcons.value.contains(appIcon)) {
            val updatedList = (_selectedAppIcons.value + appIcon).toMutableList()
            _selectedAppIcons.value = updatedList
        }
    }

    fun removeIcon(appIcon: AppIcon) {
        if (_selectedAppIcons.value.contains(appIcon)) {
            val updatedList = (_selectedAppIcons.value - appIcon).toMutableList()
            _selectedAppIcons.value = updatedList
        }
    }

    fun saveSelectedIcons(
        updatedList: MutableList<AppIcon>, onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {}
    ) {
        val context = application.applicationContext

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.value = true // Show loading indicator

                PreferencesHelper.saveAppIconsFromPreferences(context, updatedList)
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

    fun clearSelectedIcons() {
        _selectedAppIcons.value.clear()
        saveSelectedIcons(mutableListOf())
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}