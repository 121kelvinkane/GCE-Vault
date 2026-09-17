package com.gcevault.app.ui.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gcevault.app.ui.notifications.NotificationsScreen
import com.gcevault.app.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

val PurplePrimary = Color(0xFF7427E8)
val PurpleDark = Color(0xFF32136F)
val PurpleDeep = Color(0xFF17104E)
val PurpleSoft = Color(0xFFF1EAFF)
private val Lavender = Color(0xFFEDE4FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenStreakCalendar: () -> Unit,
    onOpenSuccessRoadmap: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDailyQuiz: () -> Unit,
    onOpenQBank: () -> Unit,
    onOpenProgress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentUser by viewModel.currentUser.collectAsState()
    val userName = currentUser?.username ?: "Student"
    val userStreak = currentUser?.streak ?: 0
    val roadmapDates = prefs.getStringSet("success_roadmap_dates", emptySet()) ?: emptySet()
    val totalStudyDays = roadmapDates.size

    var showStudyPlan by remember { mutableStateOf(false) }
    var showFaq by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var notificationCount by remember { mutableStateOf(prefs.getInt("notification_count", 1)) }

    val clearNotifications = {
        notificationCount = 0
        prefs.edit().putInt("notification_count", 0).apply()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Menu, "Menu", tint = PurplePrimary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("GCE Vault Menu", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF17134B))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Pass GCE. Not Luck.", fontSize = 14.sp, color = Color(0xFF514D83))
                }
                HorizontalDivider()
                NavigationDrawerItem(icon = { Icon(Icons.Default.Home, null, tint = PurplePrimary) }, label = { Text("Home", fontWeight = FontWeight.Medium) }, selected = true, onClick = { scope.launch { drawerState.close() } })
                NavigationDrawerItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile", fontWeight = FontWeight.Medium) }, selected = false, onClick = { scope.launch { drawerState.close() }; onNavigateToProfile() })
                NavigationDrawerItem(icon = { Icon(Icons.Default.Star, null) }, label = { Text("My Study Plan", fontWeight = FontWeight.Medium) }, selected = false, onClick = { scope.launch { drawerState.close() }; showStudyPlan = true })
                NavigationDrawerItem(icon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text("Streak Calendar", fontWeight = FontWeight.Medium) }, selected = false, onClick = { scope.launch { drawerState.close() }; onOpenStreakCalendar() })
                NavigationDrawerItem(icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Personal Study Tracker", fontWeight = FontWeight.Medium) }, selected = false, onClick = { scope.launch { drawerState.close() }; onOpenSuccessRoadmap() })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Settings", modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                
                // ✅ DARK MODE TOGGLE HAS BEEN REMOVED HERE
                
                NavigationDrawerItem(icon = { Icon(Icons.Default.Info, null) }, label = { Text("FAQ & Help", fontWeight = FontWeight.Medium) }, selected = false, onClick = { scope.launch { drawerState.close() }; showFaq = true })
                NavigationDrawerItem(icon = { Icon(Icons.Default.Warning, null) }, label = { Text("Report a Problem", fontWeight = FontWeight.Medium) }, selected = false, onClick = { scope.launch { drawerState.close() }; showReport = true })
                NavigationDrawerItem(icon = { Icon(Icons.Default.Share, null) }, label = { Text("Share App", fontWeight = FontWeight.Medium) }, selected = false, onClick = { 
                    scope.launch { drawerState.close() }
                    val shareIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, "Check out GCE Vault! The best app to pass your GCE exams. Pass GCE. Not Luck."); type = "text/plain" }
                    context.startActivity(Intent.createChooser(shareIntent, "Share GCE Vault"))
                })
                NavigationDrawerItem(icon = { Icon(Icons.Default.Info, null) }, label = { Text("About Us", fontWeight = FontWeight.Medium) }, selected = false, onClick = { scope.launch { drawerState.close() }; showAbout = true })
            }
        }
    ) {
        Box(modifier = modifier.fillMaxSize().background(Color(0xFFFBFAFF))) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Menu, "Menu", tint = PurpleDeep, modifier = Modifier.size(24.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text("GCE", fontSize = 24.sp, fontWeight = FontWeight.Black, color = PurpleDeep)
                            Text("Vault", fontSize = 24.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                            Box(modifier = Modifier.offset(x = (-3).dp, y = (-10).dp)) {
                                Icon(Icons.Default.School, "Graduation Cap", tint = PurpleDeep, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text("Past Questions. Better Grades.", fontSize = 10.sp, color = Color(0xFF514D83), fontWeight = FontWeight.Medium)
                    }
                    IconButton(onClick = { clearNotifications(); showNotifications = true }, modifier = Modifier.size(40.dp)) {
                        BadgedBox(badge = {
                            if (notificationCount > 0) {
                                Badge(containerColor = Color(0xFFA855F7), contentColor = Color.White) {
                                    Text(notificationCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }) {
                            Icon(Icons.Default.Notifications, "Notifications", tint = PurpleDeep, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // ✅ WELCOME + ILLUSTRATION - Perfectly contained Row layout
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left side: Welcome text (takes 60% of width)
                    Column(modifier = Modifier.weight(0.6f)) {
                        Text("Welcome back,", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PurpleDeep)
                        Text("$userName!", fontSize = 24.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                        Spacer(Modifier.height(6.dp))
                        Text("Let's get you ready for your exams.", fontSize = 13.sp, color = Color(0xFF514D83))
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.width(60.dp).height(3.dp).clip(CircleShape).background(PurplePrimary))
                    }
                    
                    // Right side: Illustration (takes 40% of width, perfectly contained)
                    StudyIllustration(
                        modifier = Modifier.weight(0.4f).height(110.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // COUNTDOWN CARD
                PurpleCountdownCard(modifier = Modifier.padding(horizontal = 20.dp))

                Spacer(Modifier.height(16.dp))

                // ✅ FEATURE CARDS (Daily 10 MCQ moved to the TOP!)
                ModernFeatureCard(
                    title = "Daily 10 MCQ",
                    subtitle = "Test your knowledge",
                    icon = Icons.Default.Star,
                    gradientColors = listOf(Color(0xFF19B68A), Color(0xFF159B9B)),
                    onClick = onOpenDailyQuiz,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                ModernFeatureCard(
                    title = "Personal Study Tracker",
                    subtitle = "You have studied for $totalStudyDays day${if (totalStudyDays != 1) "s" else ""}",
                    icon = Icons.Default.DateRange,
                    gradientColors = listOf(PurplePrimary, PurpleDark),
                    onClick = onOpenSuccessRoadmap,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                ModernFeatureCard(
                    title = "Current Streak",
                    subtitle = "$userStreak Days",
                    iconText = "🔥",
                    gradientColors = listOf(Color(0xFFFF6257), Color(0xFFFF8A3D)),
                    onClick = onOpenStreakCalendar,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                ModernFeatureCard(
                    title = "Question Bank",
                    subtitle = "Browse past questions by subject and topic.",
                    icon = Icons.AutoMirrored.Filled.List,
                    gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                    onClick = onOpenQBank,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                ModernFeatureCard(
                    title = "Progress & Analytics",
                    subtitle = "Track your scores, streaks, and weak areas.",
                    icon = Icons.Default.CheckCircle,
                    gradientColors = listOf(Color(0xFFA855F7), Color(0xFF9333EA)),
                    onClick = onOpenProgress,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(20.dp))
            }
        }

        if (showNotifications) { NotificationsScreen(onBack = { showNotifications = false }) }
    }

    if (showStudyPlan) { AlertDialog(onDismissRequest = { showStudyPlan = false }, title = { Text("📚 My Study Plan", fontWeight = FontWeight.Bold) }, text = { Column { Text("1. Complete the Daily 10 MCQ every day.\n2. Spend 30 mins reviewing incorrect answers.\n3. Focus on 1-2 weak subjects per week.\n4. Take a full Mock Exam every weekend.") } }, confirmButton = { TextButton(onClick = { showStudyPlan = false }) { Text("Got it", color = PurplePrimary, fontWeight = FontWeight.Bold) } }) }
    if (showFaq) { AlertDialog(onDismissRequest = { showFaq = false }, title = { Text(" FAQ & Help", fontWeight = FontWeight.Bold) }, text = { Column { Text("Q: How do I change my Level/Stream?\nA: Go to the Profile tab and update your settings.\n\nQ: Where do the questions come from?\nA: Real past GCE questions securely stored in our cloud.") } }, confirmButton = { TextButton(onClick = { showFaq = false }) { Text("Close", color = PurplePrimary, fontWeight = FontWeight.Bold) } }) }
    if (showReport) { AlertDialog(onDismissRequest = { showReport = false }, title = { Text("🐞 Report a Problem", fontWeight = FontWeight.Bold) }, text = { Text("Found a bug or have a suggestion? We'd love to hear from you!\n\nPlease email us at:\nsupport@gcevault.com") }, confirmButton = { TextButton(onClick = { showReport = false }) { Text("Close", color = PurplePrimary, fontWeight = FontWeight.Bold) } }) }
    if (showAbout) { AlertDialog(onDismissRequest = { showAbout = false }, title = { Text("ℹ️ About Us", fontWeight = FontWeight.Bold) }, text = { Text("GCE Vault v1.0.0\n\nPass GCE. Not Luck.\n\nBuilt for students, by students.\n\n© 2024 GCE Vault") }, confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Close", color = PurplePrimary, fontWeight = FontWeight.Bold) } }) }
}

// ✅ PERFECTLY CONTAINED ILLUSTRATION (No dangerous offsets)
@Composable
private fun StudyIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        // Soft decorative background
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterEnd)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Lavender, Color.Transparent)))
        )

        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.End
        ) {
            Icon(
                Icons.Default.School,
                "Graduation Cap",
                tint = PurpleDeep,
                modifier = Modifier.size(36.dp)
            )
            
            Spacer(Modifier.height(2.dp))
            
            IllustrationBook(label = "PAST QUESTIONS", width = 85.dp, color = Color(0xFF2459C7))
            IllustrationBook(label = "PRACTICE", width = 85.dp, color = Color(0xFF6B2ED8))
            IllustrationBook(label = "SUCCESS", width = 85.dp, color = Color(0xFF4A2BA5))
        }
    }
}

@Composable
private fun IllustrationBook(label: String, width: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        modifier = Modifier
            .width(width)
            .height(20.dp)
            .padding(bottom = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.85f)))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PurpleCountdownCard(modifier: Modifier = Modifier) {
    var timeLeft by remember { mutableStateOf(calculateTimeLeft()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            timeLeft = calculateTimeLeft()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF8A35EF), Color(0xFF5220C0), Color(0xFF341A91))))
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        // Subtle decorative blob (kept safely inside bounds)
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.045f))
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DateRange, "Calendar", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("GCE 2027", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                TimeUnitBox(timeLeft.days.toString(), "Days", Modifier.weight(1f))
                CountdownDivider()
                TimeUnitBox(timeLeft.hours.toString(), "Hrs", Modifier.weight(1f))
                CountdownDivider()
                TimeUnitBox(timeLeft.minutes.toString(), "Mins", Modifier.weight(1f))
                CountdownDivider()
                TimeUnitBox(timeLeft.seconds.toString(), "Secs", Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFF4C22A8).copy(alpha = 0.78f)).padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, "Countdown", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("left to GCE 2027", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 2.dp)) {
                    Text("Stay Focused", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontStyle = FontStyle.Italic)
                    Text("Keep Going!", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontStyle = FontStyle.Italic)
                }
            }
        }

        // Watermark icon (safe offset)
        Icon(
            Icons.Default.School,
            "Graduation",
            tint = Color.White.copy(alpha = 0.10f),
            modifier = Modifier.size(80.dp).align(Alignment.CenterEnd).offset(x = 20.dp)
        )
    }
}

@Composable
private fun CountdownDivider() {
    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.40f)))
}

@Composable
fun TimeUnitBox(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.86f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ModernFeatureCard(title: String, subtitle: String, icon: ImageVector? = null, iconText: String? = null, gradientColors: List<Color>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color.White, gradientColors.first().copy(alpha = 0.035f)))).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(colors = gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(icon, "Icon", tint = Color.White, modifier = Modifier.size(28.dp))
                } else if (iconText != null) {
                    Text(iconText, fontSize = 26.sp)
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17134B))
                Spacer(Modifier.height(3.dp))
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF514D83))
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(PurplePrimary).shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Go", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(onHome: () -> Unit, onQBank: () -> Unit, onAiTutor: () -> Unit, onScan: () -> Unit, onProfile: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .shadow(12.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF26145F), Color(0xFF43208F))))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(icon = Icons.Default.Home, label = "Home", selected = true, onClick = onHome, modifier = Modifier.weight(1f))
        BottomNavItem(icon = Icons.AutoMirrored.Filled.List, label = "Q-Bank", selected = false, onClick = onQBank, modifier = Modifier.weight(1f))
        BottomNavItem(icon = Icons.Default.AutoAwesome, label = "AI Tutor", selected = false, onClick = onAiTutor, modifier = Modifier.weight(1f))
        BottomNavItem(icon = Icons.Default.QrCodeScanner, label = "Scan", selected = false, onClick = onScan, modifier = Modifier.weight(1f))
        BottomNavItem(icon = Icons.Default.Person, label = "Profile", selected = false, onClick = onProfile, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) PurplePrimary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

data class TimeRemaining(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

fun calculateTimeLeft(): TimeRemaining {
    val target = LocalDateTime.of(2027, 5, 1, 0, 0, 0)
    val now = LocalDateTime.now()
    val duration = Duration.between(now, target)

    return if (duration.isNegative) {
        TimeRemaining(0, 0, 0, 0)
    } else {
        TimeRemaining(
            days = duration.toDays(),
            hours = duration.toHours() % 24,
            minutes = duration.toMinutes() % 60,
            seconds = duration.seconds % 60
        )
    }
}