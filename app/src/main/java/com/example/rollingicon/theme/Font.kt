package com.example.rollingicon.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.rollingicon.R

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