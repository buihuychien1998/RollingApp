package com.example.rollingicon.ui.loading

import android.graphics.DiscretePathEffect
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import androidx.compose.animation.core.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposePathEffect
import com.example.rollingicon.utils.custom.IndicatorDefaults.DefaultGradientColors
import com.example.rollingicon.utils.custom.IndicatorDefaults.Size
import com.example.rollingicon.utils.custom.IndicatorStyle
import kotlin.math.cos
import kotlin.math.sin

private const val DEGREE_TO_RADIAN = Math.PI / 180

@Composable
fun LoadingScreen() {
    Dialog(
        onDismissRequest = { }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center
        ) {
            GooeyProgressIndicator()
        }
    }
}

@Composable
fun GooeyProgressIndicator(
    modifier: Modifier = Modifier,
    style: IndicatorStyle = IndicatorStyle.Filled
) {

    val staticCircleCount = 8
    val segmentCount = 20

    val pathMeasure = remember {
        PathMeasure()
    }

    val pathDynamic = remember { Path() }
    val pathStatic = remember { Path() }

    val cornerPathEffect = remember {
        PathEffect.cornerPathEffect(50f)
    }

    val target = 360f

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = target,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2500
                target * 0.8f at 1300
                target * 0.1f at 150 with FastOutLinearInEasing
                target * 0.1f at 150
            }
        ), label = ""
    )

    if(style == IndicatorStyle.Filled){
        FilledGooeyIndicatorImpl(
            modifier = modifier,
            pathStatic = pathStatic,
            pathDynamic = pathDynamic,
            staticCircleCount = staticCircleCount,
            angle = angle,
            pathMeasure = pathMeasure,
            cornerPathEffect = cornerPathEffect,
            segmentCount = segmentCount
        )
    }else {
        StrokeGooeyImpl(
            modifier = modifier,
            pathStatic = pathStatic,
            pathDynamic = pathDynamic,
            staticCircleCount = staticCircleCount,
            angle = angle,
            pathMeasure = pathMeasure,
            cornerPathEffect = cornerPathEffect,
            segmentCount = segmentCount
        )
    }
}

@Composable
private fun FilledGooeyIndicatorImpl(
    modifier: Modifier,
    pathStatic: Path,
    pathDynamic: Path,
    staticCircleCount: Int,
    angle: Float,
    pathMeasure: PathMeasure,
    cornerPathEffect: PathEffect,
    segmentCount: Int
) {

    val paint = remember {
        Paint().apply {
            color = Color.Red
        }
    }

    var isPaintSetUp by remember {
        mutableStateOf(false)
    }


    Canvas(
        modifier
            .progressSemantics()
            .size(Size)
    ) {

        var canvasWidth = size.width
        var canvasHeight = size.height

        if (canvasHeight < canvasWidth) {
            canvasWidth = canvasHeight
        } else {
            canvasHeight = canvasWidth
        }

        val width = canvasWidth
        val height = canvasHeight

        val dynamicCircleRadius = width.coerceAtMost(height) * .15f

        // This is distance of center of circles to center of progress indicator
        val radiusDynamicContainer = (width / 2 - dynamicCircleRadius)

        if (pathStatic.isEmpty) {

            val circleRadius = width.coerceAtMost(height) * .1f

            for (i in 0..360 step 360 / staticCircleCount) {
                val rect = Rect(
                    center = Offset(
                        x = (center.x + radiusDynamicContainer * cos(i * DEGREE_TO_RADIAN)).toFloat(),
                        y = (center.y + radiusDynamicContainer * sin(i * DEGREE_TO_RADIAN)).toFloat()
                    ),
                    radius = circleRadius
                )

                pathStatic.addOval(rect)
            }
        }

        val dynamicCircleCenter = Offset(
            x = (center.x + radiusDynamicContainer * cos(angle * DEGREE_TO_RADIAN)).toFloat(),
            y = (center.y + radiusDynamicContainer * sin(angle * DEGREE_TO_RADIAN)).toFloat()
        )

        val rect = Rect(
            center = dynamicCircleCenter,
            radius = dynamicCircleRadius
        )

        pathDynamic.reset()
        pathDynamic.addOval(rect)

        pathMeasure.setPath(pathDynamic, true)
        val discretePathEffect = DiscretePathEffect(pathMeasure.length / segmentCount, 0f)

        val chainPathEffect = PathEffect.chainPathEffect(
            outer = cornerPathEffect,
            inner = discretePathEffect.toComposePathEffect()
        )

        pathDynamic.op(pathDynamic, pathStatic, PathOperation.Union)

        drawFilledGooeyIndicators(
            paint,
            chainPathEffect,
            isPaintSetUp = isPaintSetUp,
            colors = DefaultGradientColors,
            path = pathDynamic
        ){
            isPaintSetUp = true
        }
    }
}

@Composable
private fun StrokeGooeyImpl(
    modifier: Modifier,
    pathStatic: Path,
    pathDynamic: Path,
    staticCircleCount: Int,
    angle: Float,
    pathMeasure: PathMeasure,
    cornerPathEffect: PathEffect,
    segmentCount: Int
) {

    Canvas(
        modifier
            .progressSemantics()
            .size(Size)
    ) {

        var canvasWidth = size.width
        var canvasHeight = size.height

        if (canvasHeight < canvasWidth) {
            canvasWidth = canvasHeight
        } else {
            canvasHeight = canvasWidth
        }

        val width = canvasWidth
        val height = canvasHeight

        val dynamicCircleRadius = width.coerceAtMost(height) * .15f

        // This is distance of center of circles to center of progress indicator
        val radiusDynamicContainer = (width / 2 - dynamicCircleRadius)

        if (pathStatic.isEmpty) {

            val circleRadius = width.coerceAtMost(height) * .1f

            for (i in 0..360 step 360 / staticCircleCount) {
                val rect = Rect(
                    center = Offset(
                        x = (center.x + radiusDynamicContainer * cos(i * DEGREE_TO_RADIAN)).toFloat(),
                        y = (center.y + radiusDynamicContainer * sin(i * DEGREE_TO_RADIAN)).toFloat()
                    ),
                    radius = circleRadius
                )

                pathStatic.addOval(rect)
            }
        }

        val dynamicCircleCenter = Offset(
            x = (center.x + radiusDynamicContainer * cos(angle * DEGREE_TO_RADIAN)).toFloat(),
            y = (center.y + radiusDynamicContainer * sin(angle * DEGREE_TO_RADIAN)).toFloat()
        )

        val rect = Rect(
            center = dynamicCircleCenter,
            radius = dynamicCircleRadius
        )

        pathDynamic.reset()
        pathDynamic.addOval(rect)

        pathMeasure.setPath(pathDynamic, true)
        val discretePathEffect = DiscretePathEffect(pathMeasure.length / segmentCount, 0f)

        val chainPathEffect = PathEffect.chainPathEffect(
            outer = cornerPathEffect,
            inner = discretePathEffect.toComposePathEffect()
        )

        pathDynamic.op(pathDynamic, pathStatic, PathOperation.Union)

        drawPath(
            path = pathDynamic,
            color = Color(0xffE1BEE7),
            style = Stroke(1.dp.toPx(), pathEffect = chainPathEffect)
        )
    }
}


private fun DrawScope.drawFilledGooeyIndicators(
    paint: Paint,
    chainPathEffect: PathEffect,
    isPaintSetUp: Boolean,
    colors: List<Color> = DefaultGradientColors,
    path: Path,
    onPaintSetUp: () -> Unit
) {
    paint.pathEffect = chainPathEffect

    if (!isPaintSetUp) {
        paint.shader = LinearGradientShader(
            from = Offset.Zero,
            to = Offset(size.width, size.height),
            colors = colors,
            tileMode = TileMode.Clamp
        )
        paint.pathEffect = chainPathEffect
        onPaintSetUp()
    }


    with(drawContext.canvas) {
        this.drawPath(
            path,
            paint
        )
    }
}