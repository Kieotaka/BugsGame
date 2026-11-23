package com.example.bugsgame

enum class BugType(
    val points: Int,
    val speedFactor: Float,
    val sizeFactor: Float,
    val drawableRes: Int
) {
    NORMAL(points = 1, speedFactor = 1.0f, sizeFactor = 1.0f, drawableRes = R.drawable.bug_normal),
    FAST(points = 2, speedFactor = 1.8f, sizeFactor = 0.7f, drawableRes = R.drawable.bug_fast),
    RARE(points = 5, speedFactor = 2.5f, sizeFactor = 0.6f, drawableRes = R.drawable.bug_rare),

    // У золотого жука базовые очки 0, но мы их переопределяем при создании
    GOLD(points = 100, speedFactor = 1.4f, sizeFactor = 0.9f, drawableRes = R.drawable.gold_bug)
}