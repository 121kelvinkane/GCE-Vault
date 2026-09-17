@file:OptIn(ExperimentalMaterial3Api::class)

package com.gcevault.app.ui.questionbank

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.gcevault.app.ads.RewardedAdHelper
import com.google.firebase.firestore.FirebaseFirestore

data class PdfResource(val id: String = "", val brain: String = "", val subject: String = "", val year: String = "", val paper: String = "", val type: String = "", val downloadUrl: String = "")

@Composable
fun QuestionBankScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val savedLevel = prefs.getString("user_level", "O-Level") ?: "O-Level"
    val savedStream = prefs.getString("user_stream", "Arts") ?: "Arts"
    
    var searchQuery by remember { mutableStateOf("") }
    val subjects = getSubjectsForLevel(savedLevel, savedStream)
    val filteredSubjects = subjects.filter { it.contains(searchQuery, ignoreCase = true) }
    
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var selectedYear by remember { mutableStateOf<String?>(null) }
    var pdfs by remember { mutableStateOf<List<PdfResource>>(emptyList()) }
    var isLoadingPdfs by remember { mutableStateOf(false) }
    var selectedPdfUrl by remember { mutableStateOf<String?>(null) }
    var showAdDialog by remember { mutableStateOf(false) }
    var pendingSubject by remember { mutableStateOf<String?>(null) }
    var pendingYear by remember { mutableStateOf<String?>(null) }

    if (selectedPdfUrl != null) {
        PdfViewerScreen(pdfUrl = selectedPdfUrl!!, onBack = { selectedPdfUrl = null })
        return
    }

    val PurplePrimary = Color(0xFF7427E8)
    val PurpleSoft = Color(0xFFF3F0FF)

    // ✅ Pre-load QBank ad when screen opens for instant display
    LaunchedEffect(Unit) {
        RewardedAdHelper.loadQBankAd(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Question Bank", fontWeight = FontWeight.Bold, color = Color.White); Text("$savedLevel • $savedStream", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f)) } },
                navigationIcon = { IconButton(onClick = { when { selectedPdfUrl != null -> selectedPdfUrl = null; selectedYear != null -> { selectedYear = null; isLoadingPdfs = false }; selectedSubject != null -> selectedSubject = null; else -> onBack() } }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBFAFF))
                .padding(padding)
                .padding(bottom = 100.dp)
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
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)) {
                        if (filteredSubjects.isNotEmpty()) {
                            items(filteredSubjects) { subject -> SubjectCard(subject = subject, PurplePrimary = PurplePrimary, PurpleSoft = PurpleSoft, onClick = { pendingSubject = subject; showAdDialog = true }) }
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
                    Text("Select Year for $selectedSubject", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp), color = Color(0xFF17134B))
                    val years = (2015..2026).map { it.toString() }.reversed()
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)) {
                        items(years) { year -> SubjectCard(subject = year, PurplePrimary = PurplePrimary, PurpleSoft = PurpleSoft, onClick = { pendingYear = year; showAdDialog = true }) }
                    }
                }
                else -> {
                    if (isLoadingPdfs) { 
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurplePrimary) } 
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
                        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)) {
                            items(pdfs) { pdf -> PdfCard(pdf = pdf, PurplePrimary = PurplePrimary, onClick = { selectedPdfUrl = pdf.downloadUrl }) }
                        }
                    }
                }
            }
        }
    }

    // ✅ NEW: ONE AD TO UNLOCK (not 3!)
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
                    val activity = context as? Activity
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
                                // ✅ STUDENT-FIRST: Ad failed? Unlock anyway!
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
            dismissButton = { TextButton(onClick = { showAdDialog = false; pendingSubject = null; pendingYear = null }) { Text("Cancel", color = Color(0xFF514D83)) } }
        )
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PdfViewerScreen(pdfUrl: String, onBack: () -> Unit) {
    val viewerUrl = "https://docs.google.com/gview?embedded=true&url=$pdfUrl"
    Scaffold(topBar = { 
        TopAppBar(
            title = { Text("Viewing PDF", fontWeight = FontWeight.Bold, color = Color.White) }, 
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } }, 
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF7427E8))
        ) 
    }) { padding ->
        AndroidView(factory = { context -> 
            WebView(context).apply { 
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl(viewerUrl) 
            } 
        }, modifier = Modifier.fillMaxSize().padding(padding))
    }
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
    FirebaseFirestore.getInstance().collection("pdf_resources").whereEqualTo("brain", brain).whereEqualTo("subject", subject).whereEqualTo("year", year).get()
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