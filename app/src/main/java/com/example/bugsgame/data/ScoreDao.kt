package com.example.bugsgame.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    // Вставка (если игрок уже есть, заменит запись)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: Score)

    // Получить все рекорды, отсортированные по очкам
    @Query("SELECT * FROM scores ORDER BY points DESC")
    fun getAllScores(): Flow<List<Score>>

    // Найти рекорд по имени игрока
    @Query("SELECT * FROM scores WHERE playerName = :name LIMIT 1")
    suspend fun getScoreByName(name: String): Score?

    // Обновить очки у существующего игрока
    @Query("UPDATE scores SET points = :newPoints, difficulty = :newDifficulty, timestamp = :newTimestamp WHERE playerName = :name")
    suspend fun updateScore(name: String, newPoints: Int, newDifficulty: Int, newTimestamp: Long)

    // Очистить таблицу рекордов
    @Query("DELETE FROM scores")
    suspend fun clearAllScores()
}