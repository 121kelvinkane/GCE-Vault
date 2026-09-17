package com.gcevault.app.ui.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val PurplePrimary = Color(0xFF7427E8)
private val PurpleDark = Color(0xFF32136F)
private val PurpleSoft = Color(0xFFF3F0FF)
private val BackgroundColor = Color(0xFFFBFAFF)
private val TextDark = Color(0xFF17134B)
private val TextGray = Color(0xFF514D83)

@Composable
fun ProfileScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
    val uid = auth.currentUser?.uid
    
    var name by remember { mutableStateOf("Student") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(prefs.getString("user_level", "O-Level") ?: "O-Level") }
    var selectedStream by remember { mutableStateOf(prefs.getString("user_stream", "Arts") ?: "Arts") }
    
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        name = document.getString("name") ?: "Student"
                        email = document.getString("email") ?: ""
                        phone = document.getString("phone") ?: ""
                        selectedLevel = document.getString("level") ?: prefs.getString("user_level", "O-Level") ?: "O-Level"
                        selectedStream = document.getString("stream") ?: prefs.getString("user_stream", "Arts") ?: "Arts"
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) { 
        // Premium Purple Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(PurplePrimary, PurpleDark)))
                .padding(top = 32.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), 
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    if (!isEditing) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary) 
                                .clickable { isEditing = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White) 
                    Text(text = "$selectedLevel • $selectedStream", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f)) 
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurplePrimary)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Personal Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        if (isEditing) {
                            Text("Cancel", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { 
                                isEditing = false 
                            })
                        }
                    }
                    
                    OutlinedTextField(
                        value = name, onValueChange = { name = it }, 
                        label = { Text("Full Name") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        singleLine = true, 
                        shape = RoundedCornerShape(12.dp),
                        enabled = isEditing,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = if (isEditing) TextGray.copy(alpha = 0.5f) else Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            disabledContainerColor = PurpleSoft
                        )
                    )
                    OutlinedTextField(
                        value = email, onValueChange = { email = it }, 
                        label = { Text("Email Address") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        singleLine = true, 
                        shape = RoundedCornerShape(12.dp),
                        enabled = isEditing,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = if (isEditing) TextGray.copy(alpha = 0.5f) else Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            disabledContainerColor = PurpleSoft
                        )
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it }, 
                        label = { Text("Phone Number") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        singleLine = true, 
                        shape = RoundedCornerShape(12.dp),
                        enabled = isEditing,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = if (isEditing) TextGray.copy(alpha = 0.5f) else Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            disabledContainerColor = PurpleSoft
                        )
                    )

                    if (isEditing) {
                        Text("Academic Level", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.padding(top = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            listOf("O-Level", "A-Level").forEach { lvl ->
                                FilterChip(
                                    selected = selectedLevel == lvl,
                                    onClick = { selectedLevel = lvl },
                                    label = { Text(lvl) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = PurpleSoft,
                                        labelColor = TextDark
                                    )
                                )
                            }
                        }
                        
                        Text("Stream", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.padding(top = 16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            listOf("Arts", "Science").forEach { strm ->
                                FilterChip(
                                    selected = selectedStream == strm,
                                    onClick = { selectedStream = strm },
                                    label = { Text(strm) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = PurpleSoft,
                                        labelColor = TextDark
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()) {
                                    isLoading = true
                                    // ✅ Save level/stream locally immediately
                                    prefs.edit().putString("user_level", selectedLevel).putString("user_stream", selectedStream).apply()
                                    
                                    uid?.let { currentUid ->
                                        val updates = hashMapOf<String, Any>(
                                            "name" to name,
                                            "email" to email,
                                            "phone" to phone,
                                            "level" to selectedLevel,
                                            "stream" to selectedStream
                                        )
                                        db.collection("users").document(currentUid).update(updates)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                isEditing = false
                                                Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ✅ CLEAN MENU: No manual reminder settings here anymore!
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {  
            SettingsItem(icon = Icons.Default.Star, title = "Mock Exams", subtitle = "Simulate real exam conditions", onClick = { onNavigate("Mock") })
            SettingsItem(icon = Icons.Default.CheckCircle, title = "Progress & Analytics", subtitle = "Track your scores and streaks", onClick = { onNavigate("Progress") })
            SettingsItem(icon = Icons.AutoMirrored.Filled.Help, title = "Help & Support", subtitle = "How to use the app", onClick = { showHelpDialog = true })
            SettingsItem(icon = Icons.Default.Language, title = "Language", subtitle = "English", onClick = { showLanguageDialog = true })
            SettingsItem(icon = Icons.Default.Info, title = "About", subtitle = "GCE VAULT v1.0", onClick = { showAboutDialog = true })
            Spacer(Modifier.height(40.dp)) 
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false }, 
            title = { Text("About GCE Vault", fontWeight = FontWeight.Bold) }, 
            text = { Text("Version 1.0.0\n\nPass GCE. Not Luck.\nBuilt for students, by students.") }, 
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("Close", color = PurplePrimary, fontWeight = FontWeight.Bold) } }
        )
    }
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false }, 
            title = { Text("Help & Support", fontWeight = FontWeight.Bold) }, 
            text = { Text("Need help? Email us at support@gcevault.com or check our website for video tutorials.") }, 
            confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("Got it", color = PurplePrimary, fontWeight = FontWeight.Bold) } }
        )
    }
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false }, 
            title = { Text("Select Language", fontWeight = FontWeight.Bold) }, 
            text = { 
                Column { 
                    Text("English", modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().clickable { showLanguageDialog = false }, color = PurplePrimary, fontWeight = FontWeight.Bold)
                    Text("French", modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().clickable { showLanguageDialog = false }, color = TextGray) 
                } 
            }, 
            confirmButton = { TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), 
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp), 
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(PurplePrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark) 
                Spacer(Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 14.sp, color = TextGray) 
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go", tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}