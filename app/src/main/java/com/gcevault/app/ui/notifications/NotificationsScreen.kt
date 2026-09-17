package com.gcevault.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ✅ NEW PURPLE THEME COLORS
private val PurplePrimary = Color(0xFF7427E8)
private val PurpleSoft = Color(0xFFF3F0FF)
private val BackgroundColor = Color(0xFFFBFAFF)
private val TextDark = Color(0xFF17134B)
private val TextGray = Color(0xFF514D83)

data class NotificationItem(
    val title: String,
    val message: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val time: String,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    // Mock notifications (Later you can load these from a database or SharedPreferences)
    val notifications = listOf(
        NotificationItem(
            title = "Daily Quiz Ready! 📚",
            message = "Your 10 random GCE questions are waiting. Tap to start and keep your streak alive!",
            icon = Icons.Default.Star,
            time = "Just now",
            isRead = false
        ),
        NotificationItem(
            title = "Streak Milestone! 🔥",
            message = "Awesome job! You've maintained a 3-day study streak. Keep it up!",
            icon = Icons.Default.LocalFireDepartment,
            time = "Yesterday",
            isRead = true
        ),
        NotificationItem(
            title = "New Subject Added",
            message = "Geology has been added to the A-Level Science Question Bank.",
            icon = Icons.Default.Landscape,
            time = "2 days ago",
            isRead = true
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(notification = notification)
            }
            
            if (notifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsOff, "No notifications", tint = TextGray, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No new notifications", fontSize = 16.sp, color = TextGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* TODO: Navigate to specific feature */ },
        colors = CardDefaults.cardColors(
            // ✅ PurpleSoft for unread, White for read
            containerColor = if (notification.isRead) Color.White else PurpleSoft 
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PurplePrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(notification.icon, "Icon", tint = PurplePrimary, modifier = Modifier.size(24.dp))
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark, // ✅ Updated to TextDark
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = notification.time, fontSize = 12.sp, color = TextGray) // ✅ Updated to TextGray
                }
                Spacer(Modifier.height(4.dp))
                Text(text = notification.message, fontSize = 14.sp, color = TextGray, lineHeight = 20.sp) // ✅ Updated to TextGray
            }
        }
    }
}