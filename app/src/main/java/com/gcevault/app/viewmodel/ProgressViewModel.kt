package com.gcevault.app.viewmodel

import android.app.Application
import android.content.Context // ✅ ADDED MISSING IMPORT
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gcevault.app.database.AppDatabase
import com.gcevault.app.database.QuizResult
import com.gcevault.app.database.QuizResultDao
import com.gcevault.app.notifications.ReminderHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val quizResultDao = database.quizResultDao()
    private val userDao = database.userDao()

    val allResults: StateFlow<List<QuizResult>> = quizResultDao.getAllResults()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val totalQuizzes: StateFlow<Int> = quizResultDao.getTotalQuizzesTaken()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val totalXp: StateFlow<Int> = userDao.getCurrentUser()
        .map { user -> user?.xp ?: 0 }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    private val _achievementEvent = MutableSharedFlow<String>()
    val achievementEvent: SharedFlow<String> = _achievementEvent

    fun saveQuizResult(subject: String, score: Int, totalQuestions: Int) {
        viewModelScope.launch {
            // ✅ AUTOMATIC REMINDER LOGIC: Silently activate after the FIRST quiz
            val context = getApplication<Application>()
            if (!context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getBoolean("auto_reminder_scheduled", false)) {
                ReminderHelper.scheduleAutomaticReminder(context)
                Log.d("GCE_VAULT_REMINDER", "First quiz completed! Automatic midnight reminders activated.")
            }

            // 1. Save the quiz result
            quizResultDao.insertResult(
                QuizResult(subject = subject, score = score, totalQuestions = totalQuestions, date = System.currentTimeMillis())
            )
            
            // 2. Check for achievements and update XP
            val currentUser = userDao.getCurrentUser().first()
            if (currentUser != null) {
                val xpEarned = score * 10
                val newXp = currentUser.xp + xpEarned
                val currentAchievements = currentUser.unlockedAchievements.split(",").filter { it.isNotBlank() }.toMutableSet()
                val newlyUnlocked = mutableListOf<String>()

                if (!currentAchievements.contains("first_quiz")) {
                    currentAchievements.add("first_quiz")
                    newlyUnlocked.add("🏆 First Step: Completed your first quiz!")
                }
                if (score == totalQuestions && totalQuestions >= 5) {
                    if (!currentAchievements.contains("perfect_score")) {
                        currentAchievements.add("perfect_score")
                        newlyUnlocked.add("🌟 Perfect Score: Got 100% on a quiz!")
                    }
                }
                if (newXp >= 100 && !currentAchievements.contains("xp_100")) {
                    currentAchievements.add("xp_100")
                    newlyUnlocked.add("⚡ Rising Star: Reached 100 XP!")
                }

                val updatedUser = currentUser.copy(xp = newXp, unlockedAchievements = currentAchievements.joinToString(","))
                userDao.insertUser(updatedUser)

                for (achievement in newlyUnlocked) {
                    _achievementEvent.emit(achievement)
                    Log.d("GCE_VAULT_ACHIEVEMENT", "Unlocked: $achievement")
                }
            }
        }
    }
}