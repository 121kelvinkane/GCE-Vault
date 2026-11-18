package com.gcevault.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String = "",
    val subject: String = "",
    val year: String = "",
    val paper: String = "",
    val brain: String = "",
    val question_number: String = "",
    val question_text: String = "",
    val topics: List<String> = emptyList(),
    val options: Map<String, String> = emptyMap(),
    val type: String = "essay"
)
