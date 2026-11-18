package com.gcevault.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class FireQuestion(
    val id: String = "",
    val subject: String = "",
    val year: String = "",
    val paper: String = "",
    val brain: String = "",
    val questionNumber: String = "",
    val questionText: String = "",
    val topics: List<String> = emptyList(),
    val pdfUrl: String = "",
    val type: String = "essay",
    val options: Map<String, String> = emptyMap()
)

class QuestionRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collection = "questions"

    suspend fun fetchQuestionsBySubject(subject: String): List<FireQuestion> {
        return try {
            val variations = listOf(
                subject, "$subject June", "$subject Ol", "$subject Mcq", "$subject Mcq June",
                "$subject (Mcq)", "$subject (Mcq) June", "Ordinary Level $subject June",
                "Ordinary Level $subject (Mcq) June", "$subject () June",
                "Advanced Level $subject", "A-Level $subject", "O-Level $subject"
            ).distinct()

            val snapshot = db.collection(collection)
                .whereIn("subject", variations)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                mapDocumentToQuestion(doc.id, doc.data ?: emptyMap())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchByExactTopic(topic: String): List<FireQuestion> {
        return try {
            val snapshot = db.collection(collection)
                .whereArrayContains("topics", topic)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                mapDocumentToQuestion(doc.id, doc.data ?: emptyMap())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun mapDocumentToQuestion(docId: String, data: Map<String, Any>): FireQuestion {
        val rawOptions = data["options"]
        val parsedOptions: Map<String, String> = try {
            when (rawOptions) {
                is Map<*, *> -> rawOptions.entries.associate { it.key.toString() to it.value.toString() }
                is List<*> -> rawOptions.mapIndexed { i, v -> ('A' + i).toString() to (v?.toString() ?: "") }.toMap()
                else -> emptyMap()
            }
        } catch (e: Exception) { emptyMap() }

        val rawTopics = data["topics"]
        val parsedTopics: List<String> = try {
            when (rawTopics) {
                is List<*> -> rawTopics.mapNotNull { it?.toString() }
                is String -> listOf(rawTopics)
                else -> emptyList()
            }
        } catch (e: Exception) { emptyList() }

        return FireQuestion(
            id = docId,
            subject = data["subject"] as? String ?: "",
            year = data["year"] as? String ?: "",
            paper = data["paper"] as? String ?: "",
            brain = data["brain"] as? String ?: "",
            questionNumber = data["question_number"] as? String ?: "",
            questionText = data["question_text"] as? String ?: "",
            topics = parsedTopics,
            pdfUrl = data["pdf_url"] as? String ?: "",
            type = data["type"] as? String ?: "essay",
            options = parsedOptions
        )
    }
}
