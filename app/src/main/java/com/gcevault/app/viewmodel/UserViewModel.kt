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
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()

    val currentUser = userDao.getCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun saveUser(username: String, email: String, phone: String, level: String, stream: String) {
        viewModelScope.launch {
            val user = User(
                id = 1,
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

    fun addXp(amount: Int) {
        viewModelScope.launch {
            userDao.addXp(amount)
        }
    }

    fun updateStreak(newStreak: Int) {
        viewModelScope.launch {
            userDao.updateStreak(newStreak)
        }
    }
}
