package com.example.bugsgame

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bugsgame.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Список курсов
        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        binding.spCourse.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            courses
        )

        // Выбор даты
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            selectedDate = Calendar.getInstance().apply {
                set(year, month, day)
            }
        }

        // Кнопка
        binding.btnSubmit.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Введите ФИО", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val player = collectPlayer(fullName)
            binding.tvResult.text = """
                ФИО: ${player.fullName}
                Пол: ${player.gender}
                Курс: ${player.course}
                Сложность: ${player.difficulty}
                Дата рождения: ${player.birthDate}
                Знак зодиака: ${player.zodiac}
            """.trimIndent()
        }
    }

    private fun collectPlayer(fullName: String): Player {
        val gender = when (binding.rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Мужской"
            R.id.rbFemale -> "Женский"
            else -> "Не указан"
        }

        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(selectedDate.time)

        val zodiac = getZodiac(
            selectedDate.get(Calendar.DAY_OF_MONTH),
            selectedDate.get(Calendar.MONTH) + 1
        )

        binding.ivZodiac.setImageResource(zodiacResId(zodiac))

        return Player(
            fullName = fullName,
            gender = gender,
            course = binding.spCourse.selectedItem.toString(),
            difficulty = binding.sbDifficulty.progress,
            birthDate = dateStr,
            zodiac = zodiac
        )
    }

    private fun getZodiac(day: Int, month: Int): String = when {
        (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Овен"
        (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Телец"
        (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Близнецы"
        (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Рак"
        (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Лев"
        (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Дева"
        (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Весы"
        (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Скорпион"
        (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Стрелец"
        (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Козерог"
        (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Водолей"
        else -> "Рыбы"
    }

    private fun zodiacResId(zodiac: String): Int = when (zodiac) {
        "Овен" -> R.drawable.aries
        "Телец" -> R.drawable.taurus
        "Близнецы" -> R.drawable.gemini
        "Рак" -> R.drawable.cancer
        "Лев" -> R.drawable.leo
        "Дева" -> R.drawable.virgo
        "Весы" -> R.drawable.libra
        "Скорпион" -> R.drawable.scorpio
        "Стрелец" -> R.drawable.sagittarius
        "Козерог" -> R.drawable.capricorn
        "Водолей" -> R.drawable.aquarius
        else -> R.drawable.pisces
    }
}