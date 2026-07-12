package com.thytrack.android.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thytrack.android.R
import com.thytrack.android.data.repository.MedicationRepository
import com.thytrack.android.data.repository.RecordRepository
import com.thytrack.android.data.repository.SettingsRepository
import com.thytrack.android.domain.model.LabRecord
import com.thytrack.android.domain.model.MedicationChange
import com.thytrack.android.domain.model.PatientInfo
import com.thytrack.android.util.ReportPdf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 趋势图 ViewModel（Phase 3 / Phase 6.1）：记录流 + 用药流 + 当前选中指标，单向数据流驱动 Vico 图表。
 * Phase 6.1 扩展 PDF 报告导出。
 */
@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val medicationRepository: MedicationRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    val records: StateFlow<List<LabRecord>> = recordRepository.observeRecords()
        .map { list -> list.sortedBy { it.date.time } }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val medications: StateFlow<List<MedicationChange>> = medicationRepository.observeMedications()
        .map { list -> list.sortedBy { it.date.time } }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val patientInfo: StateFlow<PatientInfo> = settings.observePatientInfo()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), PatientInfo())

    private val _selectedKey = MutableStateFlow("tsh")
    val selectedKey: StateFlow<String> = _selectedKey.asStateFlow()

    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toast = _toast.asSharedFlow()

    fun setSelectedKey(key: String) {
        _selectedKey.value = key
    }

    fun exportPdf(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bytes = ReportPdf.generate(patientInfo.value, records.value)
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(bytes)
                }
            }.onSuccess { _toast.tryEmit(context.getString(R.string.pdf_export_done, records.value.size)) }
                .onFailure { _toast.tryEmit(context.getString(R.string.pdf_export_failed, it.message ?: "")) }
        }
    }
}
