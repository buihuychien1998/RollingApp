package com.example.rollingicon.utils

import com.example.rollingicon.R

enum class IconType {
    APP,
    IMAGE,
    VIDEO;

    fun getMimeType() = when (this) {
        APP -> "application/*" // Loại mime cho ứng dụng
        IMAGE -> "image/*" // Loại mime cho ảnh
        VIDEO -> "video/*" // Loại mime cho video
    }
}

enum class Speed(val speedResId: Int, val iconSpeedValue: Float) {
    NORMAL(R.string.text_speed_normal, 0.9f),
    FAST(R.string.text_speed_fast, 1.05f),
    SLOW(R.string.text_speed_slow, 0.4f),
    CRAZY(R.string.text_speed_crazy, 1.2f);


    companion object {
        fun fromResId(resId: Int): Speed {
            return entries.first { it.speedResId == resId }
        }
    }
}