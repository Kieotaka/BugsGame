package com.example.bugsgame.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: Score)

    @Query("SELECT * FROM scores ORDER BY points DESC")
    fun getAllScores(): Flow<List<Score>>
}