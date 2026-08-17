package com.net.down.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.net.down.data.model.DownloadItem
import com.net.down.download.DownloadEngine
import kotlinx.coroutines.flow.StateFlow

class DownloadsViewModel : ViewModel() {

    val items: StateFlow<List<DownloadItem>> = DownloadEngine.items

    fun cancel(id: Long) = DownloadEngine.cancel(id)

    fun retry(item: DownloadItem) = DownloadEngine.retry(item)
}
