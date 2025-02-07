package com.example.rollingicon.ui.onboarding

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rollingicon.R
import com.example.rollingicon.models.OnboardingItem
import com.example.rollingicon.routes.AppRoutes
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(navController: NavController) {
    val onboardingItems = listOf(
        OnboardingItem(
            R.drawable.onboarding_image1,
            "Unique and interactive experiences",
            "Enjoy a unique and interactive interface that brings creativity to your home screen."
        ),
        OnboardingItem(
            R.drawable.onboarding_image2,
            "Animated home screen icon",
            "Enjoy a more dynamic home screen with animated icons."
        ),
        OnboardingItem(
            R.drawable.onboarding_image3,
            "Customize icon appearance and effects",
            "Create interactive and customized icon animations."
        )
    )

    val pagerState = rememberPagerState(pageCount = { onboardingItems.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val item = onboardingItems[page]
            OnboardingSlide(item)
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Indicators (dot progress)
            Row(
                Modifier
                    .wrapContentSize(),
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
                            // Navigate to Home Screen
                            navController.navigate(AppRoutes.Home.route) {
                                popUpTo(AppRoutes.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                },
            ) {
                Text(if (pagerState.currentPage < onboardingItems.size - 1) "Next" else "Start")
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
        Text(text = item.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = item.description, fontSize = 16.sp, color = Color.Gray)
    }
}

@Composable
fun Indicator(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(if (isSelected) 10.dp else 8.dp)
            .background(
                if (isSelected) Color.Blue else Color.Gray,
                shape = MaterialTheme.shapes.small
            )
    )
}

