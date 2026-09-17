package com.gcevault.app.ui.auth

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gcevault.app.viewmodel.UserViewModel // ✅ Import the ViewModel

// Brand Colors
private val GceGreen = Color(0xFF1B5E20)
private val BackgroundColor = Color(0xFFF8F9FA)
private val TextDark = Color(0xFF1E293B)
private val TextGray = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpComplete: () -> Unit,
    viewModel: UserViewModel = viewModel() // ✅ Inject the ViewModel here
) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var selectedLevel by remember { mutableStateOf("O-Level") }
    var selectedStream by remember { mutableStateOf("Arts") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        // 1. Header Section
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = "GCE Vault Logo",
            tint = GceGreen,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = "Create your account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Save your progress, earn XP, and join other students across Africa with GCE Vault.",
            fontSize = 16.sp,
            color = TextGray,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(32.dp))

        // 2. Input Fields
        StyledTextField(
            value = username,
            onValueChange = { username = it },
            label = "USERNAME",
            placeholder = "e.g. ngwa_junior",
            icon = Icons.Default.Person
        )
        Spacer(Modifier.height(16.dp))

        StyledTextField(
            value = email,
            onValueChange = { email = it },
            label = "EMAIL",
            placeholder = "you@example.com",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(16.dp))

        // Phone Number with Country Code
        Text("PHONE NUMBER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("670 12 34 56", color = TextGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GceGreen,
                unfocusedBorderColor = BorderColor,
                focusedLabelColor = GceGreen
            ),
            leadingIcon = { 
                Text("+237", fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(start = 8.dp)) 
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(Modifier.height(16.dp))

        // Password Field
        Text("PASSWORD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Choose a password", color = TextGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GceGreen,
                unfocusedBorderColor = BorderColor
            ),
            leadingIcon = { Icon(Icons.Default.Lock, "Lock", tint = TextGray) },
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, "Toggle Password", tint = TextGray)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
        )
        
        Spacer(Modifier.height(24.dp))

        // 3. Academic Settings (Kept for app logic, styled cleanly)
        Text("ACADEMIC LEVEL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            LevelButton("O-Level", selectedLevel == "O-Level") { selectedLevel = "O-Level" }
            LevelButton("A-Level", selectedLevel == "A-Level") { selectedLevel = "A-Level" }
        }
        Spacer(Modifier.height(12.dp))
        
        Text("STREAM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            LevelButton("Arts", selectedStream == "Arts") { selectedStream = "Arts" }
            LevelButton("Science", selectedStream == "Science") { selectedStream = "Science" }
        }

        Spacer(Modifier.height(32.dp))

        // 4. Create Account Button
        Button(
            onClick = {
                if (username.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                // ✅ SAVE TO ROOM DATABASE USING VIEWMODEL (Replaces SharedPreferences)
                viewModel.saveUser(username, email, phone, selectedLevel, selectedStream)
                
                Toast.makeText(context, "Welcome to GCE Vault!", Toast.LENGTH_SHORT).show()
                onSignUpComplete()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp), // Fully rounded like the image
            colors = ButtonDefaults.buttonColors(containerColor = GceGreen)
        ) {
            Text("Create account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        // 5. Footer Text
        Text(
            text = "By creating an account you agree to our Terms of Service and Privacy Policy.",
            fontSize = 12.sp,
            color = TextGray,
            modifier = Modifier.fillMaxWidth(),
            lineHeight = 16.sp
        )

        Spacer(Modifier.weight(1f)) // Pushes the sign-in link to the bottom

        // 6. Sign In Link
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", color = TextGray, fontSize = 14.sp)
            Text(
                "Sign in", 
                color = GceGreen, 
                fontWeight = FontWeight.Bold, 
                fontSize = 14.sp,
                modifier = Modifier.clickable { 
                    // For now, just log them in directly since we don't have a backend
                    onSignUpComplete()
                }
            )
        }
    }
}

// Reusable Styled Input Field
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextGray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GceGreen,
            unfocusedBorderColor = BorderColor
        ),
        leadingIcon = { Icon(icon, label, tint = TextGray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun LevelButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.5f).height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) GceGreen else Color.White,
            contentColor = if (isSelected) Color.White else TextGray
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}