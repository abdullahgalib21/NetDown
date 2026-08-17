package com.net.down.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.net.down.data.HistoryRepository
import com.net.down.data.model.DownloadItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class HistoryViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<DownloadItem>>(emptyList())
    val items: StateFlow<List<DownloadItem>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            _items.value = HistoryRepository.load()
        }
    }

    fun delete(item: DownloadItem) {
        _items.update { list -> list.filterNot { it.id == item.id } }
        viewModelScope.launch { HistoryRepository.save(_items.value) }
        item.filePath?.let { path ->
            runCatching { File(path).delete() }
        }
    }

    fun clearAll() {
        _items.value = emptyList()
        viewModelScope.launch { HistoryRepository.save(emptyList()) }
    }
}
