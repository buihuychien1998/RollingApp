package com.buffalo.software.rolling.icon.live.wallpaper.ui.onboarding

import android.graphics.Color.parseColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
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
import com.buffalo.software.rolling.icon.live.wallpaper.models.OnboardingItem
import com.buffalo.software.rolling.icon.live.wallpaper.routes.AppRoutes
import com.buffalo.software.rolling.icon.live.wallpaper.theme.AppFont
import com.buffalo.software.rolling.icon.live.wallpaper.theme.clr_2C323F
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.NativeAdViewCompose
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.native_full_screen
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.native_full_screen2
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.native_full_screen2_2f
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.native_full_screen_2f
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.native_onboarding_2
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.native_onboarding_2_1
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.native_onboarding_2_2
import com.buffalo.software.rolling.icon.live.wallpaper.utils.LAUNCH_COUNT
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current

    val launchCount = remember { PreferencesHelper.getLaunchCount(context) }

    val (nativeAdId, fallbackNativeID) = remember(launchCount) {
        when {
            launchCount == 1 -> native_full_screen_2f to native_full_screen
            launchCount >= LAUNCH_COUNT -> native_full_screen2_2f to native_full_screen2
            else -> native_full_screen2_2f to native_full_screen2
        }
    }
    val fullOnboardingItems = listOf(
        OnboardingItem(
            R.drawable.onboarding_image1,
            stringResource(R.string.onboarding_title_1),
            stringResource(R.string.onboarding_desc_1),
            native_onboarding_2_1
        ),
        OnboardingItem(
            R.drawable.onboarding_image2,
            stringResource(R.string.onboarding_title_2),
            stringResource(R.string.onboarding_desc_2),
            native_onboarding_2
        ),
        OnboardingItem(
            R.drawable.onboarding_image2,
            stringResource(R.string.onboarding_title_2),
            stringResource(R.string.onboarding_desc_2),
            nativeAdId
        ),
        OnboardingItem(
            R.drawable.onboarding_image3,
            stringResource(R.string.onboarding_title_3),
            stringResource(R.string.onboarding_desc_3),
            native_onboarding_2_2
        )
    )

    val onboardingItems = remember(launchCount) {
        if (launchCount >= LAUNCH_COUNT) fullOnboardingItems.dropLast(1) else fullOnboardingItems
    }

    val pagerState = rememberPagerState(pageCount = { onboardingItems.size })
    val scope = rememberCoroutineScope()

    // ✅ Dùng rememberSaveable để lưu quảng cáo khi màn hình bị hủy
    val adStates = remember { mutableStateMapOf<Int, NativeAd?>() }


    val reloadTriggers = remember { mutableStateMapOf<Int, MutableState<Boolean>>() }

    // ✅ Khởi tạo reloadTrigger cho từng trang
    onboardingItems.indices.forEach { page ->
        reloadTriggers.putIfAbsent(page, mutableStateOf(true))
    }

    // ✅ Khi người dùng đổi trang, nếu quảng cáo chưa có, yêu cầu tải quảng cáo mới
//    LaunchedEffect(pagerState.currentPage) {
//        val currentPage = pagerState.currentPage
//        if (adStates[currentPage] == null) {
//            reloadTriggers[currentPage]?.value = true
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val item = onboardingItems[page]
                val reloadTrigger = reloadTriggers[page] ?: mutableStateOf(false)

                if (page == 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .then(
                                if (launchCount >= LAUNCH_COUNT) {
                                    Modifier.pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val dragAmount =
                                                    event.changes
                                                        .firstOrNull()
                                                        ?.positionChange()?.x
                                                        ?: 0f

                                                if (dragAmount < -50) { // Swipe left detected
                                                    com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper.setOnboardingDone(
                                                        context = context,
                                                        value = launchCount >= com.buffalo.software.rolling.icon.live.wallpaper.utils.LAUNCH_COUNT
                                                    )
                                                    navController.navigate(com.buffalo.software.rolling.icon.live.wallpaper.routes.AppRoutes.Home.route) {
                                                        popUpTo(com.buffalo.software.rolling.icon.live.wallpaper.routes.AppRoutes.Onboarding.route) {
                                                            inclusive = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else Modifier

                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        NativeAdViewCompose(
                            context = context,
                            nativeID = item.nativeAdId,
                            fallbackNativeID = fallbackNativeID,
                            existingAd = adStates[page],
                            reloadTrigger = reloadTrigger,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            layoutResId = R.layout.native_full_screen,
                            onAdLoaded = {
                                adStates[page] = it
                                reloadTriggers[page]?.value = false
                            },
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OnboardingSlide(item)
                        }

                        // ✅ Nếu đã có quảng cáo, hiển thị ngay
                        NativeAdViewCompose(
                            context = context,
                            nativeID = item.nativeAdId,
                            existingAd = adStates[page],
                            reloadTrigger = reloadTrigger,
                            onAdLoaded = {
                                adStates[page] = it // ✅ Lưu ad để không tải lại
                                reloadTriggers[page]?.value = false // ✅ Không cần reload nữa
                            },
                            backgroundTint = parseColor("#E7ECF2")
                        )
                    }
                }

            }

            if (pagerState.currentPage != 2) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .offset(y = (-272).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Indicators (dot progress)
                    Row(
                        Modifier.wrapContentSize(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        repeat(onboardingItems.size) { index ->
                            Indicator(isSelected = pagerState.currentPage == index)
                        }
                    }

                    // ✅ Nút điều hướng cố định
                    Button(
                        onClick = {
                            scope.launch {
                                if (pagerState.currentPage < onboardingItems.size - 1) {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                } else {
                                    PreferencesHelper.setOnboardingDone(
                                        context = context,
                                        value = launchCount >= LAUNCH_COUNT
                                    )
                                    navController.navigate(AppRoutes.Home.route) {
                                        popUpTo(AppRoutes.Onboarding.route) { inclusive = true }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pagerState.currentPage < onboardingItems.size - 1) Color.White else Color(
                                0xFF4664FF
                            ),
                            contentColor = if (pagerState.currentPage < onboardingItems.size - 1) Color(
                                0xFF4664FF
                            ) else Color.White
                        ),
                        border = if (pagerState.currentPage < onboardingItems.size - 1) {
                            BorderStroke(1.dp, Color(0xFF4664FF))
                        } else {
                            null
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            text = stringResource(
                                if (pagerState.currentPage < onboardingItems.size - 1) R.string.text_next else R.string.text_start
                            ),
                            fontFamily = AppFont.Grandstander,
                            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun OnboardingSlide(item: OnboardingItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = item.title,
            textAlign = TextAlign.Center,
            fontFamily = AppFont.Grandstander,
            color = clr_2C323F,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.description, textAlign = TextAlign.Center,
            fontFamily = AppFont.Grandstander,
            color = clr_2C323F,
            style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
        )
    }
}

@Composable
fun Indicator(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(if (isSelected) 10.dp else 8.dp)
            .background(
                if (isSelected) Color.Blue else Color.Gray, shape = MaterialTheme.shapes.small
            )
    )
}

