package com.thytrack.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thytrack.android.data.repository.RecordRepository
import com.thytrack.android.domain.model.LabRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 记录列表 ViewModel（Phase 2 将扩展搜索/排序/批量删/草稿）。
 * 演示 仓储(Flow) → StateFlow → Compose 的单向数据流。
 */
@HiltViewModel
class RecordsViewModel @Inject constructor(
    repo: RecordRepository,
) : ViewModel() {
    val records: StateFlow<List<LabRecord>> = repo.observeRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
