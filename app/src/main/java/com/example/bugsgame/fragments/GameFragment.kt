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

    // Счетчики
    private var score = 0
    private var hits = 0
    private var misses = 0

    // Таймер
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        gameView = view.findViewById(R.id.gameView)
        tvHud = view.findViewById(R.id.tvHud)

        // Подписываемся на события от GameView
        gameView.onBugKilled = { bug ->
            score += bug.points
            hits++
            updateHud()
        }

        gameView.onMiss = {
            score -= 1 // Штраф за промах
            misses++
            updateHud()
        }

        // Запускаем первый раунд
        startRound()

        return view
    }

    private fun startRound() {
        timer?.cancel() // Отменяем старый таймер, если был
        gameView.clear() // Очищаем экран от старых жуков

        // Сбрасываем счетчики
        score = 0
        hits = 0
        misses = 0
        updateHud()

        // CountDownTimer(время_раунда_мс, интервал_появления_жуков_мс)
        timer = object : CountDownTimer(60_000, 800) {
            override fun onTick(millisUntilFinished: Long) {
                // Добавляем нового случайного жука
                gameView.addBug(BugType.entries.random())

                // Обновляем текст (показываем оставшееся время)
                val sec = (millisUntilFinished / 1000).toInt()
                tvHud.text = "Очки: $score | Время: $sec | Попадания: $hits | Промахи: $misses"
            }

            override fun onFinish() {
                // Время вышло — показываем результат
                finishRound()
            }
        }.start()
    }

    private fun finishRound() {
        timer?.cancel()
        gameView.clear()

        // Считаем точность (процент попаданий от общего числа кликов)
        val accuracy = if (hits + misses == 0) 0 else (hits * 100) / (hits + misses)

        // Показываем диалоговое окно с результатами
        AlertDialog.Builder(requireContext())
            .setTitle("Раунд завершён!")
            .setMessage("""
                Очки: $score
                Попадания: $hits
                Промахи: $misses
                Точность: $accuracy%
            """.trimIndent())
            .setPositiveButton("Играть снова") { _, _ ->
                startRound() // Перезапускаем игру
            }
            .setCancelable(false) // Нельзя закрыть, нажав мимо окна
            .show()
    }

    private fun updateHud() {
        tvHud.text = "Очки: $score | Попадания: $hits | Промахи: $misses"
    }

    // Обязательно отменяем таймер, когда уходим с вкладки, чтобы не было утечек памяти
    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}