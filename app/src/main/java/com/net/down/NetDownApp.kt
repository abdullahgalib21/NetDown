package com.net.down

import android.app.Application
import com.net.down.ads.AdManager
import com.net.down.data.HistoryRepository
import com.net.down.data.SettingsRepository
import com.net.down.download.DownloadEngine
import com.net.down.download.YtDlpEngine
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NetDownApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        SettingsRepository.init(this)
        HistoryRepository.init(this)
        DownloadEngine.init(this)
        AdManager.init(this)

        applicationScope.launch {
            try {
                YoutubeDL.getInstance().init(this@NetDownApp)
                FFmpeg.getInstance().init(this@NetDownApp)
                YtDlpEngine.markInitialized()
            } catch (e: YoutubeDLException) {
                YtDlpEngine.markFailed(
                    "Engine setup failed. Check your internet connection and restart."
                )
            } catch (e: Exception) {
                YtDlpEngine.markFailed(e.message ?: "Engine setup failed")
            }
        }
    }
}
