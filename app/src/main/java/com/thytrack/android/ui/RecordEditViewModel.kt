package com.thytrack.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thytrack.android.data.repository.RecordRepository
import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.RecordSource
import com.thytrack.android.util.LabRecordFields
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * 新增 / 编辑化验记录 ViewModel（Phase 2）。
 * 记录主体（date/hospital/notes）以 [LabRecord] 为源，29 指标以原始文本 [metricText] 暂存，
 * 保存时统一解析为 Double?，非法输入置 [saveError]。
 */
@HiltViewModel
class RecordEditViewModel @Inject constructor(
    private val repo: RecordRepository,
) : ViewModel() {

    private val _record = MutableStateFlow<LabRecord?>(null)
    val record: StateFlow<LabRecord?> = _record.asStateFlow()

    private val _metricText = MutableStateFlow<Map<String, String>>(emptyMap())
    val metricText: StateFlow<Map<String, String>> = _metricText.asStateFlow()

    private val _isNew = MutableStateFlow(true)
    val isNew: StateFlow<Boolean> = _isNew.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    fun load(recordId: String?) {
        viewModelScope.launch {
            if (recordId == null || recordId == "new") {
                _isNew.value = true
                _record.value = LabRecord(id = UUID.randomUUID().toString(), date = Date())
                _metricText.value = emptyMap()
            } else {
                _isNew.value = false
                val existing = repo.getById(recordId)
                _record.value = existing
                _metricText.value = existing?.let { rec ->
                    LabRecordFields.METRIC_KEYS.associateWith { key ->
                        LabRecordFields.get(rec, key)?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                            ?: ""
                    }
                } ?: emptyMap()
            }
        }
    }

    fun setMetric(key: String, text: String) {
        _metricText.update { it.toMutableMap().apply { put(key, text) } }
    }

    fun setDate(date: Long) {
        _record.update { it?.copy(date = Date(date)) }
    }

    fun setHospital(text: String) {
        _record.update { it?.copy(hospital = text) }
    }

    fun setNotes(text: String) {
        _record.update { it?.copy(notes = text) }
    }

    fun clearError() {
        _saveError.value = null
    }

    /** 保存；成功返回 true，校验失败返回 false 并设置 [saveError]。 */
    suspend fun save(): Boolean {
        val base = _record.value ?: return false
        val parsed = mutableMapOf<String, Double>()
        for ((key, text) in _metricText.value) {
            if (text.isBlank()) continue
            val v = text.toDoubleOrNull()
            if (v == null) {
                _saveError.value = "「$key」不是有效数字"
                return false
            }
            parsed[key] = v
        }
        var rec = base
        for ((key, v) in parsed) rec = LabRecordFields.set(rec, key, v)
        val toSave = rec.copy(
            updatedAt = Date(),
            source = if (_isNew.value) RecordSource.MANUAL else base.source,
        )
        withContext(Dispatchers.IO) {
            if (_isNew.value) repo.insert(toSave) else repo.update(toSave)
        }
        return true
    }
}
