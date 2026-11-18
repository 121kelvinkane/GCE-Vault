package com.gcevault.app.ui.onboarding

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ✅ PURPLE THEME COLORS (Matching the rest of your app)
private val PurplePrimary = Color(0xFF7427E8)
private val PurpleSoft = Color(0xFFF3F0FF)
private val AppBackground = Color(0xFFFBFAFF)
private val TextDark = Color(0xFF17134B)
private val TextGray = Color(0xFF514D83)

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Purple Soft Icon Background
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(PurpleSoft, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("📚", fontSize = 48.sp)
        }
        
        Spacer(Modifier.height(32.dp))
        
        // ✅ Purple Primary Title
        Text(
            text = "Welcome to GCE Vault",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PurplePrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))
        
        // ✅ Dark Gray/Purple Subtitle
        Text(
            text = "Pass GCE today, not luck.\n100% hard work.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
        
        Spacer(Modifier.height(48.dp))
        
        // ✅ Purple Primary Button
        Button(
            onClick = {
                prefs.edit().putBoolean("has_seen_welcome", true).apply()
                onGetStarted()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Get Started Today", 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.White
            )
        }
    }
}