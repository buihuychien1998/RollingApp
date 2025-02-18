package com.buffalo.software.rolling.icon.live.wallpaper.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.buffalo.software.rolling.icon.live.wallpaper.R

object AppFont {
    val Grandstander = FontFamily(
        Font(R.font.grandstander_regular),
        Font(R.font.grandstander_italic, style = FontStyle.Italic),
        Font(R.font.grandstander_medium, FontWeight.Medium),
        Font(R.font.grandstander_medium_italic, FontWeight.Medium, style = FontStyle.Italic),
        Font(R.font.grandstander_bold, FontWeight.Bold),
        Font(R.font.grandstander_bold_italic, FontWeight.Bold, style = FontStyle.Italic)
    )
}