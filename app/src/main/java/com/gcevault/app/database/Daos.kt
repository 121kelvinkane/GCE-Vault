package com.gcevault.app.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getCurrentUser(): Flow<User?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Query("UPDATE users SET xp = xp + :amount WHERE id = 1")
    suspend fun addXp(amount: Int)
    
    @Query("UPDATE users SET streak = :newStreak WHERE id = 1")
    suspend fun updateStreak(newStreak: Int)
}

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insertResult(result: QuizResult)
    
    @Query("SELECT * FROM quiz_results ORDER BY date DESC")
    fun getAllResults(): Flow<List<QuizResult>>
    
    @Query("SELECT COUNT(*) FROM quiz_results")
    fun getTotalQuizzesTaken(): Flow<Int>
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>
}
