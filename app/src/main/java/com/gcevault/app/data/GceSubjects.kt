package com.gcevault.app.data

object GceSubjects {
    val oLevelScience = listOf("Mathematics", "Additional Mathematics", "Biology", "Human Biology", "Chemistry", "Physics", "Computer Science", "English Language", "French", "Geography", "Economics", "Citizenship")
    val oLevelArts = listOf("English Language", "French", "Literature in English", "History", "Geography", "Economics", "Commerce", "Mathematics", "Citizenship")
    val aLevelScience = listOf("Biology", "Chemistry", "Physics", "Pure Mathematics with Mechanics", "Pure Mathematics with Statistics", "Further Mathematics", "Computer Science", "Information and Communication Technology (ICT)", "Food Science and Nutrition", "Geology")
    val aLevelArts = listOf("English Language", "French", "Literature in English", "History", "Geography", "Economics", "Mathematics with Statistics", "Philosophy", "Religious Studies", "Accounting")

    fun getSubjectsFor(level: String, stream: String): List<String> {
        return when {
            level == "O-Level" && stream == "Science" -> oLevelScience
            level == "O-Level" && stream == "Arts" -> oLevelArts
            level == "A-Level" && stream == "Science" -> aLevelScience
            level == "A-Level" && stream == "Arts" -> aLevelArts
            else -> emptyList()
        }
    }
}