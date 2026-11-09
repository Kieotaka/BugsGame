package com.example.bugsgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), SensorEventListener {

    private val bugs = mutableListOf<Bug>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var onBugKilled: ((Bug) -> Unit)? = null
    var onMiss: (() -> Unit)? = null

    var speedMultiplier: Float = 1.0f
    var maxBugs: Int = 10

    private var lastFrameTime = 0L
    private var nextId = 0L
    private var isAnimating = false

    // --- БОНУС И НАКЛОН ---
    private var bonusVisible = false
    private var bonusRect = RectF()
    private var lastBonusTime = 0L
    private val BONUS_INTERVAL = 15_000L
    private var tiltModeActive = false
    private var tiltX = 0f
    private var tiltY = 0f

    // --- ЗВУК ---
    private var soundPool: SoundPool? = null
    private var screamSoundId = 0
    private var soundLoaded = false

    // --- ДАТЧИК ---
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        /*
        screamSoundId = soundPool?.load(context, R.raw.bug_scream, 1) ?: 0
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) soundLoaded = true
        }
        */
    }

    fun resumeAnimation() {
        if (!isAnimating) {
            isAnimating = true
            lastFrameTime = 0L
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
            postInvalidateOnAnimation()
        }
    }

    fun stopAnimation() {
        isAnimating = false
        sensorManager.unregisterListener(this)
    }

    fun addBug(type: BugType) {
        if (bugs.size >= maxBugs) return
        val size = 0.24f * type.sizeFactor
        val bug = Bug(
            id = nextId++,
            x = 0.1f + Random.nextFloat() * 0.8f,
            y = 0.1f + Random.nextFloat() * 0.8f,
            vx = (if (Random.nextFloat() < 0.5f) -1 else 1) * 0.3f * type.speedFactor * speedMultiplier,
            vy = (if (Random.nextFloat() < 0.5f) -1 else 1) * 0.3f * type.speedFactor * speedMultiplier,
            size = size,
            type = type,
            points = type.points,
            drawableRes = type.drawableRes
        )
        bugs.add(bug)
    }

    fun clear() {
        bugs.clear()
        bonusVisible = false
        tiltModeActive = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isAnimating) return

        val now = System.currentTimeMillis()
        if (lastFrameTime == 0L) lastFrameTime = now
        val dt = (now - lastFrameTime) / 1000f
        lastFrameTime = now

        // Логика появления бонуса
        if (!bonusVisible && now - lastBonusTime > BONUS_INTERVAL) {
            bonusVisible = true
            val bx = 0.2f + Random.nextFloat() * 0.6f
            val by = 0.2f + Random.nextFloat() * 0.6f
            val bSize = 0.15f * Math.min(width, height)
            bonusRect.set(
                bx * width - bSize / 2,
                by * height - bSize / 2,
                bx * width + bSize / 2,
                by * height + bSize / 2
            )
        }

        bugs.forEach { bug ->
            if (tiltModeActive) {
                // Плавно добавляем скорость от наклона, но ограничиваем максимум
                bug.vx += tiltX * 0.05f * dt
                bug.vy += tiltY * 0.05f * dt
                bug.vx = bug.vx.coerceIn(-0.6f, 0.6f)
                bug.vy = bug.vy.coerceIn(-0.6f, 0.6f)
            }

            bug.x += bug.vx * dt
            bug.y += bug.vy * dt

            // Отскок от краев (для master)
            if (bug.x < 0f || bug.x > 1f) { bug.vx = -bug.vx; bug.x = bug.x.coerceIn(0f, 1f) }
            if (bug.y < 0f || bug.y > 1f) { bug.vy = -bug.vy; bug.y = bug.y.coerceIn(0f, 1f) }

            // Рисуем
            val px = bug.x * width
            val py = bug.y * height
            val minSide = Math.min(width, height).toFloat()
            val ps = bug.size * minSide
            val rect = RectF(px - ps / 2, py - ps / 2, px + ps / 2, py + ps / 2)
            val bitmap = context.getDrawable(bug.drawableRes)?.toBitmap(ps.toInt(), ps.toInt())
            bitmap?.let { canvas.drawBitmap(it, null, rect, paint) }
        }

        if (bonusVisible) {
            paint.color = 0xFFFFD700.toInt()
            canvas.drawOval(bonusRect, paint)
        }

        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val lx = event.x / width
            val ly = event.y / height

            if (bonusVisible && bonusRect.contains(event.x, event.y)) {
                bonusVisible = false
                lastBonusTime = System.currentTimeMillis()
                tiltModeActive = true
                if (soundLoaded && screamSoundId != 0) {
                    soundPool?.play(screamSoundId, 1f, 1f, 1, 0, 1f)
                }
                postDelayed({
                    tiltModeActive = false
                    // СБРАСЫВАЕМ СКОРОСТЬ до обычной, чтобы жуки не летали как супергерои
                    bugs.forEach { bug ->
                        bug.vx = (if (Random.nextFloat() < 0.5f) -1 else 1) * 0.3f * bug.type.speedFactor * speedMultiplier
                        bug.vy = (if (Random.nextFloat() < 0.5f) -1 else 1) * 0.3f * bug.type.speedFactor * speedMultiplier
                    }
                }, 5000)
                invalidate()
                return true
            }

            val hit = bugs.find { bug ->
                val dx = lx - bug.x
                val dy = ly - bug.y
                val r = bug.size / 2
                dx * dx + dy * dy <= r * r
            }

            if (hit != null) {
                bugs.remove(hit)
                onBugKilled?.invoke(hit)
            } else {
                onMiss?.invoke()
            }
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            tiltX = event.values[0]
            tiltY = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Не используется
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        soundPool?.release()
        soundPool = null
    }
}