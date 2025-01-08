package com.example.rollingicon

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Parcelable
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Parcelize
data class AppIcon (
//    @IgnoredOnParcel
    val drawable: ByteArray?,
    val packageName: String, // Store the package name
    val name: String, // Store the package name
    var x: Float = 0f,
    var y: Float = 0f,
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    val radius: Float = 75f, // Flag to increase the icon size
    var isClicked: Boolean = false, // Flag to check if the icon is clicked
    var isExploding: Boolean = false, // Flag for explosion animation
    var explosionParticles: MutableList<ExplosionParticle> = mutableListOf(), // Store explosion particles
    var isDragging: Boolean = false,
    var dragOffsetX: Float = 0f,
    var dragOffsetY: Float = 0f
) : Parcelable {
    // Ensure proper equality check
    override fun equals(other: Any?): Boolean {
        return (other is AppIcon) && packageName == other.packageName
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }

    fun update(gravityX: Float, gravityY: Float, width: Int, height: Int) {
        val SENSOR_SENSITIVITY = 0.6f // Fine-tuned sensitivity for smoother gravity response
        velocityX += gravityX * SENSOR_SENSITIVITY
        velocityY += gravityY * SENSOR_SENSITIVITY

        // Apply friction to simulate natural slowing down
        velocityX *= 0.9f // Tăng nhẹ hiệu ứng giảm tốc
        velocityY *= 0.9f

        // Ngưỡng vận tốc tối thiểu
        if (Math.abs(velocityX) < 0.5f) velocityX = Random.nextFloat() * 2 - 1 // Random trong khoảng [-1, 1]
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
            drawable?.toDrawable()?.setBounds(
                (x - radius).toInt(),
                (y - radius).toInt(),
                (x + radius).toInt(),
                (y + radius).toInt()
            )
//            drawable?.toDrawable()?.draw(canvas)
            val bitmap = BitmapFactory.decodeByteArray(drawable, 0, drawable?.size ?: 0)
            canvas.drawBitmap(bitmap, x - radius, y - radius, paint)
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

@Parcelize
data class ExplosionParticle(
    var x: Float,
    var y: Float,
    var angle: Float,
    var speed: Float,
    var alpha: Float = 1.0f,
    val color: Int = generateRandomColor() // Generate a random color for each particle
) : Parcelable {
    private val radius = 5f

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


class RollingIconWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return RollingIconEngine()
    }

    inner class RollingIconEngine : Engine(), SensorEventListener {
        private val icons = mutableListOf<AppIcon>()
        private lateinit var sensorManager: SensorManager
        private var accelerometer: Sensor? = null
        private var gravityX = 0f
        private var gravityY = 0f
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val maxIcons = 10
        val padding = 15f // Khoảng đệm giữa các icon
        private var isSurfaceAvailable = false
        private var backgroundBitmap: Bitmap? = null
        val SENSOR_INACTIVITY_THRESHOLD = 1000L // 1 second of inactivity
        private var lastSensorUpdateTime = System.currentTimeMillis()

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            initializeIcons()
            loadBackgroundImage()
            initializeSensors()
            startRendering()
        }

        private fun initializeIcons() {
            val pm = packageManager
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels

//            pm.getInstalledApplications(PackageManager.GET_META_DATA)
//                .filter { app ->
//                    (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) &&
//                            (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0)
//                }
//                .take(maxIcons)
//                .forEach { app ->
//                    icons.add(AppIcon(app.loadIcon(pm), app.packageName))
//                }
            // Retrieve icons passed from MainActivity
// Retrieve the list of icons from SharedPreferences
            val sharedPreferences = getSharedPreferences("icon_data", MODE_PRIVATE)
            val iconsJson = sharedPreferences.getString("icons_list", "[]")
            val iconsList = parseIconsFromJson(iconsJson ?: "[]")
            icons.addAll(iconsList)
            println("iconsList")
            println("${icons.size}")

            for (icon in icons) {
                var isOverlapping: Boolean
                do {
                    isOverlapping = false
                    icon.x = Random.nextFloat() * (screenWidth - 2 * icon.radius) + icon.radius
                    icon.y = Random.nextFloat() * (screenHeight - 2 * icon.radius) + icon.radius

                    for (other in icons) {
                        if (icon != other && checkOverlap(icon, other, padding)) {
                            isOverlapping = true
                            break
                        }
                    }
                } while (isOverlapping)

                icon.velocityX = (-1..5).random().toFloat()
                icon.velocityY = (-1..5).random().toFloat()
//                icon.velocityX = Random.nextFloat() * 4 + 2 // Tốc độ từ 2 đến 6
//                icon.velocityY = Random.nextFloat() * 4 + 2
            }
        }

        // Method to parse JSON into a list of AppIcon
        private fun parseIconsFromJson(json: String): List<AppIcon> {
            // Parse JSON to create the list of AppIcon objects
            val gson = Gson()
            val type = object : TypeToken<List<AppIcon>>() {}.type
            return gson.fromJson(json, type)
        }

        private fun checkOverlap(icon1: AppIcon, icon2: AppIcon, padding: Float): Boolean {
            val dx = icon2.x - icon1.x
            val dy = icon2.y - icon1.y
            val distance = hypot(dx, dy)
            return distance < (icon1.radius + icon2.radius + padding)
        }

        private fun loadBackgroundImage() {
            backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.background_image)
        }

        private fun initializeSensors() {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelerometer?.let {
                sensorManager.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        }

        private fun startRendering() {
            val handler = android.os.Handler()
            val renderRunnable = object : Runnable {
                override fun run() {
                    val holder = surfaceHolder
                    if (!isSurfaceAvailable) return
                    val canvas = holder.lockCanvas() ?: return
                    try {
                        render(canvas)
                    } finally {
                        holder.unlockCanvasAndPost(canvas)
                    }

                    handler.postDelayed(this, 16) // 16~60 FPS 20~50
                }
            }

            handler.post(renderRunnable)
        }

        private fun render(canvas: Canvas) {
            val width = canvas.width
            val height = canvas.height

            backgroundBitmap?.let {
                val scaledBitmap = Bitmap.createScaledBitmap(it, width, height, true)
                canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
            } ?: canvas.drawColor(0xFF000000.toInt())

            // Update and draw icons
            updateIconsPosition(width, height)
            icons.forEach { icon ->
                icon.update(gravityX, gravityY, width, height)
                icon.draw(canvas, paint)
            }
        }

        // Method to update the icons' position and check for overlap
        private fun updateIconsPosition(width: Int, height: Int) {

            for (i in icons.indices) {
                val icon = icons[i]

                // Update the icon's position based on its velocity
                icon.x += icon.velocityX
                icon.y += icon.velocityY

                // Check for collision with screen boundaries
                if (icon.x < icon.radius || icon.x > width - icon.radius) {
                    icon.velocityX = -icon.velocityX * 0.8f
                    icon.x = max(icon.radius, min(icon.x, width - icon.radius))
                }
                if (icon.y < icon.radius || icon.y > height - icon.radius) {
                    icon.velocityY = -icon.velocityY * 0.8f
                    icon.y = max(icon.radius, min(icon.y, height - icon.radius))
                }

                icon.velocityX *= 0.9f
                icon.velocityY *= 0.9f

                // Check for overlaps with other icons
                for (j in i + 1 until icons.size) {
                    val other = icons[j]
                    if (checkOverlap(icon, other, padding)) {
                        resolveOverlap(icon, other)
                    }
                }

                // Update explosion particles if necessary
                if (icon.isExploding) {
                    for (particle in icon.explosionParticles) {
                        particle.update()
                    }
                    icon.explosionParticles.removeAll { it.alpha <= 0 }
                    if (icon.explosionParticles.isEmpty()) {
                        icon.resetState()
                    }
                }
            }
        }

        private fun resolveOverlap(icon1: AppIcon, icon2: AppIcon) {
            val dx = icon2.x - icon1.x
            val dy = icon2.y - icon1.y
            val distance = hypot(dx, dy)
            val overlap = (icon1.radius + icon2.radius) - distance + 20
            val minSpace = 50f // Minimum space to add between the icons

            // If there is an overlap, move icons apart based on the direction
            if (overlap > 0) {
                val angle = Math.atan2(dy.toDouble(), dx.toDouble())
                val overlapX = (overlap * Math.cos(angle)).toFloat()
                val overlapY = (overlap * Math.sin(angle)).toFloat()

                // Tách các biểu tượng bằng cách di chuyển dần dần
                val separationFactor = 0.5f
                icon1.x -= overlapX * separationFactor
                icon1.y -= overlapY * separationFactor
                icon2.x += overlapX * separationFactor
                icon2.y += overlapY * separationFactor

                // Giảm vận tốc để làm giảm hiệu ứng nảy mạnh
                val dampingFactor = 0.4f
                icon1.velocityX *= dampingFactor
                icon1.velocityY *= dampingFactor
                icon2.velocityX *= dampingFactor
                icon2.velocityY *= dampingFactor
            }
        }

        override fun onTouchEvent(event: MotionEvent?) {
            val DRAG_THRESHOLD = 10f // Adjust as needed
            event?.let {
                val touchX = it.x
                val touchY = it.y

                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        for (icon in icons) {
                            if (icon.isTouched(touchX, touchY)) {
                                icon.isDragging = true
                                icon.dragOffsetX = touchX - icon.x
                                icon.dragOffsetY = touchY - icon.y
                                icon.isClicked = true // Mark as clicked for ACTION_UP
                                break
                            }
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        for (icon in icons) {
                            if (icon.isDragging) {
                                val dx = (touchX - (icon.x + icon.dragOffsetX))
                                val dy = (touchY - (icon.y + icon.dragOffsetY))
                                if (hypot(dx, dy) > DRAG_THRESHOLD) {
                                    icon.isClicked = false // Cancel click only if dragged significantly
                                }
                                icon.x = touchX - icon.dragOffsetX
                                icon.y = touchY - icon.dragOffsetY
                                break
                            }
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        for (icon in icons) {
                            if (icon.isDragging) {
                                icon.isDragging = false
                                icon.velocityX = 0f
                                icon.velocityY = 0f
                                if (icon.isClicked) {
                                    handleIconClick(icon) // Handle the click
                                }

                                icon.isClicked = false // Reset click flag
                                break
                            }
                        }
                    }
                }
            }
        }

        private fun handleIconClick(icon: AppIcon) {
            icon.startExplosion() // Trigger explosion

            // Create the intent to launch the app
            val intent = packageManager.getLaunchIntentForPackage(icon.packageName)
                ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                ?: Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=${icon.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            // Start the app
            startActivity(intent)
        }

        private fun smoothGravity(newGravity: Float, oldGravity: Float, factor: Float)
        = oldGravity + factor * (newGravity - oldGravity)

        override fun onSensorChanged(event: SensorEvent?) {
            Log.d("SensorData", "X: ${event?.values?.get(0)}, Y: ${event?.values?.get(1)}")
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSensorUpdateTime > SENSOR_INACTIVITY_THRESHOLD) {
                Log.w("SensorWarning", "Sensor seems inactive, attempting reinitialization.")
                reinitializeSensor()
            }
            lastSensorUpdateTime = currentTime
            val SENSOR_NOISE_THRESHOLD = 0.05f // Fine-tune this value

            if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

            val x = event.values[0]
            val y = event.values[1]

            if (Math.abs(x - gravityX) > SENSOR_NOISE_THRESHOLD || Math.abs(y - gravityY) > SENSOR_NOISE_THRESHOLD) {
                gravityX = smoothGravity(-x, gravityX, SENSOR_NOISE_THRESHOLD)
                gravityY = smoothGravity(y, gravityY, SENSOR_NOISE_THRESHOLD)
            } else {
                // Log if sensor seems inactive for a long time
                Log.w("SensorEvent", "Sensor seems inactive, attempting reinitialization.")
                reinitializeSensor()
            }
        }

        private fun reinitializeSensor() {
            sensorManager.unregisterListener(this)
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            isSurfaceAvailable = true
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            isSurfaceAvailable = false
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                accelerometer?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
            } else {
                sensorManager.unregisterListener(this)
            }
        }

    }
}
