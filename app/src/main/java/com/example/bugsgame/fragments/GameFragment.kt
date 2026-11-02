package com.example.bugsgame.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.bugsgame.BugType
import com.example.bugsgame.GameView
import com.example.bugsgame.R

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var tvHud: TextView

    private var score = 0
    private var hits = 0
    private var misses = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        gameView = view.findViewById(R.id.gameView)
        tvHud = view.findViewById(R.id.tvHud)

        gameView.onBugKilled = { bug ->
            score += bug.points
            hits++
            updateHud()
        }

        gameView.onMiss = {
            score -= 1
            misses++
            updateHud()
        }

        // Временно: 5 жуков
        repeat(5) {
            gameView.addBug(BugType.entries.random())
        }

        return view
    }

    private fun updateHud() {
        tvHud.text = "Очки: $score | Попадания: $hits | Промахи: $misses"
    }
}