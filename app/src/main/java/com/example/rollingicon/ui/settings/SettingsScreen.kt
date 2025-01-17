package com.example.rollingicon.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.rollingicon.R
import com.example.rollingicon.routes.AppRoutes
import com.example.rollingicon.theme.AppFont
import com.example.rollingicon.theme.clr_2C323F
import com.example.rollingicon.theme.clr_4664FF
import com.example.rollingicon.theme.clr_96ACC4
import com.example.rollingicon.theme.clr_D5DEE8
import com.example.rollingicon.theme.clr_ECF4FF
import com.example.rollingicon.utils.PreferencesHelper
import com.example.rollingicon.utils.custom.CustomSwitch
import com.example.rollingicon.utils.custom.SafeClick
import com.example.rollingicon.utils.languages

@Composable
fun SettingsScreen(navController: NavController) {
    val settingsViewModel: SettingsViewModel = viewModel()


    Box {
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
            // Top Bar
            SettingsTopBar(navController)

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f) // Allow the scrollable content to take up remaining space
                    .verticalScroll(rememberScrollState()) // Enable vertical scrolling
            ) {
                Column {
                    // Grant Permission Toggle
                    PermissionToggle(settingsViewModel)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Language Section
                    LanguageSection(navController)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Icon Settings
                    IconSettings(settingsViewModel)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gravity Box Settings
                    GravityBoxSettings()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Information Section
                    InformationSection()

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsTopBar(navController: NavController) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        SafeClick(onClick = { navController.popBackStack() }) { enabled, onClick ->
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
        Text(
            text = stringResource(id = R.string.text_settings),
            textAlign = TextAlign.Start,
            fontFamily = AppFont.Grandstander,
            color = Color.White,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
            modifier = Modifier
                .weight(1f)
                .offset(x = (-12).dp),
        )
    }
}

@Composable
fun PermissionToggle(settingsViewModel: SettingsViewModel) {
    val isChecked by settingsViewModel.isPermissionGranted

    Text(
        text = stringResource(id = R.string.text_permission),
        color = Color.White,
        fontFamily = AppFont.Grandstander,
        style = TextStyle(fontSize = 16.sp),
    )
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.text_grant_write_setting_permission),
            color = clr_2C323F,
            fontFamily = AppFont.Grandstander,
            style = TextStyle(fontSize = 16.sp),
            modifier = Modifier.weight(1f)
        )
        CustomSwitch(
            checked = isChecked,
            onCheckedChange = { settingsViewModel.updatePermissionState(it) },
            trackWidth = 40.dp,
            trackHeight = 16.dp,
            thumbSize = 20.dp,
            checkedTrackColor = clr_4664FF,
            uncheckedTrackColor = clr_D5DEE8,
            thumbColor = clr_ECF4FF
        )
    }
}

@Composable
fun LanguageSection(navController: NavController) {
    val context = LocalContext.current
    val selectedLanguageCode = PreferencesHelper.getSelectedLanguage(context)
    // Find the language object that matches the stored code, default to the first language
    val currentLanguage = languages.find { it.code == selectedLanguageCode } ?: languages.first()
    Text(
        text = stringResource(id = R.string.text_language),
        color = Color.White,
        fontFamily = AppFont.Grandstander,
        style = TextStyle(fontSize = 16.sp),
    )
    Spacer(modifier = Modifier.height(12.dp))

    SafeClick(onClick = { navController.navigate(AppRoutes.Language.route) }) { enabled, onClick ->
        Button(
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(currentLanguage.flagResId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = currentLanguage.name,
                    color = clr_2C323F,
                    fontFamily = AppFont.Grandstander,
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.ic_arrow_right),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconSettings(settingsViewModel: SettingsViewModel) {
    // State variables
    val iconSize by settingsViewModel.iconSize
    val selectedSpeed by settingsViewModel.selectedSpeed
    val togglesState by settingsViewModel.togglesState

    Text(
        text = stringResource(id = R.string.text_icon),
        color = Color.White,
        fontFamily = AppFont.Grandstander,
        style = TextStyle(fontSize = 16.sp),
    )
    Spacer(modifier = Modifier.height(12.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Icon Size Slider
        Text(
            text = stringResource(id = R.string.text_size_setting),
            fontFamily = AppFont.Grandstander,
            style = TextStyle(fontSize = 16.sp),
            color = clr_2C323F
        )

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.fillMaxWidth() // Makes the Row span the entire width
        ) {
            Text(
                text = iconSize.toInt().toString(),
                fontFamily = AppFont.Grandstander,
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = clr_2C323F,
                modifier = Modifier.fillMaxWidth() // Makes Text take remaining horizontal space
            )
            Image(
                rememberAsyncImagePainter(R.drawable.img_icon_size),
                contentDescription = null,
                modifier = Modifier.size(60.dp),
            )
        }

        Slider(
            value = iconSize,
            onValueChange = { settingsViewModel.setIconSize(it) },
            valueRange = 20f..140f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = clr_4664FF,
                inactiveTrackColor = clr_D5DEE8
            ),
            modifier = Modifier
                .requiredWidth(LocalConfiguration.current.screenWidthDp.dp)
                .padding(horizontal = 24.dp),

            thumb = {
                Image(
                    rememberAsyncImagePainter(R.drawable.ic_thumb_slider),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Speed Options
        Text(
            text = stringResource(id = R.string.text_speed),
            fontFamily = AppFont.Grandstander,
            style = TextStyle(fontSize = 16.sp),
            color = clr_2C323F
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            listOf(
                R.string.text_speed_slow,
                R.string.text_speed_normal,
                R.string.text_speed_fast,
                R.string.text_speed_crazy
            ).forEach { resId ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            settingsViewModel.setSelectedSpeed(resId)
                        },
                        modifier = Modifier.size(24.dp) // Adjust size as needed
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                if (selectedSpeed == resId) R.drawable.ic_language_selected else R.drawable.ic_language_unselected
                            ),
                            contentDescription = if (selectedSpeed == resId) "Selected" else "Not Selected",
                            modifier = Modifier.size(24.dp) // Adjust size as needed
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = resId),
                        fontFamily = AppFont.Grandstander,
                        style = TextStyle(fontSize = 14.sp),
                        color = clr_96ACC4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Toggles
        togglesState.forEach { (resId, state) ->
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = resId),
                    fontFamily = AppFont.Grandstander,
                    style = TextStyle(fontSize = 16.sp),
                    color = clr_2C323F,
                    modifier = Modifier.weight(1f)
                )

                CustomSwitch(
                    checked = state,
                    onCheckedChange = { settingsViewModel.updateToggleState(resId, it) },
                    trackWidth = 40.dp,
                    trackHeight = 16.dp,
                    thumbSize = 20.dp,
                    checkedTrackColor = clr_4664FF,
                    uncheckedTrackColor = clr_D5DEE8,
                    thumbColor = clr_ECF4FF
                )
            }
        }
    }
}

@Composable
fun GravityBoxSettings() {
    Text(
        text = stringResource(id = R.string.text_gravity_box),
        color = Color.White,
        fontFamily = AppFont.Grandstander,
        style = TextStyle(fontSize = 16.sp),
    )
    Spacer(modifier = Modifier.height(12.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.text_size),
                color = clr_2C323F,
                fontFamily = AppFont.Grandstander,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = rememberAsyncImagePainter(R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(152.dp), // Matches the height of the Image
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Top 0%",
                    color = clr_96ACC4,
                    fontFamily = AppFont.Grandstander,
                    style = TextStyle(fontSize = 14.sp),
                )
                Text(
                    text = "Left 0%",
                    color = clr_96ACC4,
                    fontFamily = AppFont.Grandstander,
                    style = TextStyle(fontSize = 14.sp),
                )
                Text(
                    text = "Right 0%",
                    color = clr_96ACC4,
                    fontFamily = AppFont.Grandstander,
                    style = TextStyle(fontSize = 14.sp),
                )
                Text(
                    text = "Bottom 0%",
                    color = clr_96ACC4,
                    fontFamily = AppFont.Grandstander,
                    style = TextStyle(fontSize = 14.sp),
                )

            }
            Image(
                painter = rememberAsyncImagePainter(R.drawable.img_icon_gravity_box),
                contentDescription = null,
                modifier = Modifier.size(width = 71.dp, height = 152.dp)
            )
        }
    }


}

@Composable
fun InformationSection() {
    Text(
        text = stringResource(id = R.string.text_information),
        color = Color.White,
        fontFamily = AppFont.Grandstander,
        style = TextStyle(fontSize = 16.sp),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        listOf(
            R.string.text_privacy_policy,
            R.string.text_about_ads
        ).forEach { resId ->
            TextButton(onClick = { /* Handle click */ }) {
                Text(
                    text = stringResource(id = resId),
                    color = clr_2C323F,
                    fontFamily = AppFont.Grandstander,
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsPreview() {
    SettingsScreen(NavController(LocalContext.current))
}

