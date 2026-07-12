package com.thytrack.android.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thytrack.android.R
import com.thytrack.android.data.repository.RecordRepository
import com.thytrack.android.data.repository.SettingsRepository
import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.util.CsvHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * 记录列表 ViewModel（Phase 2）：仓储(Flow) → 搜索/排序 → StateFlow → Compose 单向数据流。
 * Phase 5.1 扩展 CSV 导入/导出。
 */
@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val repo: RecordRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()

    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toast = _toast.asSharedFlow()

    val records: StateFlow<List<LabRecord>> = combine(
        repo.observeRecords(),
        _search,
        settings.sortAscending,
    ) { all, query, asc ->
        val filtered = if (query.isBlank()) {
            all
        } else {
            all.filter { r ->
                sdf.format(r.date).contains(query, ignoreCase = true) ||
                    r.hospital.contains(query, ignoreCase = true) ||
                    r.notes.contains(query, ignoreCase = true)
            }
        }
        val sorted = filtered.sortedBy { it.date.time }
        if (asc) sorted else sorted.reversed()
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(s: String) {
        _search.value = s
    }

    fun toggleSort() {
        viewModelScope.launch {
            settings.setSortAscending(!settings.sortAscending.first())
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun exportCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            val records = repo.getAll()
            val csv = CsvHelper.toCsv(records)
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(csv.toByteArray(StandardCharsets.UTF_8))
                }
            }
            _toast.tryEmit(context.getString(R.string.export_done, records.size))
        }
    }

    fun importCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() } ?: ""
            }.getOrDefault("")
            val records = runCatching { CsvHelper.fromCsv(text) }.getOrDefault(emptyList())
            if (records.isNotEmpty()) repo.insertAll(records)
            _toast.tryEmit(context.getString(R.string.import_success, records.size, 0))
        }
    }
}
