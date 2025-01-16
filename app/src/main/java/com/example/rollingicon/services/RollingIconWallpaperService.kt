package com.example.rollingicon.services

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.example.rollingicon.R
import com.example.rollingicon.models.AppIcon
import com.example.rollingicon.models.IconSettings
import com.example.rollingicon.utils.IconType
import com.example.rollingicon.utils.PreferencesHelper
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class RollingIconWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return RollingIconEngine()
    }

    inner class RollingIconEngine : Engine(), SensorEventListener {
        private var accelerometerValues: FloatArray? = null
        private val icons = mutableListOf<AppIcon>()
        private lateinit var sensorManager: SensorManager
        private var accelerometer: Sensor? = null
        private var gravityX = 0f
        private var gravityY = 0f
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val padding = 15f // Khoảng đệm giữa các icon
        private var isSurfaceAvailable = false
        private var backgroundBitmap: Bitmap? = null
        val SENSOR_INACTIVITY_THRESHOLD = 1000L // 1 second of inactivity
        private var lastSensorUpdateTime = System.currentTimeMillis()
        private lateinit var settings: IconSettings
        private lateinit var mediaPlayer: MediaPlayer

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            settings = loadSettings()  // Load all settings at once
            loadBackgroundImage()
            initializeSound()
            initializeIcons()
            initializeSensors()
            startRendering()
        }

        private fun loadSettings(): IconSettings {
            val context = applicationContext
            // Load each setting from SharedPreferences
            val iconSize = PreferencesHelper.loadIconSize(context) 
            val iconSpeed = PreferencesHelper.loadIconSpeed(context) 
            val canTouch = PreferencesHelper.loadCanTouch(context) 
            val canDrag = PreferencesHelper.loadCanDrag(context) 
            val canExplosion =
                PreferencesHelper.loadCanExplosion(context) 
            val canSound = PreferencesHelper.loadCanSound(context) 


            return IconSettings(
                iconSize = iconSize,
                iconSpeed = iconSpeed.iconSpeedValue,
                canTouch = canTouch,
                canDrag = canDrag,
                canExplosion = canExplosion,
                canSound = canSound
            )
        }

        private fun initializeSound() {
            mediaPlayer = MediaPlayer.create(applicationContext, R.raw.icon_click_sound)
        }

        private fun initializeIcons() {
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            // Retrieve icons passed from MainActivity
            val savedIcons =
                PreferencesHelper.loadSelectedIconsFromPreferences(this@RollingIconWallpaperService)
            val iconSize = settings.iconSize
            val iconSpeed = settings.iconSpeed

            icons.addAll(savedIcons)
            icons.forEach { icon ->
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

                // Apply icon size and speed settings
                icon.radius = iconSize
//                icon.velocityX = (-1..5).random().toFloat() * iconSpeed
//                icon.velocityY = (-1..5).random().toFloat() * iconSpeed
                icon.velocityX = 5 * iconSpeed
                icon.velocityY = 5 * iconSpeed
//                icon.velocityX = Random.nextFloat() * 4 + 2 // Tốc độ từ 2 đến 6
//                icon.velocityY = Random.nextFloat() * 4 + 2
            }
        }

        private fun checkOverlap(icon1: AppIcon, icon2: AppIcon, padding: Float): Boolean {
            val dx = icon2.x - icon1.x
            val dy = icon2.y - icon1.y
            val distance = hypot(dx, dy)
            return distance < (icon1.radius + icon2.radius + padding)
        }

        private fun loadBackgroundImage() {
            backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.bg_rolling_app)
        }

        private fun initializeSensors() {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelerometer?.let {
                sensorManager.registerListener(
                    this, it, SensorManager.SENSOR_DELAY_UI
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

        private fun isDeviceMoving(accelerometerValues: FloatArray): Boolean {
            val x = accelerometerValues[0]
            val y = accelerometerValues[1]
            val z = accelerometerValues[2]

            // Calculate the total acceleration (magnitude of the vector)
            val acceleration = hypot(hypot(x.toDouble(), y.toDouble()), z.toDouble())

            // Define a threshold to determine movement (adjust as needed)
            val threshold = 1.0f

            return acceleration > threshold
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
                val separationFactor = 0.6f
                icon1.x -= overlapX * separationFactor
                icon1.y -= overlapY * separationFactor
                icon2.x += overlapX * separationFactor
                icon2.y += overlapY * separationFactor

                // Giảm vận tốc để làm giảm hiệu ứng nảy mạnh
//                val dampingFactor = 0.4f
//                icon1.velocityX *= dampingFactor
//                icon1.velocityY *= dampingFactor
//                icon2.velocityX *= dampingFactor
//                icon2.velocityY *= dampingFactor
                // Nếu vận tốc quá nhỏ, đặt lại về 0 để tránh di chuyển không mong muốn
                if (hypot(icon1.velocityX.toDouble(), icon1.velocityY.toDouble()) < 0.1) {
                    icon1.velocityX = 0f
                    icon1.velocityY = 0f
                }

                if (hypot(icon2.velocityX.toDouble(), icon2.velocityY.toDouble()) < 0.1) {
                    icon2.velocityX = 0f
                    icon2.velocityY = 0f
                }

                // Giảm tốc độ dần dần sau va chạm
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
                        if(settings.canDrag){
                            for (icon in icons) {
                                if (icon.isDragging) {
                                    val dx = (touchX - (icon.x + icon.dragOffsetX))
                                    val dy = (touchY - (icon.y + icon.dragOffsetY))
                                    if (hypot(dx, dy) > DRAG_THRESHOLD) {
                                        icon.isClicked = false
                                    }
                                    icon.x = touchX - icon.dragOffsetX
                                    icon.y = touchY - icon.dragOffsetY
                                    break
                                }
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
            println(settings.toString())
            if (!settings.canTouch) return

            if (settings.canExplosion) {
                icon.startExplosion() // Trigger explosion
            }

            if (settings.canSound) {
                mediaPlayer.start()
            }

            when (icon.type) {
                IconType.APP.name -> {
                    // Create the intent to launch the app
                    val intent = packageManager.getLaunchIntentForPackage(icon.packageName)
                        ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        ?: Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("market://details?id=${icon.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    startActivity(intent)
                }

                IconType.IMAGE.name -> {
                    println()
                    // Mở ảnh
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        setDataAndType(
                            Uri.parse(icon.filePath),
                            "image/*"
                        ) // Adjust MIME type as needed
                        flags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("Error", "Failed to open the file: ${e.message}")
                    }
                }

                IconType.VIDEO.name -> {
                    println(icon.filePath)

                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        setDataAndType(
                            Uri.parse(icon.filePath),
                            "video/*"
                        ) // Adjust MIME type as needed
                        flags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("Error", "Failed to open the file: ${e.message}")
                    }
                }

                else -> {
                    Log.w("IconType", "Unknown type: ${icon.type}")
                }
            }
        }

        private fun smoothGravity(newGravity: Float, oldGravity: Float, factor: Float) =
            oldGravity + factor * (newGravity - oldGravity)

        override fun onSensorChanged(event: SensorEvent?) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSensorUpdateTime > SENSOR_INACTIVITY_THRESHOLD) {
                reinitializeSensor()
            }
            lastSensorUpdateTime = currentTime
            val SENSOR_NOISE_THRESHOLD = 0.05f // Fine-tune this value

            if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
             accelerometerValues = event.values
            // Kiểm tra gia tốc để xem thiết bị có di chuyển không
            val deviceMoving = accelerometerValues?.let { isDeviceMoving(it) } ?: true
            if (!deviceMoving) {
                return
            }
            val x = event.values[0]
            val y = event.values[1]

            if (Math.abs(x - gravityX) > SENSOR_NOISE_THRESHOLD || Math.abs(y - gravityY) > SENSOR_NOISE_THRESHOLD) {
                gravityX = smoothGravity(-x, gravityX, SENSOR_NOISE_THRESHOLD)
                gravityY = smoothGravity(y, gravityY, SENSOR_NOISE_THRESHOLD)
            } else {
                // Log if sensor seems inactive for a long time
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
