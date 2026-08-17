package com.net.down.data.model

import com.net.down.util.formatDuration

data class MediaInfo(
    val sourceUrl: String,
    val title: String,
    val thumbnailUrl: String?,
    val duration: String?,
    val uploader: String?,
    val extractor: String?,
    val formats: List<FormatItem>
) {
    val durationLabel: String?
        get() = formatDuration(duration)

    val hasAudioOnlyFormats: Boolean
        get() = formats.any { !it.hasVideo && it.hasAudio }

    val videoFormats: List<FormatItem>
        get() = formats.filter { it.hasVideo && !it.hasAudio }.sortedByDescending { it.resolutionHeight }

    companion object {
        const val FORMAT_BEST = "bestvideo+bestaudio/best"
        const val FORMAT_AUDIO = "bestaudio/best"
    }
}

private val FormatItem.resolutionHeight: Int
    get() = resolution.filter { it.isDigit() }.toIntOrNull() ?: 0
