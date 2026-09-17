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

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    // Local variable to prevent ANY "Conflicting declarations" errors
    val primaryGreen = Color(0xFF1B5E20) 

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(100.dp).background(primaryGreen.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("📚", fontSize = 48.sp)
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = "Welcome to GCE Vault",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = primaryGreen,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "Pass GCE today, not luck.\n100% hard work.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = {
                prefs.edit().putBoolean("has_seen_welcome", true).apply()
                onGetStarted()
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Get Started Today", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}