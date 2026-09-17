package com.gcevault.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gcevault.app.database.AppDatabase
import com.gcevault.app.database.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    
    // Get the database instance
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()

    // Observe the current user. This will automatically update the UI when the user changes!
    val currentUser = userDao.getCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Function to save or update the user profile
    fun saveUser(username: String, email: String, phone: String, level: String, stream: String) {
        viewModelScope.launch {
            val user = User(
                id = 1, // We only have one local user profile
                username = username,
                email = email,
                phone = phone,
                level = level,
                stream = stream,
                xp = 0,
                streak = 0
            )
            userDao.insertUser(user)
        }
    }

    // Function to add XP (e.g., after a quiz)
    fun addXp(amount: Int) {
        viewModelScope.launch {
            userDao.addXp(amount)
        }
    }

    // Function to update the daily streak
    fun updateStreak(newStreak: Int) {
        viewModelScope.launch {
            userDao.updateStreak(newStreak)
        }
    }
}