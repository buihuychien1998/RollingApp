package com.example.rollingicon.ui.ads

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BannerShimmerEffect() {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.9f),
        Color.White.copy(alpha = 0.5f),
        Color.White.copy(alpha = 0.9f)
    )

    val transition = rememberInfiniteTransition()
    val shimmerX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Large enough to create smooth animation
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp) // Approximate height of banner ad
//            .clip(RoundedCornerShape(4.dp)) // Smooth rounded corners
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(shimmerX, 0f),
                    end = Offset(shimmerX + 500f, 0f) // Moves the shimmer effect
                )
            )
    )
}

@Composable
fun NativeShimmerEffect(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.9f),
        Color.White.copy(alpha = 0.5f),
        Color.White.copy(alpha = 0.9f)
    )

    val transition = rememberInfiniteTransition()
    val shimmerX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Large enough to create smooth animation
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier =modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(shimmerX, 0f),
                    end = Offset(shimmerX + 500f, 0f) // Moves the shimmer effect
                )
            )
    )
}
