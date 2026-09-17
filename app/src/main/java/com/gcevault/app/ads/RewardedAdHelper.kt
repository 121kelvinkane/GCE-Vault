package com.gcevault.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdHelper {

    // ✅ YOUR TWO REWARDED AD UNIT IDs
    const val AD_UNIT_QUIZ = "ca-app-pub-6772916095273626/5313126682"
    const val AD_UNIT_QBANK = "ca-app-pub-6772916095273626/3187947213"

    private var adQuiz: RewardedAd? = null
    private var adQBank: RewardedAd? = null

    // ✅ Kept for backward compatibility with MainActivity.kt
    fun loadAd(context: Context) {
        loadQuizAd(context)
        loadQBankAd(context)
    }

    fun loadQuizAd(context: Context) {
        RewardedAd.load(context, AD_UNIT_QUIZ, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) { adQuiz = ad }
            override fun onAdFailedToLoad(error: LoadAdError) { adQuiz = null }
        })
    }

    fun loadQBankAd(context: Context) {
        RewardedAd.load(context, AD_UNIT_QBANK, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) { adQBank = ad }
            override fun onAdFailedToLoad(error: LoadAdError) { adQBank = null }
        })
    }

    fun showQuizAd(activity: Activity, onReward: () -> Unit, onFailed: () -> Unit) {
        if (adQuiz != null) {
            adQuiz?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { 
                    adQuiz = null
                    loadQuizAd(activity) // ✅ Reload for next time
                }
                override fun onAdFailedToShowFullScreenContent(e: com.google.android.gms.ads.AdError) { 
                    adQuiz = null
                    onFailed() 
                    loadQuizAd(activity) 
                }
                // ✅ REMOVED onUserEarnedReward from here (it belongs in the show() lambda below)
            }
            
            // ✅ Reward is handled HERE in the show() lambda
            adQuiz?.show(activity) { onReward() }
        } else {
            onFailed()
            loadQuizAd(activity)
        }
    }

    fun showQBankAd(activity: Activity, onReward: () -> Unit, onFailed: () -> Unit) {
        if (adQBank != null) {
            adQBank?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { 
                    adQBank = null
                    loadQBankAd(activity) // ✅ Reload for next time
                }
                override fun onAdFailedToShowFullScreenContent(e: com.google.android.gms.ads.AdError) { 
                    adQBank = null
                    onFailed() 
                    loadQBankAd(activity) 
                }
                // ✅ REMOVED onUserEarnedReward from here
            }
            
            // ✅ Reward is handled HERE in the show() lambda
            adQBank?.show(activity) { onReward() }
        } else {
            onFailed()
            loadQBankAd(activity)
        }
    }
}