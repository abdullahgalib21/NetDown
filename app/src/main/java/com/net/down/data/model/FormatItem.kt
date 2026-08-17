package com.net.down.data.model

import com.net.down.util.formatSize

data class FormatItem(
    val formatId: String,
    val note: String,
    val extension: String,
    val resolution: String,
    val vcodec: String?,
    val acodec: String?,
    val fileSize: Long,
    val hasVideo: Boolean,
    val hasAudio: Boolean,
    val fps: String?
) {
    val sizeLabel: String
        get() = if (fileSize > 0) formatSize(fileSize) else "—"

    val detailLine: String
        get() = buildList {
            add(extension.uppercase())
            add(resolution)
            fps?.let { add(it) }
            note.takeIf { it.isNotBlank() }?.let { add(it) }
        }.distinct().joinToString("  •  ")
}
