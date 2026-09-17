package com.gcevault.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. The User Table
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 1, // We'll only ever have 1 user for now
    val username: String,
    val email: String,
    val phone: String,
    val level: String,
    val stream: String,
    val xp: Int = 0,
    val streak: Int = 0
)

// 2. The Quiz Results Table (For Progress & Analytics)
@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val date: Long // We store the timestamp
)