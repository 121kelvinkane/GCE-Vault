package com.gcevault.app.data.repository

import com.gcevault.app.data.local.dao.QuestionDao
import com.gcevault.app.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

class QuestionRepository(private val dao: QuestionDao) {

    // Fetches questions by Level, Subject, and Paper
    fun getQuestionsBySubjectAndPaper(level: String, subject: String, paper: String): Flow<List<QuestionEntity>> {
        return dao.getQuestionsBySubjectAndPaper(level, subject, paper)
    }

    // Fetches random questions for the Daily Quiz
    fun getRandomQuestions(level: String, subject: String, limit: Int): Flow<List<QuestionEntity>> {
        return dao.getRandomQuestions(level, subject, limit)
    }

    // Fetches all unique subjects for a specific level (used for the Brain lists)
    fun getAllSubjectsForLevel(level: String): Flow<List<String>> {
        return dao.getAllSubjectsForLevel(level)
    }
}