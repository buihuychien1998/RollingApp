package com.buffalo.software.rolling.icon.live.wallpaper.ui.feature

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FeaturesViewModel : ViewModel() {
    private val _selectedFeatures = MutableStateFlow<List<String>>(emptyList())
    val selectedFeatures: StateFlow<List<String>> = _selectedFeatures

    fun toggleFeature(feature: String) {
        val currentList = _selectedFeatures.value.toMutableList()
        if (currentList.contains(feature)) {
            currentList.remove(feature) // Deselect feature
        } else {
            currentList.add(feature) // Select feature
        }
        _selectedFeatures.value = currentList
    }
}
