package com.example.rollingicon.ui.share_view_model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    private val _iconsChanged = MutableStateFlow(false)
    val iconsChanged: StateFlow<Boolean> = _iconsChanged

    fun setIconsChanged(changed: Boolean) {
        _iconsChanged.value = changed
    }
}