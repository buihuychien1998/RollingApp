package com.buffalo.software.rolling.icon.live.wallpaper.utils

import com.buffalo.software.rolling.icon.live.wallpaper.R
import com.buffalo.software.rolling.icon.live.wallpaper.models.Language


val languages = listOf(
    Language("English", R.drawable.flag_us, "en"),
    Language("Ethiopia", R.drawable.flag_et, "am"),
    Language("عربي", R.drawable.flag_sa, "ar"),
    Language("български", R.drawable.flag_bg, "bg"),
    Language("বাংলা", R.drawable.flag_bd, "bn"),
    Language("Tiếng Việt", R.drawable.flag_vn, "vi"),
    Language("Català", R.drawable.flag_ca, "ca"),
    Language("čeština", R.drawable.flag_cz, "cs"),
    Language("Dansk", R.drawable.flag_dk, "da"),
    Language("Deutsch", R.drawable.flag_de, "de"),
    Language("ελληνικά", R.drawable.flag_gr, "el"),
    Language("Español", R.drawable.flag_es, "es"),
    Language("Français", R.drawable.flag_fr, "fr"),
    Language("भारतीय भाषा", R.drawable.flag_in, "hi"),
    Language("Hrvatski", R.drawable.flag_hr, "hr"),
    Language("Magyar", R.drawable.flag_hu, "hu"),
    Language("Indonesia", R.drawable.flag_id, "id"),
    Language("Italiano", R.drawable.flag_it, "it"),
    Language("日本語", R.drawable.flag_jp, "ja"),
    Language("ქართული", R.drawable.flag_ge, "ka"),
    Language("한국인", R.drawable.flag_kr, "ko"),
    Language("Lietuvių", R.drawable.flag_lt, "lt"),
    Language("Latviešu", R.drawable.flag_lv, "lv"),
    Language("Nederlands", R.drawable.flag_nl, "nl"),
    Language("Norsh", R.drawable.flag_no, "no"),
    Language("Polski", R.drawable.flag_pl, "pl"),
    Language("Português", R.drawable.flag_br, "pt"),
    Language("Română", R.drawable.flag_ro, "ro"),
    Language("Русский", R.drawable.flag_ru, "ru"),
    Language("Slovenský", R.drawable.flag_sk, "sk"),
    Language("Slovenski", R.drawable.flag_si, "sl"),
    Language("Српски", R.drawable.flag_rs, "sr"),
    Language("Svenska", R.drawable.flag_se, "sv"),
    Language("ภาษาไทย", R.drawable.flag_th, "th"),
    Language("Türkçe", R.drawable.flag_tr, "tr"),
    Language("Українська", R.drawable.flag_ua, "uk"),
    Language("中國人", R.drawable.flag_cn, "zh")
)


val defaultApps = listOf(
    "Phone", "Messages", "Contacts", "Browser", "Camera", "Photos", "Mail", "Calendar",
    "Clock", "Notes", "Youtube", "Whatsapp", "Facebook", "TikTok", "Netflix", "Snapchat",
    "Spotify", "Google Map", "Capcut", "Threads"
)

const val MAX_APP_ICONS_TO_LOAD = 25
const val TWEEN_DURATION = 300
const val TIME_DELAY = 300L
const val LAUNCH_COUNT = 2
const val PRIVACY_POLICY = "https://docs.google.com/document/d/1Hhy-PetB3kUe8FrHMR8Ci5OltASdNc1bpSf_tk4l4xg/edit?tab=t.0"
const val SHOW_AD = true
