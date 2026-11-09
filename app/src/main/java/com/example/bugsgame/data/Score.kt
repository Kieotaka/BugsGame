package com.example.bugsgame.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class Score(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val playerName: String,
    val points: Int,
    val difficulty: Int,
    val timestamp: Long = System.currentTimeMillis()
)