package com.buffalo.software.rolling.icon.live.wallpaper.utils.custom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    trackWidth: Dp = 60.dp,
    trackHeight: Dp = 12.dp,
    thumbSize: Dp = 24.dp, // Thumb size larger than track
    checkedTrackColor: Color = Color.Green,
    uncheckedTrackColor: Color = Color.Red,
    thumbColor: Color = Color.White,
) {
    var checkedState by remember { mutableStateOf(checked) }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .size(width = trackWidth, height = thumbSize) // Box height matches thumb
            .pointerInput(Unit) {
                detectTapGestures { onCheckedChange(!checkedState) }
            }
    ) {
        // Custom track
        Canvas(
            modifier = Modifier
                .size(width = trackWidth, height = trackHeight)
                .align(Alignment.Center) // Center the track within the box
        ) {
            drawCustomTrack(
                color = if (checkedState) checkedTrackColor else uncheckedTrackColor,
                width = size.width,
                height = size.height
            )
        }

        // Custom thumb
        Box(
            modifier = Modifier
                .offset(
                    x = if (checkedState) trackWidth - thumbSize else 0.dp
                )
                .size(thumbSize)
                .background(color = thumbColor, shape = CircleShape)
        )
    }

    // Update state externally
    LaunchedEffect(checked) {
        checkedState = checked
    }

    // Trigger change
    DisposableEffect(checkedState) {
        onCheckedChange(checkedState)
        onDispose { }
    }
}

// Function to draw the track
private fun DrawScope.drawCustomTrack(
    color: Color,
    width: Float,
    height: Float
) {
    drawRoundRect(
        color = color,
        size = androidx.compose.ui.geometry.Size(width, height),
        cornerRadius = CornerRadius(height / 2)
    )
}