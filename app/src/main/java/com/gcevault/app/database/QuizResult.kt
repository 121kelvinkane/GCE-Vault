package com.gcevault.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val date: Long = System.currentTimeMillis()
)
