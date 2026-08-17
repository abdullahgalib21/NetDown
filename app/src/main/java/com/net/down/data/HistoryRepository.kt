package com.net.down.data

import android.content.Context
import com.google.gson.Gson
import com.net.down.data.model.DownloadItem
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

object HistoryRepository {
    private val gson = Gson()
    private val lock = Mutex()
    private lateinit var file: File

    fun init(context: Context) {
        file = File(context.filesDir, "history.json")
    }

    suspend fun load(): List<DownloadItem> = lock.withLock {
        runCatching {
            if (!file.exists()) emptyList()
            else gson.fromJson(file.readText(), Array<DownloadItem>::class.java).toList()
        }.getOrDefault(emptyList())
    }

    suspend fun persist(item: DownloadItem) {
        lock.withLock {
            runCatching {
                val current = if (file.exists()) {
                    gson.fromJson(file.readText(), Array<DownloadItem>::class.java).toList()
                } else emptyList()
                val merged = (listOf(item) + current.filterNot { it.id == item.id })
                file.writeText(gson.toJson(merged))
            }
        }
    }

    suspend fun save(items: List<DownloadItem>) {
        lock.withLock {
            runCatching { file.writeText(gson.toJson(items)) }
        }
    }
}
