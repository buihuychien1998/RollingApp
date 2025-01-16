package com.example.rollingicon.ui.settings


import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.rollingicon.R
import com.example.rollingicon.utils.PreferencesHelper
import com.example.rollingicon.utils.Speed

class SettingsViewModel(private val application: Application) : AndroidViewModel(application) {
    // State for permission toggle
    val isPermissionGranted: MutableState<Boolean> = mutableStateOf(true)

    // State for icon size
    val iconSize: MutableState<Float> = mutableStateOf(
        PreferencesHelper.loadIconSize(application.applicationContext) // Load from SharedPreferences
    )

    // State for selected speed
    val selectedSpeed: MutableState<Int> = mutableStateOf(
        PreferencesHelper.loadIconSpeed(application.applicationContext).speedResId // Load from SharedPreferences
    )

    // Toggles for icon settings
    val togglesState: MutableState<Map<Int, Boolean>> = mutableStateOf(
        mapOf(
            R.string.text_touch to PreferencesHelper.loadCanTouch(application.applicationContext),
            R.string.text_drag to PreferencesHelper.loadCanDrag(application.applicationContext),
            R.string.text_explosion to PreferencesHelper.loadCanExplosion(application.applicationContext),
            R.string.text_sound to PreferencesHelper.loadCanSound(application.applicationContext)
        )
    )

    // Update the permission toggle state
    fun updatePermissionState(granted: Boolean) {
        isPermissionGranted.value = granted
    }

    // Update icon size and save it to SharedPreferences
    fun setIconSize(size: Float) {
        iconSize.value = size
        PreferencesHelper.saveIconSize(application.applicationContext, size) // Save to SharedPreferences
    }

    // Update selected speed and save it to SharedPreferences
    fun setSelectedSpeed(speedResId: Int) {
        selectedSpeed.value = speedResId
        PreferencesHelper.saveIconSpeed(application.applicationContext, Speed.fromResId(speedResId)) // Save to SharedPreferences
    }

    // Update toggle state and save it to SharedPreferences
    fun updateToggleState(toggleResId: Int, state: Boolean) {
        togglesState.value = togglesState.value.toMutableMap().apply {
            this[toggleResId] = state
        }

        // Save the toggle state for each key
        when (toggleResId) {
            R.string.text_touch -> PreferencesHelper.saveCanTouch(application.applicationContext, state)
            R.string.text_drag -> PreferencesHelper.saveCanDrag(application.applicationContext, state)
            R.string.text_explosion -> PreferencesHelper.saveCanExplosion(application.applicationContext, state)
            R.string.text_sound -> PreferencesHelper.saveCanSound(application.applicationContext, state)
        }
    }
}