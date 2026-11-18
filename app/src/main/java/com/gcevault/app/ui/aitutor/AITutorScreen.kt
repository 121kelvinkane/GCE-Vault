package com.gcevault.app.ui.aitutor

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gcevault.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String,
    val content: String
)

val PurplePrimary = Color(0xFF7427E8)
val PurpleDark = Color(0xFF32136F)
val PurpleSoft = Color(0xFFF3F0FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITutorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val savedLevel = prefs.getString("user_level", "O-Level") ?: "O-Level"
    val savedStream = prefs.getString("user_stream", "Arts") ?: "Arts"
    
    val isO = savedLevel.contains("O-Level", ignoreCase = true)
    val isA = savedLevel.contains("A-Level", ignoreCase = true)
    val isArts = savedStream.contains("Arts", ignoreCase = true) || savedStream.contains("Art", ignoreCase = true)
    val isScience = savedStream.contains("Science", ignoreCase = true) || savedStream.contains("Sci", ignoreCase = true)
    val systemPrompt = when {
        isO && isArts -> "You are an expert GCE O-Level Arts tutor. Focus on clear essay structures, historical facts, geographical case studies, literature analysis, citizenship concepts, and economics principles. Keep answers concise, accurate, and encouraging. IMPORTANT: Never use LaTeX, backslashes, or math symbols like \\( \\), \\frac, \\text. Use plain text only. Write 'x=2' not '\\(x=2\\)', write '2.3/22.99' not fractions."
        isO && isScience -> "You are an expert GCE O-Level Science tutor. Focus on step-by-step mathematical calculations, scientific formulas, biology processes, chemistry equations, and physics laws. Always show your working clearly. IMPORTANT: Never use LaTeX, backslashes, or math symbols like \\( \\), \\frac, \\text, \\times. Use plain text only. Write 'H2O' not 'H₂O', write '2.3/22.99' not fractions, write 'x=2' not '\\(x=2\\)'."
        isA && isScience -> "You are an expert GCE A-Level Science tutor. Focus on advanced calculus, further mathematics, complex biology/chemistry/physics concepts, and computer science/ICT algorithms. Provide rigorous, step-by-step academic explanations. IMPORTANT: Never use LaTeX, backslashes, or math symbols like \\( \\), \\frac, \\text, \\times. Use plain text only. Write 'x^2' for powers, write '2.3/22.99' not fractions, write 'x=2' not '\\(x=2\\)'."
        isA && isArts -> "You are an expert GCE A-Level Arts tutor. Focus on advanced economic theories, deep historical analysis, advanced literature critique, formal essay writing, and French grammar. Provide high-level academic responses. IMPORTANT: Never use LaTeX or special formatting. Use plain text only."
        else -> "You are an expert GCE Tutor for O-Level and A-Level students. Answer questions clearly, provide step-by-step solutions, and be encouraging. IMPORTANT: Never use LaTeX, backslashes, or math symbols like \\( \\), \\frac, \\text. Use plain text only. Write 'x=2' not '\\(x=2\\)', write '2.3/22.99' not fractions."
    }

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("AI Tutor", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("$savedLevel • $savedStream", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 100.dp) 
                .background(Color(0xFFFBFAFF))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val introMessage = when {
                                isA && isArts -> "Hello! I am your A-Level GCE AI Tutor. I am here to help you master advanced concepts, develop critical thinking, and excel in your exams. What topic would you like to explore?"
                                isA && isScience -> "Hello! I am your A-Level GCE AI Tutor. I am here to help you master advanced concepts, develop critical thinking, and excel in your exams. What topic would you like to explore?"
                                isO && isArts -> "Hello! I am your O-Level GCE AI Tutor. I am here to help you understand key concepts, practice exam techniques, and boost your confidence. What subject would you like to study?"
                                isO && isScience -> "Hello! I am your O-Level GCE AI Tutor. I am here to help you understand key concepts, practice exam techniques, and boost your confidence. What subject would you like to study?"
                                else -> "Hello! I am your GCE AI Tutor. I am here to help you understand concepts, practice effectively, and excel in your exams. What would you like to learn?"
                            }
                            
                            Text(
                                introMessage, 
                                modifier = Modifier.padding(16.dp),
                                fontSize = 15.sp, 
                                color = Color(0xFF17134B),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                items(messages) { message ->
                    ChatBubble(message = message)
                }

                if (isLoading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Surface(
                                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                                color = Color.White,
                                tonalElevation = 2.dp
                            ) {
                                Text("Thinking...", modifier = Modifier.padding(12.dp), color = Color(0xFF514D83), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask a question...", color = Color(0xFF514D83)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = PurpleSoft,
                            unfocusedContainerColor = PurpleSoft,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    val currentMsg = inputText
                                    inputText = ""
                                    isLoading = true
                                    scope.launch {
                                        sendMessage(currentMsg, systemPrompt, messages) { newMessages ->
                                            messages = newMessages
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val currentMsg = inputText
                                inputText = ""
                                isLoading = true
                                scope.launch {
                                    sendMessage(currentMsg, systemPrompt, messages) { newMessages ->
                                        messages = newMessages
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (inputText.isNotBlank() && !isLoading) PurplePrimary else Color.Gray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            "Send",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) PurplePrimary else Color.White,
            tonalElevation = 2.dp
        ) {
            val cleanText = if (isUser) message.content else cleanMarkdown(message.content)
            Text(
                text = cleanText,
                modifier = Modifier.padding(12.dp),
                fontSize = 15.sp,
                color = if (isUser) Color.White else Color(0xFF17134B),
                lineHeight = 22.sp
            )
        }
    }
}

fun cleanMarkdown(text: String): String {
    return text
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        .replace(Regex("\\*(.+?)\\*"), "$1")
        .replace(Regex("__(.+?)__"), "$1")
        .replace(Regex("_(.+?)_"), "$1")
        .replace(Regex("### (.+)"), "$1")
        .replace(Regex("## (.+)"), "$1")
        .replace(Regex("# (.+)"), "$1")
        .replace(Regex("```(.+?)```", RegexOption.DOT_MATCHES_ALL), "$1")
        .replace(Regex("`(.+?)`"), "$1")
        .replace(Regex("\\n\\n+"), "\n")
        .trim()
}

// ✅ FIXED: Now sends the UPDATED message list (including the new user message)
suspend fun sendMessage(
    userMessage: String, 
    systemPrompt: String,
    currentMessages: List<ChatMessage>,
    onResult: (List<ChatMessage>) -> Unit
) {
    val groqApiKey = BuildConfig.GROQ_API_KEY 
    
    // 1. Create the updated list WITH the new user message
    val updatedMessages = currentMessages + ChatMessage("user", userMessage)
    
    // 2. Update UI immediately so the user sees their message
    onResult(updatedMessages)

    withContext(Dispatchers.IO) {
        try {
            if (groqApiKey.isBlank() || groqApiKey.contains("YOUR_REAL_GROQ_KEY_HERE")) {
                throw Exception("API key not set! Please add your Groq API key to local.properties.")
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val messagesArray = JSONArray()
            
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })

            // 3. FIXED: Send the UPDATED messages (which includes the new user message)
            val historyToSend = updatedMessages.takeLast(10)
            for (msg in historyToSend) {
                messagesArray.put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }

            val jsonBody = JSONObject().apply {
                // ✅ CONFIRMED WORKING MODEL
                put("model", "openai/gpt-oss-20b") 
                put("messages", messagesArray)
                put("temperature", 0.7)
                put("max_tokens", 1024)
            }

            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer $groqApiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                if (responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val choices = jsonResponse.getJSONArray("choices")
                        if (choices.length() > 0) {
                            val message = choices.getJSONObject(0).getJSONObject("message")
                            val aiResponse = message.getString("content")
                            withContext(Dispatchers.Main) {
                                onResult(updatedMessages + ChatMessage("assistant", aiResponse))
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onResult(updatedMessages + ChatMessage("assistant", "Error: No response from AI"))
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            onResult(updatedMessages + ChatMessage("assistant", "Error parsing response: ${e.message}"))
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(updatedMessages + ChatMessage("assistant", "Error: Empty response from server"))
                    }
                }
            } else {
                val errorMsg = "Error ${response.code}: ${responseBody ?: "Request failed"}"
                withContext(Dispatchers.Main) {
                    onResult(updatedMessages + ChatMessage("assistant", errorMsg))
                }
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("API key") == true -> e.message!!
                e is java.net.UnknownHostException -> "Network Error: No internet connection. Please check your connection."
                e is java.net.SocketTimeoutException -> "Network Error: Request timed out. Please try again."
                else -> "Network Error: ${e.message ?: "Unknown error"}"
            }
            withContext(Dispatchers.Main) {
                onResult(updatedMessages + ChatMessage("assistant", errorMessage))
            }
        }
    }
}