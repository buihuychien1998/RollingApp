package com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.tracking

object FirebaseAnalyticsEvents {

    // 📌 Screen View Events
    const val SCREEN_SPLASH_VIEW = "screen_splash_view"
    const val SCREEN_LANGUAGE_VIEW = "screen_language_view"
    const val SCREEN_ONBOARDING_1_VIEW = "screen_onboarding_1_view"
    const val SCREEN_ONBOARDING_2_VIEW = "screen_onboarding_2_view"
    const val SCREEN_ONBOARDING_3_VIEW = "screen_onboarding_3_view"
    const val SCREEN_HOME_VIEW = "screen_home_view"
    const val SCREEN_PREVIEW_VIEW = "screen_preview_view"
    const val SCREEN_ADD_APPLICATION_VIEW = "screen_add_application_view"
    const val SCREEN_ADD_VIDEO_VIEW = "screen_add_video_view"
    const val SCREEN_ADD_PHOTO_VIEW = "screen_add_photo_view"
    const val SCREEN_SETTINGS_VIEW = "screen_settings_view"

    // 📌 User Interaction Events
    const val CLICK_SETTINGS_ICON = "click_settings_icon"
    const val CLICK_PREVIEW = "click_preview"
    const val CLICK_NEXT_ICON = "click_next_icon"
    const val CLICK_ADD_ICON = "click_add_icon"
    const val CLICK_SET_WALLPAPER = "click_set_wallpaper"
    const val CLICK_ADD_APPLICATION = "click_add_application"
    const val CLICK_ADD_VIDEO = "click_add_video"
    const val CLICK_ADD_VIDEO_ICON = "click_add_video_icon"
    const val CLICK_ADD_PHOTOS = "click_add_photos"
    const val CLICK_ADD_PHOTO_ICON = "click_add_photo_icon"
    const val CLICK_SEARCH = "click_search"
    const val CLICK_CLEAR_APPLICATION = "click_clear_application"
    const val CLICK_CLEAR_PHOTO = "click_clear_photo"
    const val CLICK_CLEAR_VIDEO = "click_clear_video"

    // 📌 Custom User Actions
    const val USER_SELECT_SPEED = "user_select_speed"
    const val USER_ADJUST_SIZE = "user_adjust_size"

    // 📌 Parameters
    const val PARAM_SPEED_VALUE = "speed_value"
    const val PARAM_SIZE_VALUE = "size_value"

    // 📌 Possible Speed Values
    const val SPEED_SLOW = "slow"
    const val SPEED_NORMAL = "normal"
    const val SPEED_FAST = "fast"
    const val SPEED_CRAZY = "crazy"
}
