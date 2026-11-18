package com.gcevault.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1,
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val level: String = "",
    val stream: String = "",
    val xp: Int = 0,
    val streak: Int = 0,
    val unlockedAchievements: String = ""
)
