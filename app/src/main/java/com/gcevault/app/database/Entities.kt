package com.gcevault.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 1,
    val username: String,
    val email: String,
    val phone: String,
    val level: String,
    val stream: String,
    val xp: Int = 0,
    val streak: Int = 0,
    val unlockedAchievements: String = "" // ✅ NEW: Stores comma-separated achievement IDs (e.g., "first_quiz,perfect_score")
)

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val date: Long
)