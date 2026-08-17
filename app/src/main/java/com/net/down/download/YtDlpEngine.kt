package com.net.down.download

import com.yausername.youtubedl_android.YoutubeDL
import com.net.down.data.model.FormatItem
import com.net.down.data.model.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object YtDlpEngine {

    @Volatile
    var initialized = false
        private set

    @Volatile
    var initError: String? = null
        private set

    fun markInitialized() {
        initialized = true
        initError = null
    }

    fun markFailed(error: String) {
        initialized = false
        initError = error
    }

    suspend fun fetchInfo(url: String): MediaInfo = withContext(Dispatchers.IO) {
        val info = YoutubeDL.getInstance().getInfo(url)
        val formats = info.formats.orEmpty().mapNotNull { f ->
            val vcodec = f.vcodec
            val acodec = f.acodec
            val hasVideo = !vcodec.isNullOrEmpty() && vcodec != "none"
            val hasAudio = !acodec.isNullOrEmpty() && acodec != "none"
            if (!hasVideo && !hasAudio) return@mapNotNull null
            val formatId = f.formatId ?: return@mapNotNull null
            val height = f.height
            val resolution = when {
                height >= 2160 -> "4K"
                height >= 1440 -> "1440p"
                height >= 1080 -> "1080p"
                height >= 720 -> "720p"
                height >= 480 -> "480p"
                height >= 360 -> "360p"
                height >= 240 -> "240p"
                height > 0 -> "${height}p"
                hasAudio && !hasVideo -> "Audio"
                else -> f.formatNote ?: "Video"
            }
            FormatItem(
                formatId = formatId,
                note = f.formatNote ?: "",
                extension = f.ext ?: "mp4",
                resolution = resolution,
                vcodec = vcodec,
                acodec = acodec,
                fileSize = if (f.fileSize > 0) f.fileSize else f.fileSizeApproximate,
                hasVideo = hasVideo,
                hasAudio = hasAudio,
                fps = if (f.fps > 0) "${f.fps}fps" else null
            )
        }
        MediaInfo(
            sourceUrl = url,
            title = info.title?.trim()?.takeIf { it.isNotBlank() } ?: "Untitled video",
            thumbnailUrl = info.thumbnail,
            duration = if (info.duration > 0) info.duration.toString() else null,
            uploader = info.uploader,
            extractor = info.extractor,
            formats = formats
        )
    }
}
