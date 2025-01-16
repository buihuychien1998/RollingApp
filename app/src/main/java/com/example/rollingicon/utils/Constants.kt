package com.example.rollingicon.utils

import com.example.rollingicon.R
import com.example.rollingicon.models.Language


val languages = listOf(
    Language("English", R.drawable.flag_us, "en"),
    Language("Hindi", R.drawable.flag_in, "hi"),
    Language("Urdu", R.drawable.flag_pk, "ur"),
    Language("Arabic", R.drawable.flag_ae, "ar"),
    Language("Portuguese", R.drawable.flag_br, "pt"),
    Language("German", R.drawable.flag_de, "de")
)

val defaultApps = listOf(
    "Phone", "Messages", "Contacts", "Browser", "Camera", "Photos", "Mail", "Calendar",
    "Clock", "Notes", "Youtube", "Whatsapp", "Facebook", "TikTok", "Netflix", "Snapchat",
    "Spotify", "Google Map", "Capcut", "Threads"
)

const val MAX_APP_ICONS_TO_LOAD = 25
const val TWEEN_DURATION = 300
const val TIME_DELAY = 300L
