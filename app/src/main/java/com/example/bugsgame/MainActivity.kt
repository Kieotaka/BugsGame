package com.example.bugsgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ★ ВРЕМЕННО: вместо вкладок — GameView
        val gameView = GameView(this)
        setContentView(gameView)

        repeat(5) {
            gameView.addBug(BugType.entries.random())
        }
    }
}