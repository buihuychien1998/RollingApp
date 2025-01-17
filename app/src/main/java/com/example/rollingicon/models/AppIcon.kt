package com.example.rollingicon.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
import com.example.rollingicon.utils.IconType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Parcelize
data class AppIcon(
    var drawable: ByteArray?, // Optional byte array for icon drawable
    val packageName: String, // Used for apps; can be null for images/videos
    val name: String, // Name of the app or media item
    val type: String = IconType.APP.name, // Type of the icon ("app", "image", or "video")
    val filePath: String? = null, // Path to image or video file
    var x: Float = 0f, // X position for dragging
    var y: Float = 0f, // Y position for dragging
    var velocityX: Float = 0f, // X velocity for movement
    var velocityY: Float = 0f, // Y velocity for movement
    var radius: Float = 75f, // Radius for scaling icon size
    var isClicked: Boolean = false, // Flag to track if the icon is clicked
    var isExploding: Boolean = false, // Flag to indicate explosion animation
    var explosionParticles: MutableList<ExplosionParticle> = mutableListOf(), // Explosion particles
    var isDragging: Boolean = false, // Flag to indicate if dragging is active
    var dragOffsetX: Float = 0f, // Offset for drag in X direction
    var dragOffsetY: Float = 0f, // Offset for drag in Y direction
    var selected: Boolean = true, // Default selected state is false
) : Parcelable {
    @IgnoredOnParcel
    private var cachedBitmap: Bitmap? = null // Cached bitmap for reuse

    private fun getCachedBitmap(): Bitmap? {
        val width = radius * 2
        val height = radius * 2
        if (cachedBitmap == null && drawable != null) {
            // Decode the byte array into a Bitmap
            val originalBitmap = BitmapFactory.decodeByteArray(drawable, 0, drawable?.size ?: 0)

            // Get the original dimensions of the Bitmap
            val originalWidth = originalBitmap.width
            val originalHeight = originalBitmap.height

            // Calculate the scaling factor to preserve the aspect ratio
            val scaleFactor = Math.min(width / originalWidth, height / originalHeight)

            // Calculate the new dimensions based on the scaling factor
            val newWidth = (originalWidth * scaleFactor).toInt()
            val newHeight = (originalHeight * scaleFactor).toInt()

            // Create the scaled Bitmap while preserving the aspect ratio
            cachedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        }
        return cachedBitmap
    }

    // Ensure proper equality check
    override fun equals(other: Any?): Boolean {
        if (packageName.isEmpty()) {
            return (other is AppIcon) && filePath == other.filePath && selected == other.selected
        }
        return (other is AppIcon) && packageName == other.packageName
    }

    override fun hashCode(): Int {
        if (packageName.isEmpty()) {
            return filePath.hashCode()
        }
        return packageName.hashCode()
    }

    fun update(gravityX: Float, gravityY: Float, width: Int, height: Int) {
        val SENSOR_SENSITIVITY = 1.2f // Fine-tuned sensitivity for smoother gravity response
        velocityX += gravityX * SENSOR_SENSITIVITY
        velocityY += gravityY * SENSOR_SENSITIVITY

        // Apply friction to simulate natural slowing down
        velocityX *= 1.0f // Tăng nhẹ hiệu ứng giảm tốc
        velocityY *= 1.0f

        // Ngưỡng vận tốc tối thiểu
        if (Math.abs(velocityX) < 0.5f) velocityX =
            Random.nextFloat() * 2 - 1 // Random trong khoảng [-1, 1]
        if (Math.abs(velocityY) < 0.5f) velocityY = Random.nextFloat() * 2 - 1
        // Update icon position
        x += velocityX
        y += velocityY

        // Handle collisions with screen edges
        //Khi một icon va chạm với các cạnh màn hình hoặc nhau, hãy giảm hệ số phản lực để tránh di chuyển quá nhanh.
        if (x < radius || x > width - radius) {
            velocityX = -velocityX * 0.6f // Giảm hệ số phản lực
            x = max(radius, min(x, width - radius))
        }
        if (y < radius || y > height - radius) {
            velocityY = -velocityY * 0.6f // Giảm hệ số phản lực
            y = max(radius, min(y, height - radius))
        }

        // Update explosion particles
        if (isExploding) {
            explosionParticles.forEach { it.update() }
            explosionParticles.removeAll { it.alpha <= 0 }
            if (explosionParticles.isEmpty()) resetState()
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        // If the icon is exploding, draw the particles
        if (isExploding) {
            for (particle in explosionParticles) {
                particle.draw(canvas, paint)
            }
        } else {
            val bitmap = getCachedBitmap()
            bitmap?.let { canvas.drawBitmap(it, x - radius, y - radius, paint) }
        }
    }

    // Check if the icon was touched
    fun isTouched(touchX: Float, touchY: Float) =
        hypot(touchX - x, touchY - y) <= radius * 1.1f // Adding a small buffer for precision

    // Start explosion effect by generating particles
    fun startExplosion() {
        explosionParticles.addAll(
            List(100) {
                ExplosionParticle(
                    x, y,
                    angle = Random.nextFloat() * 2 * Math.PI.toFloat(),
                    speed = Random.nextFloat() * 10 + 10
                )
            }
        )
        isExploding = true
    }

    // Reset the icon's state after explosion
    fun resetState() {
        x = Random.nextFloat() * 800f + 100f // Random new position
        y = Random.nextFloat() * 800f + 100f
        velocityX = (-1..5).random().toFloat()
        velocityY = (-1..5).random().toFloat()
//        velocityX = Random.nextFloat() * 4 + 2 // Tốc độ từ 2 đến 6
//        velocityY = Random.nextFloat() * 4 + 2
        isExploding = false
        explosionParticles.clear() // Clear the particles
    }
}