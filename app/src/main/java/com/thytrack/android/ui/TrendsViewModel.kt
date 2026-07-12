package com.thytrack.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thytrack.android.data.repository.MedicationRepository
import com.thytrack.android.data.repository.RecordRepository
import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.MedicationChange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 趋势图 ViewModel（Phase 3）：记录流 + 用药流 + 当前选中指标，单向数据流驱动 Vico 图表。
 */
@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val medicationRepository: MedicationRepository,
) : ViewModel() {

    val records: StateFlow<List<LabRecord>> = recordRepository.observeRecords()
        .map { list -> list.sortedBy { it.date.time } }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val medications: StateFlow<List<MedicationChange>> = medicationRepository.observeMedications()
        .map { list -> list.sortedBy { it.date.time } }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedKey = MutableStateFlow("tsh")
    val selectedKey: StateFlow<String> = _selectedKey.asStateFlow()

    fun setSelectedKey(key: String) {
        _selectedKey.value = key
    }
}
