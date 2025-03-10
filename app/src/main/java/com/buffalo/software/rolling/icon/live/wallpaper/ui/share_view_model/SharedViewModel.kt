package com.buffalo.software.rolling.icon.live.wallpaper.ui.share_view_model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    private val _iconsChanged = MutableStateFlow(false)
    val iconsChanged: StateFlow<Boolean> = _iconsChanged

    fun setIconsChanged(changed: Boolean) {
        _iconsChanged.value = changed
    }

    private val _appIcon = MutableStateFlow(false)
    val appIcon: StateFlow<Boolean> = _appIcon

    fun setAppIcon(changed: Boolean) {
        _appIcon.value = changed
    }

    private val _backgroundChanged = MutableStateFlow(false)
    val backgroundChanged: StateFlow<Boolean> = _backgroundChanged
    fun setBackgroundChanged(changed: Boolean) {
        _backgroundChanged.value = changed
    }
}