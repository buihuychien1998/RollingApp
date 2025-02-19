package com.buffalo.software.rolling.icon.live.wallpaper.utils

import android.content.Context
import android.content.SharedPreferences
import com.buffalo.software.rolling.icon.live.wallpaper.models.AppIcon
import com.buffalo.software.rolling.icon.live.wallpaper.models.Language
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PreferencesHelper {
    private const val PREFS_NAME = "AppPreferences"
    private const val APP_ICONS_KEY = "APP_ICONS_KEY"
    private const val IMAGE_ICONS_KEY = "IMAGE_ICONS_KEY"
    private const val VIDEO_ICONS_KEY = "VIDEO_ICONS_KEY"
    private const val FIRST_LOAD_APP_ICON_KEY = "FIRST_LOAD_APP_ICON_KEY"
    private const val LANGUAGE_KEY = "selected_language"
    private const val LFO_KEY = "LFO_KEY"
    private const val ONBOARDING_DONE_KEY = "ONBOARDING_DONE_KEY"
    private const val PREF_LAUNCH_COUNT = "launch_count"
    const val ICON_SIZE_KEY = "icon_size"
    const val ICON_SPEED_KEY = "icon_speed"
    const val CAN_TOUCH_KEY = "can_touch"
    const val CAN_DRAG_KEY = "can_drag"
    const val CAN_EXPLOSION_KEY = "can_explosion"
    const val CAN_SOUND_KEY = "can_sound"

    // Initialize SharedPreferences
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Generic PUT method
    private fun <T> put(context: Context, key: String, value: T) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()

        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }

        editor.apply()
    }

    // Generic GET method with default value
    fun <T> get(context: Context, key: String, defaultValue: T): T {
        val prefs = getPreferences(context)

        return when (defaultValue) {
            is String -> prefs.getString(key, defaultValue) as T
            is Int -> prefs.getInt(key, defaultValue) as T
            is Boolean -> prefs.getBoolean(key, defaultValue) as T
            is Float -> prefs.getFloat(key, defaultValue) as T
            is Long -> prefs.getLong(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    // Function to load icons from SharedPreferences
    fun loadIcons(context: Context, key: String): MutableList<AppIcon> {
        val iconsJson: String = get(context, key, "")
        return if (iconsJson.isEmpty()) {
            mutableListOf() // Return empty list if no icons stored
        } else {
            val gson = Gson()
            val iconType = object : TypeToken<MutableList<AppIcon>>() {}.type
            return gson.fromJson(iconsJson, iconType)
        }
    }

    fun loadAllIconsFromPreferences(context: Context): MutableList<AppIcon> {
        return (loadAppIconsFromPreferences(context) + loadImageIconsFromPreferences(context) + loadVideoIconsFromPreferences(context)).toMutableList()
    }

    fun loadSelectedIconsFromPreferences(context: Context): MutableList<AppIcon> {
        return (loadAppIconsFromPreferences(context)
                + loadImageIconsFromPreferences(context).filter { it.selected }
                + loadVideoIconsFromPreferences(context).filter { it.selected }).toMutableList()
    }

    fun loadAppIconsFromPreferences(context: Context): MutableList<AppIcon> {
        return loadIcons(context, APP_ICONS_KEY)
    }

    fun loadImageIconsFromPreferences(context: Context): MutableList<AppIcon> {
        return loadIcons(context, IMAGE_ICONS_KEY)
    }

    fun loadVideoIconsFromPreferences(context: Context): MutableList<AppIcon> {
        return loadIcons(context, VIDEO_ICONS_KEY)
    }

    // Function to save icons to SharedPreferences
    fun saveIconsToPreferences(context: Context, key: String, iconsList: MutableList<AppIcon>) {
        val gson = Gson()
        val iconsJson = gson.toJson(iconsList)
        put(context, key, iconsJson)
    }

    fun saveAppIconsFromPreferences(context: Context, iconsList: MutableList<AppIcon>){
        saveIconsToPreferences(context, APP_ICONS_KEY, iconsList)
    }

    fun saveImageIconsFromPreferences(context: Context, iconsList: MutableList<AppIcon>){
        saveIconsToPreferences(context, IMAGE_ICONS_KEY, iconsList)
    }

    fun saveVideoIconsFromPreferences(context: Context, iconsList: MutableList<AppIcon>){
        saveIconsToPreferences(context, VIDEO_ICONS_KEY, iconsList)
    }

    fun saveSelectedLanguage(context: Context, language: Language) {
        put(context, LANGUAGE_KEY, language.code)
    }

    fun getSelectedLanguage(context: Context): String {
        return get(context, LANGUAGE_KEY, "en") // Default size 75f
    }

    fun fromSetting(context: Context): Boolean {
        return get(context, LFO_KEY, false)
    }

    fun setLFODone(context: Context, value: Boolean) {
        put(context, LFO_KEY, value)
    }

    fun getLaunchCount(context: Context): Int {
        return get(context, PREF_LAUNCH_COUNT, 0)
    }

    fun increaseLaunchCount(context: Context) {
        val newCount = getLaunchCount(context) + 1
        println("increaseLaunchCount $newCount")
        put(context, PREF_LAUNCH_COUNT, newCount)
    }


    fun isOnboardingDone(context: Context): Boolean {
        return get(context, ONBOARDING_DONE_KEY, false)
    }

    fun setOnboardingDone(context: Context, value: Boolean) {
        put(context, ONBOARDING_DONE_KEY, value)
    }

    fun isFirstLoadIcon(context: Context): Boolean {
        val isFirstLoad = get(context, FIRST_LOAD_APP_ICON_KEY, true)

        if (isFirstLoad) {
            // Set the first load flag to false after checking
            put(context, FIRST_LOAD_APP_ICON_KEY, false)
        }

        return isFirstLoad
    }

    // Function to save individual settings
    fun saveIconSize(context: Context, size: Float) {
        put(context, ICON_SIZE_KEY, size)
    }

    fun saveIconSpeed(context: Context, speed: Speed) {
        put(context, ICON_SPEED_KEY, speed.name) // Saving as String (enum name)
    }

    fun saveCanTouch(context: Context, canTouch: Boolean) {
        put(context, CAN_TOUCH_KEY, canTouch)
    }

    fun saveCanDrag(context: Context, canDrag: Boolean) {
        put(context, CAN_DRAG_KEY, canDrag)
    }

    fun saveCanExplosion(context: Context, canExplosion: Boolean) {
        put(context, CAN_EXPLOSION_KEY, canExplosion)
    }

    fun saveCanSound(context: Context, canSound: Boolean) {
        put(context, CAN_SOUND_KEY, canSound)
    }

    // Function to load individual settings
    fun loadIconSize(context: Context): Float {
        return get(context, ICON_SIZE_KEY, 20f)
    }

    fun loadIconSpeed(context: Context): Speed {
        val speedName: String = get(context, ICON_SPEED_KEY, Speed.NORMAL.name)
        return Speed.valueOf(speedName) // Convert saved string to enum
    }

    fun loadCanTouch(context: Context): Boolean {
        return get(context, CAN_TOUCH_KEY, true) // Default true
    }

    fun loadCanDrag(context: Context): Boolean {
        return get(context, CAN_DRAG_KEY, true) // Default true
    }

    fun loadCanExplosion(context: Context): Boolean {
        return get(context, CAN_EXPLOSION_KEY, true) // Default true
    }

    fun loadCanSound(context: Context): Boolean {
        return get(context, CAN_SOUND_KEY, true) // Default true
    }
}