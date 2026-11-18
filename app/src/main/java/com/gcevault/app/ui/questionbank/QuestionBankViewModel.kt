package com.gcevault.app.ui.questionbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcevault.app.data.repository.FireQuestion
import com.gcevault.app.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestionBankViewModel : ViewModel() {

    private val repository = QuestionRepository()

    // All questions loaded from Firestore for the current subject
    private val _allQuestions = MutableStateFlow<List<FireQuestion>>(emptyList())
    val allQuestions: StateFlow<List<FireQuestion>> = _allQuestions.asStateFlow()

    // Questions after applying the search filter
    private val _filteredQuestions = MutableStateFlow<List<FireQuestion>>(emptyList())
    val filteredQuestions: StateFlow<List<FireQuestion>> = _filteredQuestions.asStateFlow()

    // Current search text from the search bar
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Loading state to show a spinner while Firestore fetches
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Tracks which subject is currently loaded
    private var currentSubject: String = ""

    /**
     * Called when the user selects a subject.
     * Fetches all questions for that subject from Firestore.
     */
    fun loadQuestions(subject: String) {
        if (subject == currentSubject && _allQuestions.value.isNotEmpty()) return

        currentSubject = subject
        _searchQuery.value = ""
        _filteredQuestions.value = emptyList()

        viewModelScope.launch {
            _isLoading.value = true
            val questions = repository.fetchQuestionsBySubject(subject)
            _allQuestions.value = questions
            _filteredQuestions.value = questions
            _isLoading.value = false
        }
    }

    /**
     * Called every time the user types in the search bar.
     * Filters the already-loaded questions locally for instant results.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilter(query)
    }

    /**
     * Clears the search and shows all questions again.
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _filteredQuestions.value = _allQuestions.value
    }

    // Filters questions by topic, question text, year, and paper
    private fun applyFilter(query: String) {
        if (query.isBlank()) {
            _filteredQuestions.value = _allQuestions.value
            return
        }

        val lowerQuery = query.lowercase().trim()

        _filteredQuestions.value = _allQuestions.value.filter { question ->
            question.topics.any { it.lowercase().contains(lowerQuery) } ||
            question.questionText.lowercase().contains(lowerQuery) ||
            question.year.contains(lowerQuery) ||
            question.paper.lowercase().contains(lowerQuery) ||
            question.type.lowercase().contains(lowerQuery)
        }
    }
}