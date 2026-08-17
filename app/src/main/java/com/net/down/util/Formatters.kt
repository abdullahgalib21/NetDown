package com.net.down.util

import java.net.URI
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "—"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.size - 1) {
        value /= 1024
        unit++
    }
    return DecimalFormat("#.#").format(value) + " " + units[unit]
}

fun formatEta(seconds: Long): String {
    if (seconds <= 0) return ""
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%02d:%02d", m, s)
}

fun formatDuration(secondsText: String?): String? {
    val secs = secondsText?.toLongOrNull() ?: return null
    return formatEta(secs)
}

fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))

fun sanitizeFileName(name: String): String {
    val cleaned = name
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .replace(Regex("\\s+"), " ")
        .trim()
    return cleaned.ifBlank { "video" }.take(120)
}

fun hostOf(url: String): String =
    runCatching { URI(url).host }.getOrNull()?.removePrefix("www.") ?: ""

fun isHttpUrl(value: String): Boolean =
    runCatching { URI(value) }.getOrNull()?.let { it.scheme == "http" || it.scheme == "https" } ?: false
