package com.thytrack.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thytrack.android.data.repository.RecordRepository
import com.thytrack.android.domain.model.LabRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** 记录详情 ViewModel（Phase 2）：按 id 加载单条记录，提供删除。 */
@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val repo: RecordRepository,
) : ViewModel() {

    private val _record = MutableStateFlow<LabRecord?>(null)
    val record: StateFlow<LabRecord?> = _record.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _record.value = repo.getById(id)
        }
    }

    suspend fun delete(): Boolean {
        val r = _record.value ?: return false
        withContext(Dispatchers.IO) { repo.delete(r.id) }
        _deleted.value = true
        return true
    }
}
