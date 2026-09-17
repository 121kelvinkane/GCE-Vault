package com.gcevault.app.ui.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gcevault.app.viewmodel.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// ✅ ORIGINAL PURPLE THEME COLORS
val PurplePrimary = Color(0xFF7427E8)
val PurpleDark = Color(0xFF32136F)
val PurpleSoft = Color(0xFFF3F0FF)

val TagColors = mapOf(
    "O-Level Arts" to Color(0xFFEDE4FF),
    "O-Level Science" to Color(0xFFE8F5E9),
    "A-Level Arts" to Color(0xFFF3E5F5),
    "A-Level Science" to Color(0xFFFFF3E0),
    "General" to Color(0xFFF5F5F5)
)

data class CommunityPost(
    val id: String = "",
    val author: String = "",
    val subject: String = "",
    val question: String = "",
    val brain: String = "General",
    val timestamp: Long = System.currentTimeMillis(),
    val answers: List<CommunityAnswer> = emptyList()
)

data class CommunityAnswer(
    val author: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityChatScreen(
    onBack: () -> Unit = {},
    viewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val auth = Firebase.auth
    val snackbarHostState = remember { SnackbarHostState() }
    
    val currentUser by viewModel.currentUser.collectAsState()
    val userBrain = getUserBrain(currentUser?.level, currentUser?.stream)

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnFailureListener { 
                Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showNewPostDialog by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<CommunityPost?>(null) }
    var newAnswerText by remember { mutableStateOf("") }
    
    val filters = listOf("All", "O-Level Arts", "O-Level Science", "A-Level Arts", "A-Level Science")
    var selectedFilter by remember { mutableStateOf("All") }

    val db = Firebase.firestore
    val postsState = db.collection("community_posts")
        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .snapshots()
        .collectAsState(initial = null)

    val previousPostCount = remember { mutableStateOf(0) }
    LaunchedEffect(postsState.value) {
        val currentCount = postsState.value?.documents?.size ?: 0
        if (previousPostCount.value > 0 && currentCount > previousPostCount.value) {
            snackbarHostState.showSnackbar("🔔 A new question was just posted!")
        }
        previousPostCount.value = currentCount
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Community Q&A", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Ask & help students worldwide", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                actions = {
                    IconButton(onClick = { showNewPostDialog = true }) {
                        Icon(Icons.Default.Add, "New Post", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewPostDialog = true }, containerColor = PurplePrimary) {
                Icon(Icons.Default.Edit, "Ask Question", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 100.dp) 
                .background(Color(0xFFFBFAFF))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp, fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary, 
                            selectedLabelColor = Color.White,
                            containerColor = Color.White, // ✅ FIXED: Reverted to standard Color.White
                            labelColor = Color(0xFF514D83)
                        )
                    )
                }
            }

            val allPosts = postsState.value?.documents?.map { doc ->
                doc.toObject(CommunityPost::class.java)!!.copy(id = doc.id)
            } ?: emptyList()
            
            val filteredPosts = if (selectedFilter == "All") allPosts else allPosts.filter { it.brain == selectedFilter }

            when {
                postsState.value == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurplePrimary) }
                }
                filteredPosts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Forum, "No questions", tint = Color(0xFF514D83), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No questions in this brain yet.", color = Color(0xFF514D83))
                        }
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredPosts) { post -> PostCard(post = post, onClick = { selectedPost = post }) }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    if (showNewPostDialog) {
        var newSubject by remember { mutableStateOf("") }
        var newQuestion by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewPostDialog = false },
            title = { Text("Ask the Community", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Posting as: $userBrain", fontSize = 12.sp, color = PurplePrimary, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = newSubject, onValueChange = { newSubject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = newQuestion, onValueChange = { newQuestion = it }, label = { Text("Your question...") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newSubject.isNotBlank() && newQuestion.isNotBlank()) {
                        val newPost = CommunityPost(
                            author = "Student_" + (auth.currentUser?.uid?.takeLast(4) ?: "Anon"),
                            subject = newSubject, question = newQuestion, brain = userBrain
                        )
                        db.collection("community_posts").add(newPost)
                        showNewPostDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary), shape = RoundedCornerShape(12.dp)) { Text("Post Question") }
            },
            dismissButton = { TextButton(onClick = { showNewPostDialog = false }) { Text("Cancel") } }
        )
    }

    if (selectedPost != null) {
        val currentPost = selectedPost!!
        val previousAnswerCount = remember { mutableStateOf(currentPost.answers.size) }
        
        LaunchedEffect(currentPost.answers.size) {
            if (currentPost.answers.size > previousAnswerCount.value) {
                val newAnswer = currentPost.answers.last()
                Toast.makeText(context, "🔔 ${newAnswer.author} just answered!", Toast.LENGTH_LONG).show()
                previousAnswerCount.value = currentPost.answers.size
            }
        }

        AlertDialog(
            onDismissRequest = { selectedPost = null },
            title = { 
                Column {
                    Text(currentPost.subject, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Asked by ${currentPost.author} • ${currentPost.brain}", fontSize = 12.sp, color = Color(0xFF514D83))
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    Text(currentPost.question, fontSize = 16.sp, color = Color(0xFF17134B), modifier = Modifier.padding(bottom = 16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("Answers (${currentPost.answers.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF17134B))
                    Spacer(Modifier.height(8.dp))
                    if (currentPost.answers.isEmpty()) {
                        Text("No answers yet. Be the first to help!", color = Color(0xFF514D83), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    } else {
                        currentPost.answers.forEach { answer ->
                            Surface(color = PurpleSoft, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(answer.author, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PurplePrimary)
                                    Spacer(Modifier.height(4.dp))
                                    Text(answer.text, fontSize = 14.sp, color = Color(0xFF17134B))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newAnswerText, onValueChange = { newAnswerText = it },
                        placeholder = { Text("Write an answer...", color = Color(0xFF514D83)) }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = PurpleSoft, unfocusedContainerColor = PurpleSoft, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (newAnswerText.isNotBlank()) {
                                val newAnswer = CommunityAnswer(
                                    author = "Student_" + (auth.currentUser?.uid?.takeLast(4) ?: "Anon"), 
                                    text = newAnswerText,
                                    timestamp = System.currentTimeMillis()
                                )
                                db.collection("community_posts").document(currentPost.id)
                                    .update("answers", FieldValue.arrayUnion(newAnswer))
                                newAnswerText = ""
                            }
                        })
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (newAnswerText.isNotBlank()) {
                            val newAnswer = CommunityAnswer(
                                author = "Student_" + (auth.currentUser?.uid?.takeLast(4) ?: "Anon"), 
                                text = newAnswerText,
                                timestamp = System.currentTimeMillis()
                            )
                            db.collection("community_posts").document(currentPost.id)
                                .update("answers", FieldValue.arrayUnion(newAnswer))
                            newAnswerText = ""
                        }
                    }) { Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = PurplePrimary) }
                }
            }
        )
    }
}

fun getUserBrain(level: String?, stream: String?): String {
    val isO = level?.contains("O-Level", ignoreCase = true) == true || level?.contains("O Level", ignoreCase = true) == true
    val isA = level?.contains("A-Level", ignoreCase = true) == true || level?.contains("A Level", ignoreCase = true) == true
    val isArts = stream?.contains("Arts", ignoreCase = true) == true || stream?.contains("Art", ignoreCase = true) == true
    val isScience = stream?.contains("Science", ignoreCase = true) == true || stream?.contains("Sci", ignoreCase = true) == true
    return when {
        (isO && isArts) -> "O-Level Arts"
        (isO && isScience) -> "O-Level Science"
        (isA && isArts) -> "A-Level Arts"
        (isA && isScience) -> "A-Level Science"
        else -> "General"
    }
}

@Composable
fun PostCard(post: CommunityPost, onClick: () -> Unit) {
    Surface(
        color = Color.White, // ✅ FIXED: Reverted to standard Color.White
        shape = RoundedCornerShape(20.dp), 
        shadowElevation = 2.dp, 
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, "User", tint = PurplePrimary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(post.author, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF17134B))
                Spacer(Modifier.weight(1f))
                val tagColor = TagColors[post.brain] ?: Color(0xFFF5F5F5)
                Surface(color = tagColor, shape = RoundedCornerShape(8.dp)) {
                    Text(post.brain, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(post.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF17134B))
            Spacer(Modifier.height(4.dp))
            Text(post.question, fontSize = 14.sp, color = Color(0xFF514D83), maxLines = 3)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Forum, "Answers", tint = Color(0xFF514D83), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("${post.answers.size} Answers", fontSize = 12.sp, color = Color(0xFF514D83))
            }
        }
    }
}