package com.net.down.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.net.down.data.SettingsRepository
import com.net.down.download.DownloadEngine
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    var maxConcurrent by mutableStateOf(SettingsRepository.maxConcurrent)
        private set

    var defaultVideoFormat by mutableStateOf(SettingsRepository.defaultVideoFormat)
        private set

    var engineVersion by mutableStateOf<String?>(null)
        private set

    var isUpdatingEngine by mutableStateOf(false)
        private set

    var engineMessage by mutableStateOf<String?>(null)
        private set

    init {
        refreshEngineVersion()
    }

    fun setMaxConcurrent(value: Int) {
        maxConcurrent = value.coerceIn(1, 3)
        SettingsRepository.maxConcurrent = maxConcurrent
        DownloadEngine.setMaxConcurrent(maxConcurrent)
    }

    fun setDefaultVideoFormat(value: String) {
        defaultVideoFormat = value
        SettingsRepository.defaultVideoFormat = value
    }

    fun refreshEngineVersion() {
        viewModelScope.launch {
            engineVersion = withContext(Dispatchers.IO) {
                YoutubeDL.version(getApplication())
            }
        }
    }

    fun updateEngine() {
        if (isUpdatingEngine) return
        viewModelScope.launch {
            isUpdatingEngine = true
            engineMessage = null
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    YoutubeDL.getInstance().updateYoutubeDL(
                        getApplication(),
                        YoutubeDL.UpdateChannel.STABLE
                    )
                }
            }
            isUpdatingEngine = false
            engineMessage = result.fold(
                onSuccess = { status ->
                    when (status) {
                        YoutubeDL.UpdateStatus.DONE ->
                            "Engine updated. Some previously broken sites may work again."
                        YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE ->
                            "Engine is already up to date."
                        null -> "No update available."
                    }
                },
                onFailure = {
                    "Update failed. Check your internet connection."
                }
            )
            refreshEngineVersion()
        }
    }
}
