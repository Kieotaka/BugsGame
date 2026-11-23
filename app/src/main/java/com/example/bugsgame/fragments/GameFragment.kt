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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bugsgame.BugType
import com.example.bugsgame.GameView
import com.example.bugsgame.GameViewModel
import com.example.bugsgame.R
import com.example.bugsgame.data.AppDatabase
import com.example.bugsgame.data.Score
import com.example.bugsgame.data.gold.GoldRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private lateinit var gameView: GameView
    private lateinit var tvHud: TextView
    private lateinit var database: AppDatabase
    private val goldRepo = GoldRepository.get()

    // Используем activityViewModels() — ViewModel живет вместе с Activity (переживает поворот)
    private val viewModel: GameViewModel by activityViewModels()

    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        gameView = view.findViewById(R.id.gameView)
        tvHud = view.findViewById(R.id.tvHud)

        database = AppDatabase.getDatabase(requireContext())

        gameView.onBugKilled = { bug ->
            viewModel.score += bug.points
            viewModel.hits++
            updateHud()
        }

        gameView.onMiss = {
            viewModel.score -= 1
            viewModel.misses++
            updateHud()
        }

        // Восстанавливаем HUD (если поворот, тут уже будут старые очки)
        updateHud()

        // Если игра шла до поворота, мы должны просто продолжить анимацию и таймер
        if (viewModel.isGameRunning) {
            gameView.resumeAnimation()
            startTimer(viewModel.timeLeft * 1000L)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Запускаем игру только если она еще не запущена
        if (!isHidden && !viewModel.isGameRunning) {
            startGameIfNeeded()
        }
    }

    override fun onPause() {
        super.onPause()
        // НЕ ОСТАНАВЛИВАЕМ игру полностью, просто ставим на паузу анимацию
        timer?.cancel()
        gameView.stopAnimation()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            timer?.cancel()
            gameView.stopAnimation()
        } else {
            if (viewModel.isGameRunning) {
                gameView.resumeAnimation()
                startTimer(viewModel.timeLeft * 1000L)
            } else {
                startGameIfNeeded()
            }
        }
    }

    private fun startGameIfNeeded() {
        if (viewModel.isGameRunning) return
        viewModel.isGameRunning = true

        // Сбрасываем очки ТОЛЬКО при новом старте
        viewModel.score = 0
        viewModel.hits = 0
        viewModel.misses = 0
        viewModel.timeLeft = 60
        updateHud()

        gameView.clear()
        gameView.resetGoldTimer()

        viewLifecycleOwner.lifecycleScope.launch {
            val price = goldRepo.fetchGoldPrice()
            gameView.goldPrice = price ?: 5000.0
        }

        gameView.resumeAnimation()

        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val roundDurationMs = prefs.getInt("round_duration", 60).toLong() * 1000L

        startTimer(roundDurationMs)
    }

    private fun startTimer(durationMs: Long) {
        timer?.cancel()

        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val maxBugs = prefs.getInt("max_bugs", 10)
        val speedProgress = prefs.getInt("speed", 5)

        gameView.maxBugs = maxBugs
        gameView.speedMultiplier = speedProgress / 5f

        timer = object : CountDownTimer(durationMs, 800) {
            override fun onTick(millisUntilFinished: Long) {
                gameView.addBug(
                    BugType.entries
                        .filter { it != BugType.GOLD }
                        .random()
                )
                viewModel.timeLeft = (millisUntilFinished / 1000).toInt()
                updateHud()
            }

            override fun onFinish() {
                finishRound()
            }
        }.start()
    }

    private fun finishRound() {
        viewModel.isGameRunning = false
        timer?.cancel()
        gameView.clear()
        gameView.stopAnimation()

        val accuracy = if (viewModel.hits + viewModel.misses == 0) 0
        else (viewModel.hits * 100) / (viewModel.hits + viewModel.misses)

        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val playerName = prefs.getString("player_name", "Игрок") ?: "Игрок"
        val difficulty = prefs.getInt("difficulty", 5)

        CoroutineScope(Dispatchers.IO).launch {
            val existingScore = database.scoreDao().getScoreByName(playerName)
            if (existingScore == null) {
                database.scoreDao().insert(
                    Score(
                        playerName = playerName,
                        points = viewModel.score,
                        difficulty = difficulty,
                        accuracy = accuracy
                    )
                )
            } else {
                if (viewModel.score > existingScore.points) {
                    database.scoreDao().updateScore(
                        name = playerName,
                        newPoints = viewModel.score,
                        newDifficulty = difficulty,
                        newAccuracy = accuracy,
                        newTimestamp = System.currentTimeMillis()
                    )
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Раунд завершён!")
            .setMessage("""
                Очки: ${viewModel.score}
                Попадания: ${viewModel.hits}
                Промахи: ${viewModel.misses}
                Точность: $accuracy%
            """.trimIndent())
            .setPositiveButton("Играть снова") { _, _ ->
                startGameIfNeeded() // тут startGameIfNeeded сбросит всё и запустит заново
            }
            .setCancelable(false)
            .show()
    }

    private fun updateHud() {
        tvHud.text = "Очки: ${viewModel.score} | Время: ${viewModel.timeLeft} | Попадания: ${viewModel.hits} | Промахи: ${viewModel.misses}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        // gameView.clear() не нужен, иначе при повороте удалим всех жуков
    }
}