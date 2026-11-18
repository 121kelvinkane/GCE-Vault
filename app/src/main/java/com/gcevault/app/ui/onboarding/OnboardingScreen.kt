package com.gcevault.app.ui.onboarding

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var currentStep by remember { mutableIntStateOf(1) }
    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var selectedStream by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("GCE VAULT", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
        Spacer(Modifier.height(8.dp))
        Text("Pass GCE. Not Luck.", fontSize = 16.sp, color = TextGray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))

        when (currentStep) {
            1 -> {
                Text("Step 1 of 2", fontSize = 14.sp, color = TextGray)
                Spacer(Modifier.height(8.dp))
                Text("Choose Your Level", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(32.dp))

                BrainCard(title = "O-Level", subtitle = "Ordinary Level", isSelected = selectedLevel == "O-Level", onClick = { selectedLevel = "O-Level" })
                Spacer(Modifier.height(16.dp))
                BrainCard(title = "A-Level", subtitle = "Advanced Level", isSelected = selectedLevel == "A-Level", onClick = { selectedLevel = "A-Level" })

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { currentStep = 2 },
                    enabled = selectedLevel != null,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            2 -> {
                Text("Step 2 of 2", fontSize = 14.sp, color = TextGray)
                Spacer(Modifier.height(8.dp))
                Text("Choose Your Stream", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(8.dp))
                Text("($selectedLevel)", fontSize = 16.sp, color = PurplePrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(32.dp))

                BrainCard(title = "Arts", subtitle = "Languages, Humanities, Business", isSelected = selectedStream == "Arts", onClick = { selectedStream = "Arts" })
                Spacer(Modifier.height(16.dp))
                BrainCard(title = "Science", subtitle = "Math, Biology, Chemistry, Physics", isSelected = selectedStream == "Science", onClick = { selectedStream = "Science" })

                Spacer(Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { currentStep = 1 },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PurplePrimary)
                    ) {
                        Text("Back", fontSize = 16.sp, color = PurplePrimary)
                    }
                    Button(
                        onClick = {
                            prefs.edit()
                                .putString("user_level", selectedLevel)
                                .putString("user_stream", selectedStream)
                                .putBoolean("onboarding_complete", true)
                                .apply()
                            onComplete()
                        },
                        enabled = selectedStream != null,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BrainCard(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PurpleSoft else Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (isSelected) PurplePrimary else TextDark)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 14.sp, color = TextGray)
            }
            if (isSelected) {
                Text("✓", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
            }
        }
    }
}