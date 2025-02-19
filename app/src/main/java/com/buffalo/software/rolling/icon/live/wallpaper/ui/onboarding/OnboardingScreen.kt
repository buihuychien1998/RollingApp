package com.buffalo.software.rolling.icon.live.wallpaper.ui.onboarding

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.native_onboarding
import com.buffalo.software.rolling.icon.live.wallpaper.utils.PreferencesHelper
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val onboardingItems = listOf(
        OnboardingItem(
            R.drawable.onboarding_image1,
            stringResource(R.string.onboarding_title_1),
            stringResource(R.string.onboarding_desc_1)
        ),
        OnboardingItem(
            R.drawable.onboarding_image2,
            stringResource(R.string.onboarding_title_2),
            stringResource(R.string.onboarding_desc_2)
        ),
        OnboardingItem(
            R.drawable.onboarding_image3,
            stringResource(R.string.onboarding_title_3),
            stringResource(R.string.onboarding_desc_3)
        )
    )

    val pagerState = rememberPagerState(pageCount = { onboardingItems.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState, modifier = Modifier.weight(1f)
        ) { page ->
            val item = onboardingItems[page]
            OnboardingSlide(item)
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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

            // Navigation Buttons
            Button(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage < onboardingItems.size - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            PreferencesHelper.setOnboardingDone(context = context, value = true)
                            // Navigate to Home Screen
                            navController.navigate(AppRoutes.Home.route) {
                                popUpTo(AppRoutes.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pagerState.currentPage < onboardingItems.size - 1) Color.White else Color(0xFF4664FF),
                    contentColor = if (pagerState.currentPage < onboardingItems.size - 1) Color(0xFF4664FF) else Color.White
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
        NativeAdViewCompose(
            context = LocalContext.current,
            nativeID = native_onboarding,
        )
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

