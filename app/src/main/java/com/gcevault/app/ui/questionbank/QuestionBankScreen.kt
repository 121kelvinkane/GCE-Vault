@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.gcevault.app.ui.questionbank

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.gcevault.app.ads.RewardedAdHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.github.barteksc.pdfviewer.PDFView
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class PdfResource(
    val id: String = "", val brain: String = "", val subject: String = "", 
    val year: String = "", val paper: String = "", val type: String = "", val downloadUrl: String = ""
)

data class ExamQuestion(
    val id: String = "",
    val subject: String = "",
    val year: String = "",
    val paper: String = "",
    val questionNumber: String = "",
    val questionText: String = "",
    val topics: List<String> = emptyList(),
    val type: String = "essay",
    val options: Map<String, String>? = null,
    val pdfUrl: String = ""
)

@Composable
fun QuestionBankScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val savedLevel = prefs.getString("user_level", "O-Level") ?: "O-Level"
    val savedStream = prefs.getString("user_stream", "Arts") ?: "Arts"
    
    var searchQuery by remember { mutableStateOf("") }
    var searchTopic by remember { mutableStateOf("") }
    
    val subjects = getSubjectsForLevel(savedLevel, savedStream)
    val filteredSubjects = remember(searchQuery, subjects) { 
        subjects.filter { it.contains(searchQuery, ignoreCase = true) } 
    }
    
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var selectedYear by remember { mutableStateOf<String?>(null) }
    var pdfs by remember { mutableStateOf<List<PdfResource>>(emptyList()) }
    var isLoadingPdfs by remember { mutableStateOf(false) }
    var selectedPdfUrl by remember { mutableStateOf<String?>(null) }
    var showAdDialog by remember { mutableStateOf(false) }
    var pendingSubject by remember { mutableStateOf<String?>(null) }
    var pendingYear by remember { mutableStateOf<String?>(null) }

    var allSubjectQuestions by remember { mutableStateOf<List<ExamQuestion>>(emptyList()) }
    var isLoadingAllQuestions by remember { mutableStateOf(false) }

    // Fetch questions when subject changes
    LaunchedEffect(selectedSubject) {
        if (selectedSubject != null) {
            isLoadingAllQuestions = true
            fetchQuestionsForSubject(selectedSubject!!, savedLevel) { questions ->
                allSubjectQuestions = questions
                isLoadingAllQuestions = false
            }
        } else {
            allSubjectQuestions = emptyList()
        }
    }

    // Extract back navigation logic
    val handleBack = {
        when {
            searchTopic.isNotBlank() -> { searchTopic = "" }
            selectedYear != null -> { selectedYear = null; isLoadingPdfs = false; pdfs = emptyList() }
            selectedSubject != null -> { selectedSubject = null; searchTopic = "" }
            else -> onBack() 
        }
    }

    // Handle PDF viewer separately to avoid early return
    if (selectedPdfUrl != null) {
        PdfViewerScreen(pdfUrl = selectedPdfUrl!!, onBack = { selectedPdfUrl = null })
        BackHandler { selectedPdfUrl = null }
    } else {
        val PurplePrimary = Color(0xFF7427E8)
        val PurpleSoft = Color(0xFFF3F0FF)

        LaunchedEffect(Unit) {
            RewardedAdHelper.loadQBankAd(context)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column { 
                            Text("Question Bank", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("$savedLevel • $savedStream", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f)) 
                        } 
                    },
                    navigationIcon = { 
                        IconButton(onClick = handleBack) { 
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) 
                        } 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary)
                )
            }
        ) { padding ->
            
            BackHandler(onBack = handleBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFBFAFF))
                    .padding(padding)
            ) {
                when {
                    selectedSubject == null -> {
                        Surface(color = Color.White, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, "Search", tint = Color(0xFF514D83), modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                TextField(
                                    value = searchQuery, 
                                    onValueChange = { searchQuery = it }, 
                                    placeholder = { Text("Search subject...", color = Color(0xFF514D83)) }, 
                                    modifier = Modifier.fillMaxWidth(), 
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), 
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), 
                                    keyboardActions = KeyboardActions(onSearch = {})
                                )
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            if (filteredSubjects.isNotEmpty()) {
                                items(filteredSubjects) { subject -> 
                                    SubjectCard(subject = subject, PurplePrimary = PurplePrimary, PurpleSoft = PurpleSoft, onClick = { pendingSubject = subject; showAdDialog = true }) 
                                }
                            } else {
                                item { 
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { 
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                                            Icon(Icons.Default.Search, "No results", tint = Color(0xFF514D83), modifier = Modifier.size(64.dp))
                                            Spacer(Modifier.height(16.dp))
                                            Text("No subjects found", fontSize = 16.sp, color = Color(0xFF514D83)) 
                                        } 
                                    } 
                                }
                            }
                        }
                    }
                    
                    selectedYear == null -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text("Select Year for $selectedSubject", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp), color = Color(0xFF17134B))
                            
                            Surface(color = Color.White, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tag, "Topic", tint = Color(0xFF514D83), modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(12.dp))
                                    TextField(
                                        value = searchTopic, 
                                        onValueChange = { searchTopic = it }, 
                                        placeholder = { Text("Search by topic (e.g., Logic)...", color = Color(0xFF514D83)) }, 
                                        modifier = Modifier.weight(1f), 
                                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), 
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                                    )
                                    if (searchTopic.isNotEmpty()) {
                                        IconButton(onClick = { searchTopic = "" }) {
                                            Icon(Icons.Default.Close, "Clear", tint = Color(0xFF514D83), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }

                            if (searchTopic.isNotBlank()) {
                                if (isLoadingAllQuestions) { 
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                                        CircularProgressIndicator(color = PurplePrimary) 
                                    } 
                                } else {
                                    val filteredQuestions = remember(allSubjectQuestions, searchTopic) {
                                        allSubjectQuestions.filter { q ->
                                            q.topics.any { it.contains(searchTopic, ignoreCase = true) } || 
                                            q.questionText.contains(searchTopic, ignoreCase = true)
                                        }
                                    }

                                    if (filteredQuestions.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) { 
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                                                Icon(Icons.Default.SearchOff, "No results", tint = Color(0xFF514D83), modifier = Modifier.size(64.dp))
                                                Spacer(Modifier.height(16.dp))
                                                Text("No questions found for \"$searchTopic\".", color = Color(0xFF514D83), textAlign = TextAlign.Center) 
                                            } 
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
                                        ) {
                                            items(filteredQuestions, key = { it.id }) { question -> 
                                                QuestionCard(question = question, PurplePrimary = PurplePrimary, PurpleSoft = PurpleSoft, context = context) 
                                            }
                                        }
                                    }
                                }
                            } else {
                                val years = (2015..2026).map { it.toString() }.reversed()
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
                                ) {
                                    items(years) { year -> 
                                        SubjectCard(subject = year, PurplePrimary = PurplePrimary, PurpleSoft = PurpleSoft, onClick = { pendingYear = year; showAdDialog = true }) 
                                    }
                                }
                            }
                        }
                    }
                    
                    else -> {
                        if (isLoadingPdfs) { 
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                                CircularProgressIndicator(color = PurplePrimary) 
                            } 
                        } else if (pdfs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) { 
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                                    Icon(Icons.Default.FolderOff, "No PDFs", tint = Color(0xFF514D83), modifier = Modifier.size(64.dp))
                                    Spacer(Modifier.height(16.dp))
                                    Text("No PDFs uploaded for this year yet.", color = Color(0xFF514D83)) 
                                } 
                            }
                        } else {
                            Text("$selectedSubject - $selectedYear", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp), color = Color(0xFF17134B))
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
                            ) {
                                items(pdfs) { pdf -> 
                                    PdfCard(pdf = pdf, PurplePrimary = PurplePrimary, onClick = { selectedPdfUrl = pdf.downloadUrl }) 
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAdDialog) {
            val itemToUnlock = pendingYear ?: pendingSubject
            AlertDialog(
                onDismissRequest = { showAdDialog = false; pendingSubject = null; pendingYear = null },
                icon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(48.dp)) },
                title = { Text("Unlock $itemToUnlock", fontWeight = FontWeight.Bold) },
                text = { Text("Watch 1 short video to unlock past questions for $itemToUnlock.") },
                confirmButton = {
                    Button(onClick = {
                        showAdDialog = false
                        val activity = context.findActivity()
                        if (activity != null) {
                            RewardedAdHelper.showQBankAd(activity,
                                onReward = {
                                    if (pendingYear != null) { 
                                        selectedYear = pendingYear
                                        isLoadingPdfs = true
                                        fetchPdfs(getUserBrain(savedLevel, savedStream), selectedSubject!!, pendingYear!!) { pdfs = it; isLoadingPdfs = false } 
                                    } else { 
                                        selectedSubject = pendingSubject 
                                    }
                                    Toast.makeText(context, "Unlocked! Loading...", Toast.LENGTH_SHORT).show()
                                    pendingSubject = null; pendingYear = null
                                },
                                onFailed = {
                                    if (pendingYear != null) { 
                                        selectedYear = pendingYear
                                        isLoadingPdfs = true
                                        fetchPdfs(getUserBrain(savedLevel, savedStream), selectedSubject!!, pendingYear!!) { pdfs = it; isLoadingPdfs = false } 
                                    } else { 
                                        selectedSubject = pendingSubject 
                                    }
                                    Toast.makeText(context, "Ad unavailable, but we unlocked it for you! Keep learning.", Toast.LENGTH_LONG).show()
                                    pendingSubject = null; pendingYear = null
                                }
                            )
                        } else { 
                            pendingSubject = null; pendingYear = null 
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.PlayCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Watch Video")
                    }
                },
                dismissButton = { 
                    TextButton(onClick = { showAdDialog = false; pendingSubject = null; pendingYear = null }) { 
                        Text("Cancel", color = Color(0xFF514D83)) 
                    } 
                }
            )
        }
    }
}

@Composable
fun QuestionCard(question: ExamQuestion, PurplePrimary: Color, PurpleSoft: Color, context: Context) {
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = PurpleSoft, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "${question.year} • ${question.paper}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("Q${question.questionNumber}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF514D83))
                
                if (question.type == "mcq") {
                    Spacer(Modifier.width(8.dp))
                    Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp)) {
                        Text(text = "MCQ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(10.dp))
            Text(text = question.questionText, fontSize = 15.sp, color = Color(0xFF17134B), lineHeight = 22.sp)
            
            val options = question.options
            if (question.type == "mcq" && !options.isNullOrEmpty()) {
                Spacer(Modifier.height(12.dp))
                options.forEach { (key, value) ->
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                        if (key.isNotBlank()) {
                            Text("$key.", fontWeight = FontWeight.Bold, color = Color(0xFF514D83), modifier = Modifier.width(24.dp))
                        }
                        Text(value, fontSize = 14.sp, color = Color(0xFF17134B), modifier = Modifier.weight(1f))
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            if (question.topics.isNotEmpty()) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    question.topics.filter { it.isNotBlank() }.forEach { topic ->
                        Surface(color = Color(0xFFE8EAF6), shape = RoundedCornerShape(8.dp)) {
                            Text(topic, fontSize = 11.sp, color = Color(0xFF3F51B5),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
            
            if (question.pdfUrl.isNotBlank() && question.pdfUrl != "local-ollama" && question.pdfUrl.startsWith("http")) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(question.pdfUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open PDF link", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17134B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Full PDF for Diagrams")
                }
            }
        }
    }
}

@Composable
fun SubjectCard(subject: String, PurplePrimary: Color, PurpleSoft: Color, onClick: () -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(PurpleSoft, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { 
                Icon(Icons.Default.Folder, "Subject", tint = PurplePrimary, modifier = Modifier.size(28.dp)) 
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { 
                Text(text = subject, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17134B))
                Text(text = "Tap to unlock", fontSize = 14.sp, color = Color(0xFF514D83)) 
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Go", tint = Color(0xFF514D83), modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun PdfCard(pdf: PdfResource, PurplePrimary: Color, onClick: () -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PictureAsPdf, "PDF", tint = Color(0xFFE53935), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column { 
                Text("${pdf.paper} - ${pdf.type}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17134B))
                Text("Tap to open", fontSize = 12.sp, color = Color(0xFF514D83)) 
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.OpenInNew, "Open", tint = PurplePrimary) 
        }
    }
}

@Composable
fun PdfViewerScreen(pdfUrl: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pdfUrl) {
        try {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder().url(pdfUrl.trim()).build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        errorMessage = "Server returned HTTP ${response.code}. The file might not exist."
                        return@withContext
                    }
                    
                    val tempFile = File.createTempFile("gce_vault_pdf_", ".pdf", context.cacheDir)
                    response.body?.byteStream()?.use { inputStream ->
                        tempFile.outputStream().use { output ->
                            inputStream.copyTo(output)
                        }
                    }
                    pdfFile = tempFile
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() 
            val errorType = e::class.simpleName ?: "Unknown Error"
            val errorMsg = e.message ?: "No details provided"
            errorMessage = "Error: $errorType\nDetails: $errorMsg"
        } finally {
            isLoading = false
        }
    }

    Scaffold(topBar = { 
        TopAppBar(
            title = { Text("Viewing PDF", fontWeight = FontWeight.Bold, color = Color.White) }, 
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } }, 
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF7427E8))
        ) 
    }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                isLoading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF7427E8))
                        Spacer(Modifier.height(16.dp))
                        Text("Downloading PDF...", color = Color(0xFF514D83))
                    }
                }
                errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, "Error", tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(errorMessage ?: "Unknown error", color = Color.Red, modifier = Modifier.padding(horizontal = 16.dp), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBack) { Text("Go Back") }
                    }
                }
                pdfFile != null -> {
                    AndroidView(
                        factory = { ctx ->
                            PDFView(ctx, null).apply {
                                fromFile(pdfFile!!)
                                    .defaultPage(0)
                                    .enableSwipe(true)
                                    .enableDoubletap(true)
                                    .enableAnnotationRendering(true)
                                    .load()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

fun fetchQuestionsForSubject(subject: String, level: String, onResult: (List<ExamQuestion>) -> Unit) {
    // Filter variations by level to prevent mixing
    val isOLevel = level.contains("O-Level", ignoreCase = true) || level.contains("O Level", ignoreCase = true)
    
    val variations = if (isOLevel) {
        listOf(
            subject, "$subject June", "$subject Mcq", "$subject Mcq June",
            "$subject (Mcq)", "$subject (Mcq) June", "Ordinary Level $subject June",
            "Ordinary Level $subject (Mcq) June"
        ).distinct().take(10) // Firestore whereIn limit
    } else {
        listOf(
            subject, "$subject June", "Advanced Level $subject", "A-Level $subject"
        ).distinct().take(10)
    }

    FirebaseFirestore.getInstance().collection("questions")
        .whereIn("subject", variations)
        .get()
        .addOnSuccessListener { result -> 
            val fetchedQuestions = result.mapNotNull { doc -> 
                try {
                    // Safely parse all fields using get() instead of getString()
                    val rawOptions = doc.get("options")
                    val parsedOptions: Map<String, String> = try {
                        when (rawOptions) {
                            is Map<*, *> -> rawOptions.entries.associate { it.key.toString() to it.value.toString() }
                            is List<*> -> rawOptions.mapIndexed { i, v -> ('A' + i).toString() to (v?.toString() ?: "") }.toMap()
                            is String -> {
                                // Improved regex that doesn't truncate on A-D in text
                                val parts = rawOptions.split(Regex("(?=\\b[A-D]\\s*[:.)]\\s)")).filter { it.isNotBlank() }
                                if (parts.size > 1) {
                                    parts.associate { part ->
                                        val match = Regex("^([A-D])\\s*[:.)]\\s*(.*)$").find(part.trim())
                                        if (match != null) {
                                            match.groupValues[1] to match.groupValues[2].trim()
                                        } else {
                                            "" to part.trim()
                                        }
                                    }
                                } else {
                                    mapOf("" to rawOptions)
                                }
                            }
                            else -> emptyMap()
                        }
                    } catch (e: Exception) { emptyMap() }

                    val rawTopics = doc.get("topics")
                    val parsedTopics: List<String> = try {
                        when (rawTopics) {
                            is List<*> -> rawTopics.mapNotNull { it?.toString() }
                            is String -> listOf(rawTopics)
                            else -> emptyList()
                        }
                    } catch (e: Exception) { emptyList() }

                    ExamQuestion(
                        id = doc.id,
                        subject = doc.get("subject")?.toString() ?: "",
                        year = doc.get("year")?.toString() ?: "",
                        paper = doc.get("paper")?.toString() ?: "",
                        questionNumber = doc.get("question_number")?.toString() ?: "",
                        questionText = doc.get("question_text")?.toString() ?: "",
                        topics = parsedTopics,
                        type = doc.get("type")?.toString() ?: "essay",
                        options = parsedOptions,
                        pdfUrl = doc.get("pdf_url")?.toString() ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            onResult(fetchedQuestions)
        }
        .addOnFailureListener { onResult(emptyList()) }
}

fun getUserBrain(level: String, stream: String): String {
    val isO = level.contains("O-Level", ignoreCase = true) || level.contains("O Level", ignoreCase = true)
    val isA = level.contains("A-Level", ignoreCase = true) || level.contains("A Level", ignoreCase = true)
    val isArts = stream.contains("Arts", ignoreCase = true) || stream.contains("Art", ignoreCase = true)
    val isScience = stream.contains("Science", ignoreCase = true) || stream.contains("Sci", ignoreCase = true)
    return when { 
        (isO && isArts) -> "O-Level Arts"
        (isO && isScience) -> "O-Level Science"
        (isA && isArts) -> "A-Level Arts"
        (isA && isScience) -> "A-Level Science"
        else -> "General" 
    }
}

fun fetchPdfs(brain: String, subject: String, year: String, onResult: (List<PdfResource>) -> Unit) {
    FirebaseFirestore.getInstance().collection("pdf_resources")
        .whereEqualTo("brain", brain)
        .whereEqualTo("subject", subject)
        .whereEqualTo("year", year)
        .get()
        .addOnSuccessListener { result -> 
            onResult(result.map { doc -> 
                PdfResource(
                    id = doc.id, brain = doc.getString("brain") ?: "", subject = doc.getString("subject") ?: "", 
                    year = doc.getString("year") ?: "", paper = doc.getString("paper") ?: "", type = doc.getString("type") ?: "", downloadUrl = doc.getString("downloadUrl") ?: ""
                ) 
            }) 
        }
        .addOnFailureListener { onResult(emptyList()) }
}

fun getSubjectsForLevel(level: String, stream: String): List<String> {
    val isO = level.contains("O-Level", ignoreCase = true) || level.contains("O Level", ignoreCase = true)
    val isA = level.contains("A-Level", ignoreCase = true) || level.contains("A Level", ignoreCase = true)
    val isArts = stream.contains("Arts", ignoreCase = true) || stream.contains("Art", ignoreCase = true)
    val isScience = stream.contains("Science", ignoreCase = true) || stream.contains("Sci", ignoreCase = true)
    return when {
        (isO && isArts) -> listOf("Mathematics", "English Language", "French", "History", "Geography", "Literature in English", "Citizenship Education", "Economics", "Commerce")
        (isO && isScience) -> listOf("Mathematics", "Additional Mathematics", "Biology", "Human Biology", "Chemistry", "Physics", "Computer Science", "English Language", "French", "Geography", "Economics", "Citizenship Education")
        (isA && isArts) -> listOf("Mathematics", "English Language", "French", "History", "Geography", "Literature in English", "Economics")
        (isA && isScience) -> listOf("Mathematics", "Further Mathematics", "Biology", "Chemistry", "Physics", "Computer Science", "ICT", "Geology")
        isO -> listOf("Mathematics", "English Language", "French", "History", "Geography", "Literature in English", "Citizenship Education", "Economics", "Commerce")
        isA -> listOf("Mathematics", "English Language", "French", "History", "Geography", "Literature in English", "Economics")
        else -> listOf("Mathematics", "English Language", "French", "Biology", "Chemistry", "Physics")
    }
}

// Helper extension to find Activity from Context
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}