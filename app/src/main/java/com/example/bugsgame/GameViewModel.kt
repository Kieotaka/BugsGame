package com.example.bugsgame

import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    var score = 0
    var hits = 0
    var misses = 0
    var timeLeft = 60
    var isGameRunning = false // Флаг, что игра уже идет
}