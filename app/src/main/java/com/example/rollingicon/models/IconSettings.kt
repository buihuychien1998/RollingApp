package com.example.rollingicon.models

data class IconSettings(
    val iconSize: Float,
    val iconSpeed: Float,
    val canTouch: Boolean,
    val canDrag: Boolean,
    val canExplosion: Boolean,
    val canSound: Boolean
)