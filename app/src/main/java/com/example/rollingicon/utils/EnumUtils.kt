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
    NORMAL(R.string.text_speed_normal, 0.85f),
    FAST(R.string.text_speed_fast, 0.98f),
    SLOW(R.string.text_speed_slow, 0.001f),
    CRAZY(R.string.text_speed_crazy, 1.15f);


    companion object {
        fun fromResId(resId: Int): Speed {
            return values().first { it.speedResId == resId }
        }
    }
}