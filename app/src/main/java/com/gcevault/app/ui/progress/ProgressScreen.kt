package com.gcevault.app.ui.progress

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gcevault.app.viewmodel.ProgressViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ✅ CONSISTENT PURPLE THEME COLORS
private val PurplePrimary = Color(0xFF7427E8)
private val PurpleSoft = Color(0xFFF3F0FF)
private val BackgroundColor = Color(0xFFFBFAFF)
private val TextDark = Color(0xFF17134B)
private val TextGray = Color(0xFF514D83)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit = {},
    viewModel: ProgressViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    val level = prefs.getString("user_level", "O-Level") ?: "O-Level"
    val stream = prefs.getString("user_stream", "Arts") ?: "Arts"
    val userBrain = getUserBrain(level, stream)
    
    // Dynamic subjects based on Brain
    val expectedSubjects = getSubjectsForLevel(level, stream)
    
    // ✅ OBSERVE REAL DATA DIRECTLY FROM VIEWMODEL
    val allResults by viewModel.allResults.collectAsState()
    val totalXp by viewModel.totalXp.collectAsState() // Single source of truth
    
    // Calculate Streak locally from dates
    val completedDates = prefs.getStringSet("completed_quiz_dates", emptySet()) ?: emptySet()
    val currentStreak = calculateCurrentStreak(completedDates)

    // Calculate total ads watched from SharedPreferences
    var totalAdsWatched by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        var adsCount = 0
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("ads_watched_") && value is Int) {
                adsCount += value
            }
        }
        totalAdsWatched = adsCount
    }

    // ✅ CALCULATE REAL SUBJECT MASTERY FROM DATABASE RESULTS
    val subjectProgressMap = remember(allResults) {
        val map = mutableMapOf<String, Pair<Int, Int>>() // subject -> (correct, total)
        for (result in allResults) {
            val existing = map[result.subject] ?: Pair(0, 0)
            map[result.subject] = Pair(existing.first + result.score, existing.second + result.totalQuestions)
        }
        map.mapValues { (_, value) ->
            if (value.second > 0) value.first.toFloat() / value.second.toFloat() else 0f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Progress & Analytics", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(userBrain, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding)
                .padding(bottom = 100.dp)
        ) {
            // ✅ STATS HEADER (XP, Streak, Ads Watched)
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Total XP", value = "$totalXp", icon = Icons.Default.Star, modifier = Modifier.weight(1f))
                StatCard(title = "Day Streak", value = "$currentStreak", icon = Icons.Default.LocalFireDepartment, modifier = Modifier.weight(1f))
                StatCard(title = "Ads Watched", value = "$totalAdsWatched", icon = Icons.Default.PlayCircle, modifier = Modifier.weight(1f))
            }

            Text("Subject Mastery", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expectedSubjects) { subject ->
                    // If we have data for this subject, show it. Otherwise, show 0%
                    val progress = subjectProgressMap[subject] ?: 0f
                    SubjectProgressCard(subject = subject, progress = progress)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, title, tint = PurplePrimary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(title, fontSize = 12.sp, color = TextGray)
        }
    }
}

@Composable
fun SubjectProgressCard(subject: String, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(subject, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("${(progress * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
            }
            Spacer(Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = PurplePrimary,
                trackColor = PurpleSoft,
                strokeCap = StrokeCap.Round
            )
            
            // ✅ Show "No data yet" for subjects the user hasn't touched
            if (progress == 0f) {
                Spacer(Modifier.height(6.dp))
                Text("No data yet. Take a quiz!", fontSize = 11.sp, color = TextGray)
            }
        }
    }
}

// ✅ REUSED HELPER FUNCTIONS
fun calculateCurrentStreak(completedDates: Set<String>): Int {
    if (completedDates.isEmpty()) return 0
    var streak = 0
    var currentDate = LocalDate.now()
    val todayString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    if (todayString !in completedDates) {
        currentDate = currentDate.minusDays(1)
    }
    while (true) {
        val dateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if (dateString in completedDates) {
            streak++
            currentDate = currentDate.minusDays(1)
        } else {
            break
        }
    }
    return streak
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