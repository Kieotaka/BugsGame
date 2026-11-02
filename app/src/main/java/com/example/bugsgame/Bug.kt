package com.example.bugsgame

data class Bug(
    val id: Long,              // уникальный идентификатор
    var x: Float,              // логическая координата X (0..1)
    var y: Float,              // логическая координата Y (0..1)
    var vx: Float,             // скорость по X (ед/сек в логике)
    var vy: Float,             // скорость по Y
    val size: Float,           // размер в логических единицах (0..1)
    val type: BugType,         // тип
    val points: Int,           // стоимость в очках
    val drawableRes: Int       // визуальный ресурс
)