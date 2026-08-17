package com.net.down.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import java.lang.ref.WeakReference

object AdManager {

    const val START_IO_APP_ID = "207922512"

    private const val MIN_INTERVAL_MS = 45_000L

    private var isInitialized = false
    private var activityRef: WeakReference<Activity>? = null
    private var interstitial: StartAppAd? = null
    private var isLoading = false
    private var lastShownAt = 0L

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        StartAppSDK.initParams(context, START_IO_APP_ID).init()
    }

    fun attachActivity(activity: Activity) {
        activityRef = WeakReference(activity)
        loadInterstitialIfNeeded()
    }

    fun createBanner(context: Context): Banner {
        val banner = Banner(context, object : BannerListener {
            override fun onReceiveAd(view: View) {}
            override fun onFailedToReceiveAd(view: View) {}
            override fun onClick(view: View) {}
            override fun onImpression(view: View) {}
        })
        banner.load()
        return banner
    }

    private fun newInterstitial(activity: Activity): StartAppAd {
        return StartAppAd(activity).also { load(it) }
    }

    private fun load(ad: StartAppAd) {
        if (isLoading) return
        isLoading = true
        ad.loadAd(object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                isLoading = false
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                isLoading = false
            }
        })
    }

    fun loadInterstitialIfNeeded() {
        val activity = activityRef?.get() ?: return
        val current = interstitial ?: newInterstitial(activity).also { interstitial = it }
        if (!isLoading && !current.isReady()) {
            load(current)
        }
    }

    fun showInterstitialIfReady(): Boolean {
        val ad = interstitial ?: return false
        if (!ad.isReady()) return false
        val now = System.currentTimeMillis()
        if (now - lastShownAt < MIN_INTERVAL_MS) return false
        lastShownAt = now
        interstitial = null
        isLoading = false
        Handler(Looper.getMainLooper()).post {
            ad.showAd()
            loadInterstitialIfNeeded()
        }
        return true
    }
}
