package com.buffalo.software.rolling.icon.live.wallpaper.ui.background

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.buffalo.software.rolling.icon.live.wallpaper.R
import com.buffalo.software.rolling.icon.live.wallpaper.routes.AppRoutes
import com.buffalo.software.rolling.icon.live.wallpaper.theme.AppFont
import com.buffalo.software.rolling.icon.live.wallpaper.theme.clr_4664FF
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper
import com.buffalo.software.rolling.icon.live.wallpaper.utils.custom.SafeClick

@Composable
fun BackgroundSelectionScreen(navController: NavController) {
    val context = LocalContext.current

    val selectedBackground =
        remember { mutableStateOf(PreferencesHelper.getBackground(context).toIntOrNull()) }

    // Lắng nghe dữ liệu khi quay lại từ BackgroundDetailScreen
    val backStackEntry = navController.currentBackStackEntry
    LaunchedEffect(backStackEntry) {
        backStackEntry?.savedStateHandle?.getLiveData<Int>("selectedBackground")
            ?.observeForever { newBackground ->
                selectedBackground.value = newBackground
            }
    }

    val backgrounds = listOf(
        R.drawable.bg_1, R.drawable.bg_2, R.drawable.bg_3,
        R.drawable.bg_4, R.drawable.bg_5, R.drawable.bg_6,
        R.drawable.bg_7, R.drawable.bg_8, R.drawable.bg_9,
        R.drawable.bg_10, R.drawable.bg_11, R.drawable.bg_12,
        R.drawable.bg_13, R.drawable.bg_14, R.drawable.bg_15,
        R.drawable.bg_16, R.drawable.bg_17, R.drawable.bg_18,
        R.drawable.bg_19, R.drawable.bg_20
    )

    Box {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.bg_rolling_app),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                SafeClick(onClick = { navController.popBackStack() }) { enabled, onClick ->
                    IconButton(
                        onClick = onClick,
                        enabled = enabled,
                        modifier = Modifier.offset(x = (-16).dp)
                    ) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.ic_arrow_left),
                            contentDescription = "ic_arrow_left"
                        )
                    }
                }
                Text(
                    text = stringResource(id = R.string.text_background),
                    textAlign = TextAlign.Start,
                    fontFamily = AppFont.Grandstander,
                    color = Color.White,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = (-12).dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(backgrounds) { backgroundRes ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(210 / 316f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                navController.navigate("${AppRoutes.BackgroundDetail.route}/$backgroundRes")
                            }
                    ) {
                        Image(
                            painter = painterResource(backgroundRes),
                            contentDescription = "Background",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (backgroundRes == selectedBackground.value) {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .background(Color.Black.copy(alpha = 0.3f))
//                            )
//                            Icon(
//                                imageVector = Icons.Default.Check,
//                                contentDescription = "Selected",
//                                tint = Color.White,
//                                modifier = Modifier
//                                    .align(Alignment.TopEnd)
//                                    .padding(8.dp)
//                                    .size(24.dp)
//                            )
                            Box(
                                Modifier
                                    .padding(12.dp)
                                    .align(Alignment.TopEnd)) {
                                Image(
                                    modifier = Modifier
                                        .size(24.dp),
                                    painter = painterResource(R.drawable.ic_tick),
                                    contentDescription = "ic_arrow_left"
                                )
                            }

                        }
                    }
                }
            }
        }
    }

}

@Composable
fun BackgroundDetailScreen(navController: NavController, backgroundRes: Int) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = "Selected Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SafeClick(onClick = { navController.popBackStack() }) { enabled, onClick ->
                IconButton(
                    onClick = onClick, enabled = enabled, modifier = Modifier
                        .offset(x = (-16).dp)
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_arrow_left),
                        contentDescription = "ic_arrow_left"
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(id = R.string.text_save),
                textAlign = TextAlign.Start,
                fontFamily = AppFont.Grandstander,
                color = clr_4664FF,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                modifier = Modifier
                    .offset(x = (-12).dp)
                    .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                    .clickable {
                        PreferencesHelper.saveBackground(context, backgroundRes.toString())
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedBackground", backgroundRes)

                        navController.popBackStack()
                    }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
            )
        }
    }
}