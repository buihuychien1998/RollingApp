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
            val savedIcons = PreferencesHelper.loadSelectedIconsFromPreferences(this@RollingIconWallpaperService)
            Log.d("IconLoading", "App Icons: ${savedIcons.size}")

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
                        if (icon != other && checkOverlap(icon, other)) {
                            isOverlapping = true
                            break
                        }
                    }
                } while (isOverlapping)

                val desiredRadius = iconSize * 5
                val maxRadius = minOf(screenWidth, screenHeight) / 2
                icon.radius = minOf(desiredRadius, maxRadius.toFloat())
                icon.velocityX = 5 * iconSpeed
                icon.velocityY = 5 * iconSpeed
            }
        }

        private fun checkOverlap(icon1: AppIcon, icon2: AppIcon): Boolean {
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
                    try {
                        val holder = surfaceHolder
                        if (!isSurfaceAvailable) return
                        val canvas = holder.lockCanvas() ?: return
                        render(canvas)
                        holder.unlockCanvasAndPost(canvas)
                        handler.postDelayed(this, 16) // 16~60 FPS 20~50
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
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
            updateIconsPosition(canvas, width, height)
        }

        // Method to update the icons' position and check for overlap
        private fun updateIconsPosition(canvas: Canvas, width: Int, height: Int) {
            val bounceDamping = 0.1f // Damping when bouncing off edges

            // Validate width and height
            if (width <= 0 || height <= 0) return

            for (i in icons.indices) {
                val icon = icons[i]
                // Apply gravity to the velocity, with adjusted sensitivity
                icon.velocityX += gravityX
                icon.velocityY += gravityY


                // Update the icon's position based on its velocity
                icon.x += icon.velocityX * settings.iconSpeed
                icon.y += icon.velocityY * settings.iconSpeed

                // Check for collision with screen boundaries
                if (icon.x < icon.radius * 0.75f || icon.x > width - icon.radius * 1.25f) {
                    icon.velocityX = -icon.velocityX * bounceDamping
                    icon.x = max(icon.radius * 0.75f, min(icon.x, width - icon.radius))
                }
                if (icon.y < icon.radius * 0.75f || icon.y > height - icon.radius * 1.25f) {
                    icon.velocityY = -icon.velocityY * bounceDamping
                    icon.y = max(icon.radius * 0.75f, min(icon.y, height - icon.radius))
                }

                // Giảm vận tốc dần dần (dựa trên iconSpeed)
                val dampingFactor = settings.iconSpeed
                icon.velocityX *= dampingFactor
                icon.velocityY *= dampingFactor

                // Check for overlaps with other icons
                for (j in i + 1 until icons.size) {
                    val other = icons[j]
                    if (checkOverlap(icon, other)) {
                        resolveOverlap(icon, other)
                    }
                }

                // Update explosion particles if necessary
                if (icon.isExploding) {
                    icon.explosionParticles.forEach { it.update() }
                    icon.explosionParticles.removeAll { it.alpha <= 0 }
                    if (icon.explosionParticles.isEmpty()) icon.resetState()
                }

                icon.draw(canvas, paint)
            }
        }

        private fun resolveOverlap(icon1: AppIcon, icon2: AppIcon) {
            val dx = icon2.x - icon1.x
            val dy = icon2.y - icon1.y
            val distance = hypot(dx, dy)
            val overlap = (icon1.radius + icon2.radius) - distance + 20

            // If there is an overlap, move icons apart based on the direction
            if (overlap > 0) {
                val angle = Math.atan2(dy.toDouble(), dx.toDouble())
                val overlapX = (overlap * Math.cos(angle)).toFloat()
                val overlapY = (overlap * Math.sin(angle)).toFloat()

                // Tách các biểu tượng bằng cách di chuyển dần dần
                val totalRadius = icon1.radius + icon2.radius
                val factor1 = icon2.radius / totalRadius
                val factor2 = icon1.radius / totalRadius

                icon1.x -= overlapX * factor1
                icon1.y -= overlapY * factor1
                icon2.x += overlapX * factor2
                icon2.y += overlapY * factor2

                // Reduce velocity slightly to avoid excessive bouncing
                val dampingFactor = 0.5f
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
                        if (settings.canDrag) {
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
                    val intent = Intent(Intent.ACTION_VIEW).apply {
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

                    val intent = Intent(Intent.ACTION_VIEW).apply {
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


        override fun onSensorChanged(event: SensorEvent?) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSensorUpdateTime > SENSOR_INACTIVITY_THRESHOLD) {
                reinitializeSensor()
            }
            lastSensorUpdateTime = currentTime

            if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
            accelerometerValues = event.values

            gravityX = -event.values[0]
            gravityY = event.values[1]
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
