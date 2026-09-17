package com.gcevault.app.ui.questionbank

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gcevault.app.data.local.AppDatabase
import com.gcevault.app.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class QuestionBankViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).questionDao()

    private val _questions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val questions: StateFlow<List<QuestionEntity>> = _questions

    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects: StateFlow<List<String>> = _subjects

    // Fetches questions for a specific subject and paper
    fun loadQuestions(level: String, subject: String, paper: String) {
        dao.getQuestionsBySubjectAndPaper(level, subject, paper)
            .onEach { _questions.value = it }
            .launchIn(viewModelScope)
    }

    // Fetches all unique subjects for a given level (used for the Brain lists)
    fun loadSubjects(level: String) {
        dao.getAllSubjectsForLevel(level)
            .onEach { _subjects.value = it }
            .launchIn(viewModelScope)
    }
}