package com.example.bugsgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bugs = mutableListOf<Bug>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var onBugKilled: ((Bug) -> Unit)? = null
    var onMiss: (() -> Unit)? = null

    var speedMultiplier: Float = 1.0f
    var maxBugs: Int = 10

    private var lastFrameTime = 0L
    private var nextId = 0L
    private var isAnimating = false

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
        invalidate()
    }

    fun resumeAnimation() {
        if (!isAnimating) {
            isAnimating = true
            lastFrameTime = 0L
            postInvalidateOnAnimation()
        }
    }

    fun stopAnimation() {
        isAnimating = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isAnimating) return

        val now = System.currentTimeMillis()
        if (lastFrameTime == 0L) lastFrameTime = now
        val dt = (now - lastFrameTime) / 1000f
        lastFrameTime = now

        bugs.forEach { bug ->
            bug.x += bug.vx * dt
            bug.y += bug.vy * dt

            if (bug.x < 0f || bug.x > 1f) { bug.vx = -bug.vx; bug.x = bug.x.coerceIn(0f, 1f) }
            if (bug.y < 0f || bug.y > 1f) { bug.vy = -bug.vy; bug.y = bug.y.coerceIn(0f, 1f) }

            val px = bug.x * width
            val py = bug.y * height

            val minSide = Math.min(width, height).toFloat()
            val ps = bug.size * minSide

            val rect = RectF(px - ps / 2, py - ps / 2, px + ps / 2, py + ps / 2)
            val bitmap = context.getDrawable(bug.drawableRes)?.toBitmap(ps.toInt(), ps.toInt())
            bitmap?.let { canvas.drawBitmap(it, null, rect, paint) }
        }

        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val lx = event.x / width
            val ly = event.y / height

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
}