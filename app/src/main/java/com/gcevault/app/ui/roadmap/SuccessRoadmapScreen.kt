package com.gcevault.app.ui.roadmap

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

// FIXED RANGE: September 2026 to December 2027
val FULL_ACTIVITIES_RANGE: List<YearMonth> = buildList {
    var current = YearMonth.of(2026, 9)
    val end = YearMonth.of(2027, 12)
    while (!current.isAfter(end)) {
        add(current)
        current = current.plusMonths(1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessRoadmapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    var checkedDates by remember { 
        mutableStateOf(
            prefs.getStringSet("success_roadmap_dates", emptySet())?.toMutableSet() ?: mutableSetOf()
        )
    }
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showFullYear by remember { mutableStateOf(false) }
    val totalDaysStudied = checkedDates.size
    val today = LocalDate.now()
    
    // ✅ NEW: Dialog state for confirmation
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingDateToToggle by remember { mutableStateOf<LocalDate?>(null) }
    
    // Helper to handle the toggle logic
    val handleDateToggle = { date: LocalDate ->
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val newCheckedDates = checkedDates.toMutableSet()
        if (dateString in checkedDates) {
            newCheckedDates.remove(dateString)
        } else {
            newCheckedDates.add(dateString)
        }
        checkedDates = newCheckedDates
        prefs.edit().putStringSet("success_roadmap_dates", newCheckedDates).apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Study Tracker", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(BackgroundColor).padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // ✅ PREMIUM PURPLE GRADIENT CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(PurplePrimary, PurpleDark)), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("$totalDaysStudied", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Days Studied", fontSize = 18.sp, color = Color.White.copy(alpha = 0.9f))
                        Spacer(Modifier.height(8.dp))
                        Text(getMotivationalMessage(totalDaysStudied), fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                    }
                }
            }
            
            if (showFullYear) {
                items(FULL_ACTIVITIES_RANGE.size) { index ->
                    val month = FULL_ACTIVITIES_RANGE[index]
                    MonthCard(
                        month = month,
                        checkedDates = checkedDates,
                        today = today,
                        onDateClick = { date ->
                            pendingDateToToggle = date
                            showConfirmDialog = true
                        }
                    )
                }
            } else {
                item {
                    MonthCard(
                        month = currentMonth,
                        checkedDates = checkedDates,
                        today = today,
                        onDateClick = { date ->
                            pendingDateToToggle = date
                            showConfirmDialog = true
                        },
                        showNavigation = true,
                        onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                        onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(20.dp).background(PurpleSoft, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, null, tint = PurplePrimary, modifier = Modifier.size(14.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Studied", fontSize = 12.sp, color = TextGray)
                        }
                        Spacer(Modifier.width(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(20.dp).background(Color.White, RoundedCornerShape(4.dp)).border(1.5.dp, PurplePrimary, RoundedCornerShape(4.dp)))
                            Spacer(Modifier.width(8.dp))
                            Text("Today", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { showFullYear = !showFullYear }, 
                    modifier = Modifier.fillMaxWidth(), 
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary), 
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (showFullYear) "Show Current Month" else "See Full Activities", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // ✅ CONFIRMATION DIALOG
    if (showConfirmDialog && pendingDateToToggle != null) {
        AlertDialog(
            onDismissRequest = { 
                showConfirmDialog = false
                pendingDateToToggle = null
            },
            icon = { 
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(48.dp)) 
            },
            title = { Text("Confirm Completion", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you accomplished all your goals for today?") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDateToToggle?.let { handleDateToggle(it) }
                    showConfirmDialog = false
                    pendingDateToToggle = null
                }) {
                    Text("Yes", color = PurplePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    pendingDateToToggle = null
                }) {
                    Text("No", color = TextGray)
                }
            }
        )
    }
}

@Composable
fun MonthCard(
    month: YearMonth,
    checkedDates: Set<String>,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    showNavigation: Boolean = false,
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {}
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (showNavigation) IconButton(onClick = onPreviousMonth) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Month", tint = TextDark) } else Spacer(Modifier.width(48.dp))
                Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                if (showNavigation) IconButton(onClick = onNextMonth) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Month", tint = TextDark) } else Spacer(Modifier.width(48.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day -> Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray) }
            }
            Spacer(Modifier.height(8.dp))
            val firstDayOfMonth = month.atDay(1)
            val daysInMonth = month.lengthOfMonth()
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
            val totalCells = firstDayOfWeek + daysInMonth
            val numRows = (totalCells + 6) / 7
            for (row in 0 until numRows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayOfMonth = cellIndex - firstDayOfWeek + 1
                        if (dayOfMonth in 1..daysInMonth) {
                            val date = month.atDay(dayOfMonth)
                            val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val isChecked = dateString in checkedDates
                            val isToday = date == today
                            
                            Box(modifier = Modifier.weight(1f)) {
                                ClickableDayCell(day = dayOfMonth, isChecked = isChecked, isToday = isToday, onClick = { if (isToday) onDateClick(date) })
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun ClickableDayCell(day: Int, isChecked: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isChecked -> PurpleSoft
        isToday -> Color.White
        else -> Color.White.copy(alpha = 0.5f) // Greyed out
    }
    val textColor = if (isToday || isChecked) TextDark else TextGray.copy(alpha = 0.5f)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .alpha(if (isToday || isChecked) 1.0f else 0.6f)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.5.dp, if (isToday) PurplePrimary else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(enabled = isToday) { onClick() } // ONLY TODAY IS ENABLED
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(day.toString(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(if (isChecked) PurplePrimary else Color.Transparent, RoundedCornerShape(4.dp))
                .border(1.5.dp, if (isToday) PurplePrimary else TextGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) Icon(Icons.Default.Check, "Checked", tint = Color.White, modifier = Modifier.size(12.dp))
        }
    }
}

fun getMotivationalMessage(days: Int): String {
    return when {
        days == 0 -> "Start tracking your study days!"
        days < 5 -> "Great start! Keep going!"
        days < 10 -> "You're building momentum!"
        days < 30 -> "Amazing progress! You're on fire!"
        days < 100 -> "Outstanding dedication! Keep it up!"
        else -> "You're unstoppable! A true GCE champion!"
    }
}