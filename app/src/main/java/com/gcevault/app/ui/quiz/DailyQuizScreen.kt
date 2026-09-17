package com.gcevault.app.ui.quiz

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gcevault.app.ads.RewardedAdHelper
import com.gcevault.app.viewmodel.ProgressViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class FirebaseQuestion(
    val id: String = "",
    val level: String = "",
    val stream: String = "",
    val subject: String = "",
    val topic: String = "",
    val year: Int = 0,
    val paper: String = "",
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctOption: String = "",
    val explanation: String = ""
)

private val PurplePrimary = Color(0xFF7427E8)
private val PurpleSoft = Color(0xFFF3F0FF)
private val BackgroundColor = Color(0xFFFBFAFF)
private val TextDark = Color(0xFF17134B)
private val TextGray = Color(0xFF514D83)

private val CorrectGreen = Color(0xFF2E7D32)
private val WrongRed = Color(0xFFC62828)

const val BANNER_AD_UNIT_ID = "ca-app-pub-6772916095273626/1824270409"
const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6772916095273626/2945780384"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuizScreen(
    onBack: () -> Unit,
    progressViewModel: ProgressViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    val userLevel = prefs.getString("user_level", "O-Level") ?: "O-Level"
    val userStream = prefs.getString("user_stream", "Science") ?: "Science"

    var questions by remember { mutableStateOf<List<FirebaseQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    var isQuizUnlocked by remember { mutableStateOf(false) }
    var isResultsUnlocked by remember { mutableStateOf(false) }
    var showStartAdDialog by remember { mutableStateOf(true) }
    var showEndAdDialog by remember { mutableStateOf(false) }

    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }

    LaunchedEffect(Unit) {
        RewardedAdHelper.loadQuizAd(context)
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) { interstitialAd = null }
        })
    }

    LaunchedEffect(Unit) {
        isLoading = true
        val db = FirebaseFirestore.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val seed = today.hashCode().toLong()
        val random = java.util.Random(seed)

        db.collection("questions")
            .whereEqualTo("level", userLevel)
            .whereEqualTo("stream", userStream)
            .get()
            .addOnSuccessListener { result ->
                val fetchedQuestions = result.mapNotNull { doc ->
                    FirebaseQuestion(
                        id = doc.id,
                        level = doc.getString("level") ?: "",
                        stream = doc.getString("stream") ?: "",
                        // ✅ Added fallback for 'Subject' just in case your database uses a capital S
                        subject = doc.getString("subject") ?: doc.getString("Subject") ?: "Science",
                        topic = doc.getString("topic") ?: "",
                        year = doc.getLong("year")?.toInt() ?: doc.getLong("Year")?.toInt() ?: 0,
                        paper = doc.getString("paper") ?: "",
                        questionText = doc.getString("questionText") ?: doc.getString("questiontext") ?: "",
                        optionA = doc.getString("optionA") ?: doc.getString("optiona") ?: "",
                        optionB = doc.getString("optionB") ?: doc.getString("optionb") ?: "",
                        optionC = doc.getString("optionC") ?: doc.getString("optionc") ?: "",
                        optionD = doc.getString("optionD") ?: doc.getString("optiond") ?: "",
                        correctOption = doc.getString("correctOption") ?: doc.getString("correctoption") ?: "",
                        explanation = doc.getString("explanation") ?: ""
                    )
                }
                questions = fetchedQuestions.shuffled(random).take(10)
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    val checkAnswer: (String, String) -> Unit = { selected, correct ->
        selectedOption = selected
        isAnswered = true
        if (selected == correct) score++
    }

    if (showStartAdDialog && !isQuizUnlocked) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(48.dp)) },
            title = { Text("Unlock Daily Quiz", fontWeight = FontWeight.Bold) },
            text = { Text("Watch 1 short video to unlock today's 10 questions.") },
            confirmButton = {
                Button(onClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        RewardedAdHelper.showQuizAd(activity,
                            onReward = {
                                isQuizUnlocked = true
                                showStartAdDialog = false
                                Toast.makeText(context, "Quiz Unlocked! Good luck.", Toast.LENGTH_SHORT).show()
                            },
                            onFailed = {
                                isQuizUnlocked = true
                                showStartAdDialog = false
                                Toast.makeText(context, "Ad unavailable, but you can still take the quiz! Keep learning.", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)) {
                    Text("Watch Video")
                }
            },
            dismissButton = {
                TextButton(onClick = onBack) { Text("Go Back", color = TextGray) }
            }
        )
    }

    if (showEndAdDialog && !isResultsUnlocked) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Default.Star, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(48.dp)) },
            title = { Text("Unlock Results", fontWeight = FontWeight.Bold) },
            text = { Text("Watch 1 short video to see your final score and earn XP.") },
            confirmButton = {
                Button(onClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        RewardedAdHelper.showQuizAd(activity,
                            onReward = {
                                isResultsUnlocked = true
                                showEndAdDialog = false
                                progressViewModel.saveQuizResult(subject = "Daily Mixed Quiz", score = score, totalQuestions = questions.size)
                                Toast.makeText(context, "Results Unlocked! +$score XP", Toast.LENGTH_LONG).show()
                            },
                            onFailed = {
                                isResultsUnlocked = true
                                showEndAdDialog = false
                                progressViewModel.saveQuizResult(subject = "Daily Mixed Quiz", score = score, totalQuestions = questions.size)
                                Toast.makeText(context, "Ad unavailable, but your results are unlocked! Keep learning.", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)) {
                    Text("Watch Video")
                }
            },
            dismissButton = {
                TextButton(onClick = onBack) { Text("Go Back", color = TextGray) }
            }
        )
    }

    if (!isQuizUnlocked) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PurplePrimary)
                Spacer(Modifier.height(16.dp))
                Text("Loading quiz...", color = TextGray)
            }
        }
    } else if (isResultsUnlocked) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Star, "Achievement", tint = PurplePrimary, modifier = Modifier.size(100.dp))
            Spacer(Modifier.height(24.dp))
            Text("Quiz Completed!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextDark, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your Score", fontSize = 16.sp, color = TextGray)
                    Spacer(Modifier.height(8.dp))
                    Text("$score / ${questions.size}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("+$score XP Earned", fontSize = 16.sp, color = TextGray)
                }
            }
            Spacer(Modifier.height(40.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary), shape = RoundedCornerShape(12.dp)) {
                Text("Back to Home", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Daily 10: $userLevel $userStream", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary)
                )
            },
            bottomBar = {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = BANNER_AD_UNIT_ID
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().background(BackgroundColor).padding(padding)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PurplePrimary) }
                } else if (questions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No questions found.", color = TextGray) }
                } else {
                    val currentQ = questions[currentIndex]
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)
                    ) {
                        Text("Question ${currentIndex + 1} of ${questions.size}", fontSize = 14.sp, color = TextGray)
                        Spacer(Modifier.height(16.dp))
                        
                        // ✅ UPDATED CARD WITH PROMINENT SUBJECT & YEAR BADGES
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                // Badges Row
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(PurpleSoft, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = currentQ.subject.ifEmpty { "Science" }, // Fallback if subject is missing
                                            fontSize = 12.sp,
                                            color = PurplePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Year ${currentQ.year}",
                                            fontSize = 12.sp,
                                            color = TextDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.height(16.dp))

                                // Question Text
                                Text(
                                    text = currentQ.questionText,
                                    fontSize = 18.sp,
                                    color = TextDark,
                                    lineHeight = 26.sp
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OptionButton("A", currentQ.optionA, selectedOption == "A", isAnswered, currentQ.correctOption == "A") { if (!isAnswered) checkAnswer("A", currentQ.correctOption) }
                            OptionButton("B", currentQ.optionB, selectedOption == "B", isAnswered, currentQ.correctOption == "B") { if (!isAnswered) checkAnswer("B", currentQ.correctOption) }
                            OptionButton("C", currentQ.optionC, selectedOption == "C", isAnswered, currentQ.correctOption == "C") { if (!isAnswered) checkAnswer("C", currentQ.correctOption) }
                            OptionButton("D", currentQ.optionD, selectedOption == "D", isAnswered, currentQ.correctOption == "D") { if (!isAnswered) checkAnswer("D", currentQ.correctOption) }
                        }
                        if (isAnswered) {
                            Spacer(Modifier.height(24.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = if (selectedOption == currentQ.correctOption) CorrectGreen.copy(0.1f) else WrongRed.copy(0.1f)), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(if (selectedOption == currentQ.correctOption) "✅ Correct!" else "❌ Incorrect", fontWeight = FontWeight.Bold, color = if (selectedOption == currentQ.correctOption) CorrectGreen else WrongRed)
                                    Spacer(Modifier.height(8.dp))
                                    Text(currentQ.explanation, fontSize = 15.sp, color = TextDark)
                                }
                            }
                        }
                        Spacer(Modifier.height(100.dp))
                    }
                    Box(modifier = Modifier.fillMaxWidth().background(BackgroundColor).padding(16.dp)) {
                        Button(
                            onClick = {
                                if (currentIndex == 4) {
                                    val activity = context as? Activity
                                    if (activity != null && interstitialAd != null) {
                                        interstitialAd?.show(activity)
                                        interstitialAd = null
                                    }
                                }
                                if (currentIndex < questions.size - 1) {
                                    currentIndex++
                                    selectedOption = null
                                    isAnswered = false
                                } else {
                                    showEndAdDialog = true
                                }
                            },
                            enabled = isAnswered,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (currentIndex < questions.size - 1) "Next Question" else "Submit & See Results", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionButton(letter: String, text: String, isSelected: Boolean, isAnswered: Boolean, isCorrect: Boolean, onClick: () -> Unit) {
    val borderColor = when {
        isAnswered && isCorrect -> CorrectGreen
        isAnswered && isSelected && !isCorrect -> WrongRed
        isSelected -> PurplePrimary
        else -> TextGray.copy(alpha = 0.3f)
    }
    val bgColor = when {
        isAnswered && isCorrect -> CorrectGreen.copy(0.1f)
        isAnswered && isSelected && !isCorrect -> WrongRed.copy(0.1f)
        else -> Color.White
    }
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).background(bgColor, RoundedCornerShape(12.dp)).border(2.dp, borderColor, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$letter.", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = borderColor, modifier = Modifier.width(32.dp))
        Text(text, fontSize = 16.sp, color = TextDark)
    }
}