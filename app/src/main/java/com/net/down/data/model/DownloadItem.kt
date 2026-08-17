package com.net.down.data.model

data class DownloadItem(
    val id: Long,
    val sourceUrl: String,
    val title: String,
    val thumbnailUrl: String?,
    val formatId: String,
    val note: String,
    val extension: String,
    val isAudioExtraction: Boolean,
    val status: DownloadStatus,
    val progress: Float,
    val speed: String?,
    val eta: String?,
    val filePath: String?,
    val error: String?,
    val createdAt: Long
)
