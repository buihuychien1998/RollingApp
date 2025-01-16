package com.example.rollingicon.ui.language

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.example.rollingicon.R
import com.example.rollingicon.models.Language
import com.example.rollingicon.theme.AppFont
import com.example.rollingicon.theme.clr_2C323F
import com.example.rollingicon.theme.clr_4664FF
import com.example.rollingicon.utils.custom.SafeClick

@Composable
fun LanguageScreen(
    languages: List<Language>,
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onConfirm: () -> Unit,
    showBackButton: Boolean, // New parameter to control the back button
    onBackPressed: (() -> Unit)? = null // Callback for back button action
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = rememberAsyncImagePainter(R.drawable.bg_rolling_app),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Top bar with title and confirm icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Show back button if required
                if (showBackButton) {
                    SafeClick(onClick = { onBackPressed?.invoke() }) { enabled, onClick ->
                        IconButton(
                            onClick = onClick,
                            enabled = enabled,
                            modifier = Modifier
                                .offset(x = (-16).dp)
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(24.dp),
                                painter = rememberAsyncImagePainter(R.drawable.ic_arrow_left),
                                contentDescription = "ic_arrow_left"
                            )
                        }
                    }
                }

                Text(
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = if (showBackButton) (-12).dp else 0.dp),
                    text = stringResource(id = R.string.text_language),
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    fontFamily = AppFont.Grandstander,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                SafeClick(onClick = onConfirm) { enabled, onClick ->
                    IconButton(
                        onClick = onClick,
                        enabled = enabled,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm",
                            tint = Color.White
                        )
                    }
                }
            }

            // Language list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(languages) { language ->
                    LanguageItem(
                        language = language,
                        isSelected = language == selectedLanguage,
                        onSelect = { onLanguageSelected(language) }
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isSelected) clr_4664FF else Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag image
            Image(
                painter = rememberAsyncImagePainter(language.flagResId),
                contentDescription = language.name,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Language name
            Text(
                text = language.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = clr_2C323F
                ),
                modifier = Modifier.weight(1f)
            )

            // Selection image
            Image(
                painter = painterResource(
                    id = if (isSelected) R.drawable.ic_language_selected else R.drawable.ic_language_unselected
                ),
                contentDescription = if (isSelected) "Selected" else "Not Selected",
                modifier = Modifier.size(24.dp) // Adjust size as needed
            )
        }
    }
}

// Preview function
@Preview(showBackground = true)
@Composable
fun PreviewLanguageSelectionScreen() {
    val languages = listOf(
        Language("English", R.drawable.flag_us, "en"),
        Language("Hindi", R.drawable.flag_in, "hi"),
        Language("Urdu", R.drawable.flag_pk, "ur"),
        Language("Arabic", R.drawable.flag_ae, "ar"),
        Language("Portuguese", R.drawable.flag_br, "pt"),
        Language("German", R.drawable.flag_de, "de")
    )
    var selectedLanguage by remember { mutableStateOf(languages.first()) }

    LanguageScreen(
        languages = languages,
        selectedLanguage = selectedLanguage,
        onLanguageSelected = { selectedLanguage = it },
        onConfirm = { /* Confirm action */ },
        showBackButton = true, // Show back button if we are navigating from another screen
        onBackPressed = {

        }
    )
}
