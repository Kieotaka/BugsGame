package com.example.bugsgame.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: Score)

    @Query("SELECT * FROM scores ORDER BY points DESC")
    fun getAllScores(): Flow<List<Score>>

    @Query("SELECT * FROM scores WHERE playerName = :name LIMIT 1")
    suspend fun getScoreByName(name: String): Score?

    // Добавлен параметр newAccuracy
    @Query("UPDATE scores SET points = :newPoints, difficulty = :newDifficulty, accuracy = :newAccuracy, timestamp = :newTimestamp WHERE playerName = :name")
    suspend fun updateScore(name: String, newPoints: Int, newDifficulty: Int, newAccuracy: Int, newTimestamp: Long)

    @Query("DELETE FROM scores")
    suspend fun clearAllScores()
}