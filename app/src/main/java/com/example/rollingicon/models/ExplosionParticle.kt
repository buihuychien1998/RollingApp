package com.example.rollingicon.models

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Parcelize
data class ExplosionParticle(
    var x: Float,
    var y: Float,
    var angle: Float,
    var speed: Float,
    var alpha: Float = 1.0f,
    var radius: Float = 1.0f,
    val color: Int = generateRandomColor() // Generate a random color for each particle
) : Parcelable {
    // Generate a random color (using ARGB format)
    companion object {
        fun generateRandomColor(): Int {
            val red = Random.nextInt(0xFF)
            val green = Random.nextInt(0xFF)
            val blue = Random.nextInt(0xFF)
            return (0xFF shl 24) or (red shl 16) or (green shl 8) or blue // ARGB color
        }
    }

    fun update() {
        // Move the particle in the direction of the angle with speed
        x += speed * Math.cos(angle.toDouble()).toFloat()
        y += speed * Math.sin(angle.toDouble()).toFloat()
        speed *= 0.98f // Slow down over time
        alpha -= 0.05f // Fade out over time
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.alpha = (alpha * 255).toInt() // Set alpha for fading effect
        paint.color = color // Set the color for the particle
        canvas.drawCircle(x, y, radius, paint)
    }
}