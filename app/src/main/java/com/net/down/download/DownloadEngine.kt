package com.net.down.download

import android.content.Context
import android.os.Environment
import com.net.down.data.HistoryRepository
import com.net.down.data.SettingsRepository
import com.net.down.data.model.DownloadItem
import com.net.down.data.model.DownloadStatus
import com.net.down.util.formatEta
import com.net.down.util.sanitizeFileName
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern

object DownloadEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _items = MutableStateFlow<List<DownloadItem>>(emptyList())
    val items: StateFlow<List<DownloadItem>> = _items.asStateFlow()

    private val idGen = AtomicLong(1)
    private val jobs = ConcurrentHashMap<Long, Job>()
    private val speedPattern = Pattern.compile("at\\s+([\\d.]+\\s?[KMGT]i?B/s)")

    private var appContext: Context? = null
    private var maxConcurrent = 1

    val activeCount: Int
        get() = _items.value.count { it.status == DownloadStatus.DOWNLOADING }

    fun init(context: Context) {
        appContext = context.applicationContext
        maxConcurrent = SettingsRepository.maxConcurrent
    }

    fun setMaxConcurrent(value: Int) {
        maxConcurrent = value.coerceIn(1, 3)
    }

    fun enqueue(
        url: String,
        title: String,
        thumbnailUrl: String?,
        formatId: String,
        note: String,
        extension: String,
        isAudioExtraction: Boolean
    ): Long {
        val id = idGen.getAndIncrement()
        val item = DownloadItem(
            id = id,
            sourceUrl = url,
            title = title,
            thumbnailUrl = thumbnailUrl,
            formatId = formatId,
            note = note,
            extension = extension,
            isAudioExtraction = isAudioExtraction,
            status = DownloadStatus.QUEUED,
            progress = 0f,
            speed = null,
            eta = null,
            filePath = null,
            error = null,
            createdAt = System.currentTimeMillis()
        )
        _items.update { it + item }
        val job = scope.launch { runDownload(item) }
        jobs[id] = job
        return id
    }

    fun cancel(id: Long) {
        YoutubeDL.getInstance().destroyProcessById(processIdOf(id))
        jobs.remove(id)?.cancel()
        update(id) { it.copy(status = DownloadStatus.CANCELLED) }
        scope.launch {
            delay(1000)
            removeFromActive(id)
        }
    }

    fun retry(item: DownloadItem) {
        enqueue(
            url = item.sourceUrl,
            title = item.title,
            thumbnailUrl = item.thumbnailUrl,
            formatId = item.formatId,
            note = item.note,
            extension = item.extension,
            isAudioExtraction = item.isAudioExtraction
        )
    }

    private fun processIdOf(id: Long): String = "netdown-$id"

    private suspend fun runDownload(initial: DownloadItem) {
        while (true) {
            if (jobs.getOrDefault(initial.id, null)?.isActive != true) return
            val active = _items.value.count { it.status == DownloadStatus.DOWNLOADING }
            if (active < maxConcurrent) break
            delay(250)
        }

        update(initial.id) { it.copy(status = DownloadStatus.DOWNLOADING) }

        try {
            val path = execute(initial)
            update(initial.id) {
                it.copy(status = DownloadStatus.COMPLETED, progress = 100f, filePath = path)
            }
            persistCurrent(initial.id)
            delay(2000)
            removeFromActive(initial.id)
        } catch (ce: CancellationException) {
            throw ce
        } catch (ce: YoutubeDL.CanceledException) {
            update(initial.id) { it.copy(status = DownloadStatus.CANCELLED) }
            delay(1000)
            removeFromActive(initial.id)
        } catch (e: Exception) {
            update(initial.id) {
                it.copy(status = DownloadStatus.FAILED, error = e.message ?: "Download failed")
            }
            persistCurrent(initial.id)
            delay(2000)
            removeFromActive(initial.id)
        }
    }

    private suspend fun persistCurrent(id: Long) {
        val current = _items.value.firstOrNull { it.id == id } ?: return
        HistoryRepository.persist(current)
    }

    private suspend fun execute(item: DownloadItem): String? {
        val context = appContext ?: error("Download engine not initialized")
        val request = YoutubeDLRequest(item.sourceUrl)
        request.addOption("-f", item.formatId)
        if (item.isAudioExtraction) {
            request.addOption("-x")
            request.addOption("--audio-format", "mp3")
            request.addOption("--audio-quality", "0")
        }
        request.addOption("--no-part")
        request.addOption("--no-mtime")
        request.addOption("--newline")

        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetDir = File(downloadsDir, "NetDown")
        if (!targetDir.exists()) targetDir.mkdirs()

        val output = File(targetDir, "${sanitizeFileName(item.title)}.%(ext)s")
        request.addOption("-o", output.absolutePath)

        return withContext(Dispatchers.IO) {
            YoutubeDL.getInstance().execute(
                request,
                processIdOf(item.id)
            ) { progress, eta, line ->
                val speed = parseSpeed(line)
                update(item.id) {
                    it.copy(
                        progress = progress.coerceIn(0f, 100f),
                        speed = speed,
                        eta = formatEta(eta)
                    )
                }
            }
            findLatestOutput(targetDir)
        }
    }

    private fun findLatestOutput(dir: File): String? =
        dir.listFiles()?.maxWithOrNull(compareBy { it.lastModified() })?.absolutePath

    private fun parseSpeed(line: String): String? {
        val m = speedPattern.find(line) ?: return null
        return m.groupValues[1].trim()
    }

    private fun update(id: Long, transform: (DownloadItem) -> DownloadItem) {
        _items.update { list -> list.map { if (it.id == id) transform(it) else it } }
    }

    private fun removeFromActive(id: Long) {
        _items.update { list ->
            list.find { it.id == id }?.let { target ->
                if (target.status == DownloadStatus.COMPLETED ||
                    target.status == DownloadStatus.FAILED ||
                    target.status == DownloadStatus.CANCELLED
                ) {
                    list.filterNot { it.id == id }
                } else list
            } ?: list
        }
    }
}
