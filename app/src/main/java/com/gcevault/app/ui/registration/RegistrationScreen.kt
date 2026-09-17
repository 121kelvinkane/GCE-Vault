package com.gcevault.app.ui.registration

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val PurplePrimary = Color(0xFF7427E8)
val BackgroundColor = Color(0xFFFBFAFF)
val TextDark = Color(0xFF17134B)
val TextGray = Color(0xFF514D83)
val CardBackground = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var isLoginMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // ✅ ADDED: State for Level and Stream selection
    var selectedLevel by remember { mutableStateOf("O-Level") }
    var selectedStream by remember { mutableStateOf("Arts") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        
        Text(if (isLoginMode) "Welcome Back!" else "Create Account", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextDark)
        Text(if (isLoginMode) "Log in to continue your learning journey" else "Join GCE Vault and start learning", fontSize = 14.sp, color = TextGray, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                
                if (!isLoginMode) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PurplePrimary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary, unfocusedBorderColor = TextGray.copy(alpha = 0.5f))
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = PurplePrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary, unfocusedBorderColor = TextGray.copy(alpha = 0.5f))
                    )
                    
                    // ✅ ADDED: Level Selection
                    Text("Select Level", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        LevelButton("O-Level", selectedLevel == "O-Level") { selectedLevel = "O-Level" }
                        LevelButton("A-Level", selectedLevel == "A-Level") { selectedLevel = "A-Level" }
                    }
                    
                    // ✅ ADDED: Stream Selection
                    Text("Select Stream", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        LevelButton("Arts", selectedStream == "Arts") { selectedStream = "Arts" }
                        LevelButton("Science", selectedStream == "Science") { selectedStream = "Science" }
                    }
                }

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = PurplePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary, unfocusedBorderColor = TextGray.copy(alpha = 0.5f))
                )

                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = PurplePrimary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary, unfocusedBorderColor = TextGray.copy(alpha = 0.5f))
                )

                if (errorMessage.isNotBlank()) {
                    Text(errorMessage, color = Color(0xFFD32F2F), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank() || (!isLoginMode && (name.isBlank() || phone.isBlank()))) {
                            errorMessage = "Please fill in all fields."
                            return@Button
                        }
                        if (!isLoginMode && password.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = ""

                        if (isLoginMode) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = "Login failed: ${task.exception?.message}"
                                    }
                                }
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        // ✅ FIXED: Now saves the ACTUAL selectedLevel and selectedStream
                                        val userData = hashMapOf(
                                            "name" to name, 
                                            "email" to email, 
                                            "phone" to phone,
                                            "level" to selectedLevel, 
                                            "stream" to selectedStream, 
                                            "streak" to 0, 
                                            "xp" to 0,
                                            "createdAt" to System.currentTimeMillis()
                                        )
                                        user?.uid?.let { uid ->
                                            db.collection("users").document(uid).set(userData)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                                    onAuthSuccess()
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    errorMessage = "Failed to save user data: ${e.message}"
                                                }
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = "Registration failed: ${task.exception?.message}"
                                    }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isLoginMode) "Log In" else "Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (isLoginMode) "Don't have an account? " else "Already have an account? ", color = TextGray, fontSize = 14.sp)
            Text(
                text = if (isLoginMode) "Sign Up" else "Log In",
                color = PurplePrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { 
                    isLoginMode = !isLoginMode
                    errorMessage = "" 
                }
            )
        }
        Spacer(Modifier.height(40.dp))
    }
}

// ✅ ADDED: Level Button Composable (using fillMaxWidth(0.5f) to avoid Compose weight bugs)
@Composable
fun LevelButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.5f).height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PurplePrimary else CardBackground, 
            contentColor = if (isSelected) Color.White else TextDark
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}