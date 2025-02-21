package com.buffalo.software.rolling.icon.live.wallpaper.ui.feature

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.buffalo.software.rolling.icon.live.wallpaper.R
import com.buffalo.software.rolling.icon.live.wallpaper.routes.AppRoutes
import com.buffalo.software.rolling.icon.live.wallpaper.theme.AppFont
import com.buffalo.software.rolling.icon.live.wallpaper.theme.clr_2C323F
import com.buffalo.software.rolling.icon.live.wallpaper.theme.clr_4664FF
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.NativeAdViewCompose
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.native_feature
import com.buffalo.software.rolling.icon.live.wallpaper.ui.share_view_model.LocalRemoteConfig
import com.buffalo.software.rolling.icon.live.wallpaper.ui.share_view_model.RemoteConfigKeys

@Composable
fun FeaturesScreen(navController: NavController, viewModel: FeaturesViewModel = viewModel()) {
    val selectedFeatures by viewModel.selectedFeatures.collectAsState()
    val context = LocalContext.current
    val configValues = LocalRemoteConfig.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 📌 Header Image
            Image(
                painter = painterResource(id = R.drawable.ic_features_header),
                contentDescription = stringResource(R.string.text_features),
                modifier = Modifier
                    .width(244.dp)
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📌 Title & Subtitle
            Text(
                text = stringResource(R.string.text_features),
                fontFamily = AppFont.Grandstander,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                color = clr_4664FF
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.features_subtitle),
                fontFamily = AppFont.Grandstander,
                style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
                color = clr_2C323F
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 📌 Feature List with Different Icons
            val features = listOf(
                Triple(
                    stringResource(R.string.text_rolling_icon),
                    R.drawable.ic_rolling_icon,
                    "Rolling"
                ),
                Triple(stringResource(R.string.text_video_icon), R.drawable.ic_video_icon, "Video"),
                Triple(stringResource(R.string.text_image_icon), R.drawable.ic_image_icon, "Image")
            )

            features.forEach { (title, icon, key) ->
                FeatureItem(
                    title = title,
                    iconResId = icon, // Dynamic icon
                    isSelected = key in selectedFeatures,
                    onClick = { viewModel.toggleFeature(key) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        if (configValues[RemoteConfigKeys.NATIVE_FEATURE] == true) {
            NativeAdViewCompose(
                context = context,
                nativeID = native_feature,
                backgroundTint = android.graphics.Color.parseColor("#E7ECF2")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 📌 Buttons
        Button(
            onClick = {
                // Handle Next Click
                println("User selected: $selectedFeatures")
                // Navigate to next screen with selected features
                navController.navigate(AppRoutes.Home.route) {
                    popUpTo(AppRoutes.Feature.route) {
                        inclusive = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(clr_4664FF),
            shape = RoundedCornerShape(16.dp),
            enabled = selectedFeatures.isNotEmpty() // Disable if no features are selected
        ) {
            Text(
                text = stringResource(R.string.text_next),
                fontFamily = AppFont.Grandstander,
                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun FeatureItem(title: String, iconResId: Int, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Shadow effect
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Clickable effect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconResId), // Dynamic icon
                contentDescription = title,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontFamily = AppFont.Grandstander,
                style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 20.sp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = if (isSelected) R.drawable.ic_check else R.drawable.ic_uncheck),
                contentDescription = "Selected",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

