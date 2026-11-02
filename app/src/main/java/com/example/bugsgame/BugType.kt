package com.example.bugsgame

enum class BugType(
    val points: Int,
    val speedFactor: Float,
    val sizeFactor: Float,
    val drawableRes: Int
) {
    NORMAL(points = 1, speedFactor = 1.0f, sizeFactor = 1.0f, drawableRes = R.drawable.bug_normal),
    FAST(points = 2, speedFactor = 2.0f, sizeFactor = 0.7f, drawableRes = R.drawable.bug_fast),
    RARE(points = 5, speedFactor = 0.8f, sizeFactor = 0.6f, drawableRes = R.drawable.bug_rare)
}