package com.example.bugsgame.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
    private var timer: CountDownTimer? = null
    private var isGameRunning = false

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

        return view
    }

    // Вызывается, когда вкладка становится активной
    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            startGameIfNeeded()
        }
    }

    // Вызывается, когда вкладка уходит в фон (свайп на другую)
    override fun onPause() {
        super.onPause()
        stopGame()
    }

    // Вызывается, когда ViewPager2 скрывает/показывает фрагмент
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            stopGame()
        } else {
            startGameIfNeeded()
        }
    }

    private fun startGameIfNeeded() {
        if (isGameRunning) return
        isGameRunning = true
        startRound()
    }

    private fun stopGame() {
        isGameRunning = false
        timer?.cancel()
        gameView.clear()
        gameView.stopAnimation()
    }

    private fun startRound() {
        timer?.cancel()
        gameView.clear()

        score = 0
        hits = 0
        misses = 0
        updateHud()

        // Запускаем отрисовку
        gameView.resumeAnimation()

        timer = object : CountDownTimer(60_000, 800) {
            override fun onTick(millisUntilFinished: Long) {
                gameView.addBug(BugType.entries.random())
                val sec = (millisUntilFinished / 1000).toInt()
                tvHud.text = "Очки: $score | Время: $sec | Попадания: $hits | Промахи: $misses"
            }

            override fun onFinish() {
                finishRound()
            }
        }.start()
    }

    private fun finishRound() {
        stopGame()
        isGameRunning = false

        val accuracy = if (hits + misses == 0) 0 else (hits * 100) / (hits + misses)

        AlertDialog.Builder(requireContext())
            .setTitle("Раунд завершён!")
            .setMessage("""
                Очки: $score
                Попадания: $hits
                Промахи: $misses
                Точность: $accuracy%
            """.trimIndent())
            .setPositiveButton("Играть снова") { _, _ ->
                startGameIfNeeded()
            }
            .setCancelable(false)
            .show()
    }

    private fun updateHud() {
        tvHud.text = "Очки: $score | Попадания: $hits | Промахи: $misses"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopGame()
    }
}