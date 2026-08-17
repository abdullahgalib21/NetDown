package com.net.down.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.net.down.ads.AdManager
import com.net.down.data.model.FormatItem
import com.net.down.data.model.MediaInfo
import com.net.down.download.DownloadEngine
import com.net.down.download.YtDlpEngine
import com.net.down.util.isHttpUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class HomeUiState(
    val urlInput: String = "",
    val isAnalyzing: Boolean = false,
    val mediaInfo: MediaInfo? = null,
    val error: String? = null,
    val engineReady: Boolean = false,
    val engineError: String? = null
)

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { waitForEngine() }
    }

    private suspend fun waitForEngine() {
        if (YtDlpEngine.initialized) {
            _state.update { it.copy(engineReady = true) }
            return
        }
        withTimeoutOrNull(120_000) {
            while (!YtDlpEngine.initialized && YtDlpEngine.initError == null) delay(500)
        }
        _state.update {
            if (YtDlpEngine.initialized) it.copy(engineReady = true)
            else it.copy(engineError = YtDlpEngine.initError ?: "Engine initialization timed out")
        }
    }

    fun updateUrl(value: String) {
        _state.update { it.copy(urlInput = value, error = null) }
    }

    fun analyze(rawUrl: String) {
        val url = rawUrl.trim()
        if (url.isEmpty()) {
            _state.update { it.copy(error = "Paste a video URL first") }
            return
        }
        if (!isHttpUrl(url)) {
            _state.update { it.copy(error = "That does not look like a valid video URL") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true, mediaInfo = null, error = null) }
            try {
                val info = YtDlpEngine.fetchInfo(url)
                _state.update { it.copy(isAnalyzing = false, mediaInfo = info) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isAnalyzing = false,
                        error = "Could not analyze this link. Make sure it is a supported video link."
                    )
                }
            }
        }
    }

    fun dismissResult() {
        _state.update { it.copy(mediaInfo = null) }
    }

    fun download(info: MediaInfo, format: FormatItem, isAudioExtraction: Boolean) {
        DownloadEngine.enqueue(
            url = info.sourceUrl,
            title = info.title,
            thumbnailUrl = info.thumbnailUrl,
            formatId = format.formatId,
            note = format.note.ifBlank { format.resolution },
            extension = if (isAudioExtraction) "mp3" else format.extension,
            isAudioExtraction = isAudioExtraction
        )
        AdManager.showInterstitialIfReady()
        _state.update { it.copy(mediaInfo = null) }
    }
}
