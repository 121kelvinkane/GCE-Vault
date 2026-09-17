package com.gcevault.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdHelper {
    private var rewardedAd: RewardedAd? = null
    private const val AD_UNIT_ID = "ca-app-pub-6772916095273626/7783929661"

    fun loadAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
            override fun onAdFailedToLoad(adError: LoadAdError) { rewardedAd = null }
        })
    }

    fun showAd(activity: Activity, onRewardEarned: () -> Unit, onAdFailed: () -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { rewardedAd = null; loadAd(activity) }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) { rewardedAd = null; onAdFailed() }
            }
            ad.show(activity) { onRewardEarned() }
        } else {
            onAdFailed()
        }
    }
}