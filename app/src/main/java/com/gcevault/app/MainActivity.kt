package com.gcevault.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.gcevault.app.ads.RewardedAdHelper
import com.gcevault.app.ui.aitutor.AITutorScreen
import com.gcevault.app.ui.chat.CommunityChatScreen
import com.gcevault.app.ui.home.HomeScreen
import com.gcevault.app.ui.onboarding.OnboardingScreen
import com.gcevault.app.ui.onboarding.WelcomeScreen
import com.gcevault.app.ui.profile.ProfileScreen
import com.gcevault.app.ui.progress.ProgressScreen
import com.gcevault.app.ui.questionbank.QuestionBankScreen
import com.gcevault.app.ui.quiz.DailyQuizScreen
import com.gcevault.app.ui.registration.RegistrationScreen
import com.gcevault.app.ui.roadmap.SuccessRoadmapScreen
import com.gcevault.app.ui.streak.StreakCalendarScreen
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object QBank : BottomNavItem("qbank", Icons.AutoMirrored.Filled.List, "Q-Bank")
    object AITutor : BottomNavItem("ai", Icons.Default.Star, "AI Tutor")      
    object Chat : BottomNavItem("chat", Icons.AutoMirrored.Filled.Chat, "Community")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}

class MainActivity : ComponentActivity() {
    private var appOpenAdManager: AppOpenAdManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        RewardedAdHelper.loadAd(this)
        appOpenAdManager = AppOpenAdManager(application)
        
        setContent {
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            var showWelcome by remember { mutableStateOf(!prefs.getBoolean("has_seen_welcome", false)) }
            var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarding_complete", false)) }
            
            // ✅ 1. CREATE A NAVIGATION STACK (Remembers your history)
            val navStack = remember { mutableStateListOf("home") }
            val currentRoute = navStack.last()
            
            var showDailyQuiz by remember { mutableStateOf(false) }
            var showStreakCalendar by remember { mutableStateOf(false) }
            var showSuccessRoadmap by remember { mutableStateOf(false) }
            var isLoggedIn by remember { mutableStateOf(true) } 

            // ✅ 2. HELPER FUNCTIONS FOR NAVIGATION
            val navigateTo = { route: String -> 
                if (navStack.lastOrNull() != route) {
                    navStack.add(route) 
                }
            }
            
            val goBack = { 
                if (navStack.size > 1) {
                    navStack.removeLast() // Goes to the actual previous page
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    
                    // ✅ 3. GLOBAL BACK HANDLER FOR OVERLAYS
                    if (showDailyQuiz) BackHandler { showDailyQuiz = false }
                    if (showStreakCalendar) BackHandler { showStreakCalendar = false }
                    if (showSuccessRoadmap) BackHandler { showSuccessRoadmap = false }

                    when {
                        !isLoggedIn -> {
                            RegistrationScreen(onAuthSuccess = { isLoggedIn = true })
                        }
                        showWelcome -> WelcomeScreen(onGetStarted = { prefs.edit().putBoolean("has_seen_welcome", true).apply(); showWelcome = false })
                        showOnboarding -> OnboardingScreen(onComplete = { prefs.edit().putBoolean("onboarding_complete", true).apply(); showOnboarding = false })
                        showDailyQuiz -> DailyQuizScreen(onBack = { showDailyQuiz = false })
                        showStreakCalendar -> StreakCalendarScreen(onBack = { showStreakCalendar = false })
                        showSuccessRoadmap -> SuccessRoadmapScreen(onBack = { showSuccessRoadmap = false })
                        else -> {
                            MainAppShell(
                                currentRoute = currentRoute,
                                navStack = navStack,
                                navigateTo = navigateTo,
                                goBack = goBack,
                                onOpenDailyQuiz = { showDailyQuiz = true },
                                onOpenStreakCalendar = { showStreakCalendar = true },
                                onOpenSuccessRoadmap = { showSuccessRoadmap = true },
                                onOpenProgress = { navigateTo("Progress") } 
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppShell(
    currentRoute: String,
    navStack: MutableList<String>, // ✅ FIXED: Changed to MutableList<String>
    navigateTo: (String) -> Unit,
    goBack: () -> Unit,
    onOpenDailyQuiz: () -> Unit,
    onOpenStreakCalendar: () -> Unit, 
    onOpenSuccessRoadmap: () -> Unit, 
    onOpenProgress: () -> Unit,
) {
    val AppBackgroundColor = Color(0xFFFBFAFF) 
    
    // ✅ 4. SYSTEM BACK BUTTON NOW USES goBack()
    BackHandler(enabled = navStack.size > 1) {
        goBack()
    }
    
    val showBottomNav = currentRoute != "Progress"
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = AppBackgroundColor, 
            contentColor = MaterialTheme.colorScheme.onBackground
        ) { padding ->
            when (currentRoute) {
                "home" -> HomeScreen(
                    modifier = Modifier.padding(padding),
                    onOpenDailyQuiz = onOpenDailyQuiz,
                    onOpenStreakCalendar = onOpenStreakCalendar,
                    onOpenSuccessRoadmap = onOpenSuccessRoadmap,
                    onOpenQBank = { navigateTo("qbank") },
                    onOpenProgress = onOpenProgress,
                    onNavigateToProfile = { navigateTo("profile") }
                )
                // ✅ Pass goBack to screens so their in-app back buttons also work perfectly
                "qbank" -> QuestionBankScreen(onBack = goBack)
                "ai" -> AITutorScreen(modifier = Modifier.padding(padding).padding(bottom = 100.dp))
                "chat" -> CommunityChatScreen(onBack = goBack)
                "profile" -> ProfileScreen(onNavigate = navigateTo)
                "Progress" -> ProgressScreen(onBack = goBack) 
            }
        }
        
        if (showBottomNav) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .shadow(16.dp, RoundedCornerShape(34.dp))
                        .clip(RoundedCornerShape(34.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF26145F), Color(0xFF43208F))
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(icon = Icons.Default.Home, label = "Home", selected = currentRoute == "home", onClick = { navigateTo("home") }, modifier = Modifier.weight(1f))
                    BottomNavItem(icon = Icons.AutoMirrored.Filled.List, label = "Q-Bank", selected = currentRoute == "qbank", onClick = { navigateTo("qbank") }, modifier = Modifier.weight(1f))
                    BottomNavItem(icon = Icons.Default.AutoAwesome, label = "AI Tutor", selected = currentRoute == "ai", onClick = { navigateTo("ai") }, modifier = Modifier.weight(1f))
                    BottomNavItem(icon = Icons.AutoMirrored.Filled.Chat, label = "Community", selected = currentRoute == "chat", onClick = { navigateTo("chat") }, modifier = Modifier.weight(1f))
                    BottomNavItem(icon = Icons.Default.Person, label = "Profile", selected = currentRoute == "profile", onClick = { navigateTo("profile") }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val PurplePrimary = Color(0xFF7427E8)
    Box(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(24.dp)).background(if (selected) PurplePrimary else Color.Transparent).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

class AppOpenAdManager(private val application: Application) {
    companion object { const val AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294" }
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var isAppLaunchedForFirstTime = true

    init {
        loadAd()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) return
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(application, AD_UNIT_ID, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) { appOpenAd = ad; isLoadingAd = false; loadTime = Date().time }
                override fun onAdFailedToLoad(error: LoadAdError) { isLoadingAd = false }
            })
    }

    private fun isAdAvailable(): Boolean = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        return dateDifference < 3600000 * numHours
    }

    fun showAdIfAvailable(activity: Activity) {
        if (!isShowingAd && isAdAvailable()) {
            val fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { appOpenAd = null; isShowingAd = false; loadAd() }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) { appOpenAd = null; isShowingAd = false; loadAd() }
                override fun onAdShowedFullScreenContent() { isShowingAd = true }
            }
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            appOpenAd?.show(activity)
        } else { loadAd() }
    }

    private inner class AppLifecycleObserver : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            if (isAppLaunchedForFirstTime) { isAppLaunchedForFirstTime = false; return }
            val activity = owner as? Activity
            if (activity != null) { showAdIfAvailable(activity) }
        }
    }
}