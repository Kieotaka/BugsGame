package com.example.bugsgame

data class Bug(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val type: BugType,
    var points: Int,
    val drawableRes: Int,
    val isGold: Boolean = false
)