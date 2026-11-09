package com.example.bugsgame

data class Bug(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val type: BugType,
    val points: Int,
    val drawableRes: Int,
    var lastDirectionChange: Long = System.currentTimeMillis(),
    var wasStuck: Boolean = false,
    var chaoticUntil: Long = 0L // <-- Время, до которого жук движется хаотично
)