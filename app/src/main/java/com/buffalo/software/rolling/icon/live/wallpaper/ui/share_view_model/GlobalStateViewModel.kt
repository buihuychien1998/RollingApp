package com.buffalo.software.rolling.icon.live.wallpaper.ui.share_view_model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GlobalStateViewModel : ViewModel() {
    private val _enabled = mutableStateOf(true)
    val enabled: State<Boolean> get() = _enabled

    fun disableTemporarily() {
        if (_enabled.value) {
            _enabled.value = false
            viewModelScope.launch(Dispatchers.IO) {
                delay(1000L) // Delay for 1 second
                withContext(Dispatchers.Main) {
                    _enabled.value = true
                }
            }
        }
    }
}