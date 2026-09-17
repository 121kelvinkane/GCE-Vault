package com.gcevault.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.gcevault.app.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    // Fetches questions by Level and Subject (ignores stream, so repeating subjects share the same questions!)
    @Query("SELECT * FROM questions WHERE level = :level AND subject = :subject AND paper = :paper ORDER BY year DESC")
    fun getQuestionsBySubjectAndPaper(level: String, subject: String, paper: String): Flow<List<QuestionEntity>>

    // For Daily Quiz: Gets random questions for the user's specific Level and Subject
    @Query("SELECT * FROM questions WHERE level = :level AND subject = :subject ORDER BY RANDOM() LIMIT :limit")
    fun getRandomQuestions(level: String, subject: String, limit: Int): Flow<List<QuestionEntity>>

    // Get all unique subjects for a specific Level (used to populate the Brain lists)
    @Query("SELECT DISTINCT subject FROM questions WHERE level = :level ORDER BY subject")
    fun getAllSubjectsForLevel(level: String): Flow<List<String>>
}