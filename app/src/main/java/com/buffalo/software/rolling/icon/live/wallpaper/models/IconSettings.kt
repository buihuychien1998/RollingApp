package com.buffalo.software.rolling.icon.live.wallpaper.models

data class IconSettings(
    val iconSize: Float,
    val iconSpeed: Float,
    val canTouch: Boolean,
    val canDrag: Boolean,
    val canExplosion: Boolean,
    val canSound: Boolean
)