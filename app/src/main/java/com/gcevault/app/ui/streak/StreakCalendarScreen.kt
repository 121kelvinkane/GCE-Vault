package com.gcevault.app.ui.streak

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// ✅ NEW PURPLE THEME COLORS
private val PurplePrimary = Color(0xFF7427E8)
private val PurpleDark = Color(0xFF32136F)
private val PurpleSoft = Color(0xFFF3F0FF)
private val BackgroundColor = Color(0xFFFBFAFF)
private val TextDark = Color(0xFF17134B)
private val TextGray = Color(0xFF514D83)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakCalendarScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    val completedDates = prefs.getStringSet("completed_quiz_dates", emptySet()) ?: emptySet()
    val currentStreak = calculateCurrentStreak(completedDates)
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streak Calendar", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ HERO STREAK CARD (Purple Gradient)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(PurplePrimary, PurpleDark)))
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔥", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("$currentStreak", fontSize = 56.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Day Streak", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (currentStreak == 0) "Start your streak today!" 
                            else if (currentStreak == 1) "Great start! Keep it going!"
                            else "Amazing! You're on fire!",
                            fontSize = 14.sp, 
                            color = Color.White.copy(alpha = 0.8f), 
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // ✅ MONTH NAVIGATION
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Month", tint = TextDark)
                }
                Text(
                    currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")), 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = TextDark
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Month", tint = TextDark)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // ✅ DAYS OF WEEK HEADER
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        day, 
                        modifier = Modifier.weight(1f), 
                        textAlign = TextAlign.Center, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = TextGray
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // ✅ CALENDAR GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth().height(350.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val firstDayOfMonth = currentMonth.atDay(1)
                val daysInMonth = currentMonth.lengthOfMonth()
                val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
                
                items(firstDayOfWeek) { Box(modifier = Modifier.aspectRatio(1f)) }
                
                items(daysInMonth) { index ->
                    val day = index + 1
                    val date = currentMonth.atDay(day)
                    val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val isCompleted = dateString in completedDates
                    val isToday = date == LocalDate.now()
                    
                    DayCell(day = day, isCompleted = isCompleted, isToday = isToday)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // ✅ LEGEND
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(16.dp).background(PurpleSoft, RoundedCornerShape(4.dp)), 
                        contentAlignment = Alignment.Center
                    ) { Text("🔥", fontSize = 10.sp) }
                    Spacer(Modifier.width(8.dp))
                    Text("Completed", fontSize = 12.sp, color = TextGray)
                }
                Spacer(Modifier.width(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(16.dp).background(PurplePrimary, RoundedCornerShape(4.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text("Today", fontSize = 12.sp, color = TextGray)
                }
            }
        }
    }
}

@Composable
fun DayCell(day: Int, isCompleted: Boolean, isToday: Boolean) {
    val backgroundColor = when {
        isToday -> PurplePrimary
        isCompleted -> PurpleSoft
        else -> Color.White
    }
    
    val textColor = when {
        isToday -> Color.White
        isCompleted -> PurplePrimary
        else -> TextDark
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isToday) Modifier.border(2.dp, PurplePrimary, RoundedCornerShape(8.dp)) 
                else Modifier
            )
            .background(backgroundColor), 
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Text("🔥", fontSize = 18.sp)
        } else {
            Text(
                day.toString(), 
                fontSize = 14.sp, 
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, 
                color = textColor
            )
        }
    }
}

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