package com.gcevault.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gcevault.app.data.local.dao.QuestionDao
import com.gcevault.app.data.local.entity.QuestionEntity

@Database(entities = [QuestionEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gce_vault_database"
                )
                .fallbackToDestructiveMigration()
                .createFromAsset("gce_questions.db") // THIS LOADS YOUR 20,000 QUESTIONS!
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}