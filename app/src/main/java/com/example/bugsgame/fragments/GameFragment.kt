package com.example.bugsgame.fragments

import android.content.Context
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
import com.example.bugsgame.data.AppDatabase
import com.example.bugsgame.data.Score
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var tvHud: TextView
    private lateinit var database: AppDatabase

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

        database = AppDatabase.getDatabase(requireContext())

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

    override fun onResume() {
        super.onResume()
        if (!isHidden) startGameIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        stopGame()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) stopGame() else startGameIfNeeded()
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

        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val roundDurationMs = prefs.getInt("round_duration", 60).toLong() * 1000L
        val maxBugs = prefs.getInt("max_bugs", 10)
        val speedProgress = prefs.getInt("speed", 5)
        val speedMultiplier = speedProgress / 5f

        gameView.maxBugs = maxBugs
        gameView.speedMultiplier = speedMultiplier
        gameView.resumeAnimation()

        timer = object : CountDownTimer(roundDurationMs, 800) {
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

        // СОХРАНЕНИЕ / ОБНОВЛЕНИЕ В БАЗЕ
        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val playerName = prefs.getString("player_name", "Игрок") ?: "Игрок"
        val difficulty = prefs.getInt("difficulty", 5)

        CoroutineScope(Dispatchers.IO).launch {
            val existingScore = database.scoreDao().getScoreByName(playerName)
            if (existingScore == null) {
                // Игрока нет — создаем новую запись
                database.scoreDao().insert(
                    Score(playerName = playerName, points = score, difficulty = difficulty)
                )
            } else {
                // Игрок есть — обновляем только если новый результат ЛУЧШЕ
                if (score > existingScore.points) {
                    database.scoreDao().updateScore(
                        name = playerName,
                        newPoints = score,
                        newDifficulty = difficulty,
                        newTimestamp = System.currentTimeMillis()
                    )
                }
            }
        }

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