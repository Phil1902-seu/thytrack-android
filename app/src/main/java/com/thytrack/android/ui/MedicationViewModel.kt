package com.thytrack.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thytrack.android.data.repository.MedicationRepository
import com.thytrack.android.domain.model.Drug
import com.thytrack.android.domain.model.MedicationChange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/** 用药时间线 ViewModel（Phase 4）。 */
@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val repository: MedicationRepository,
) : ViewModel() {

    val medications: StateFlow<List<MedicationChange>> = repository.observeMedications()
        .map { it.sortedBy { m -> m.date.time } }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _editingId = MutableStateFlow<String?>(null)
    val editingId: StateFlow<String?> = _editingId.asStateFlow()

    private val _drug = MutableStateFlow(Drug.LEVOTHYROXINE)
    val drug: StateFlow<Drug> = _drug.asStateFlow()
    private val _oldDose = MutableStateFlow("")
    val oldDose: StateFlow<String> = _oldDose.asStateFlow()
    private val _newDose = MutableStateFlow("")
    val newDose: StateFlow<String> = _newDose.asStateFlow()
    private val _reason = MutableStateFlow("")
    val reason: StateFlow<String> = _reason.asStateFlow()
    private val _dateMillis = MutableStateFlow(Date().time)
    val dateMillis: StateFlow<Long> = _dateMillis.asStateFlow()

    fun openNew() {
        _editingId.value = null
        _drug.value = Drug.LEVOTHYROXINE
        _oldDose.value = ""
        _newDose.value = ""
        _reason.value = ""
        _dateMillis.value = Date().time
        _showDialog.value = true
    }

    fun openEdit(med: MedicationChange) {
        _editingId.value = med.id
        _drug.value = med.drug
        _oldDose.value = med.oldDose.toString()
        _newDose.value = med.newDose.toString()
        _reason.value = med.reason
        _dateMillis.value = med.date.time
        _showDialog.value = true
    }

    fun dismiss() { _showDialog.value = false }

    fun setDrug(d: Drug) { _drug.value = d }
    fun setOldDose(s: String) { _oldDose.value = s }
    fun setNewDose(s: String) { _newDose.value = s }
    fun setReason(s: String) { _reason.value = s }
    fun setDateMillis(t: Long) { _dateMillis.value = t }

    fun save() {
        val med = MedicationChange(
            id = _editingId.value ?: UUID.randomUUID().toString(),
            date = Date(_dateMillis.value),
            drug = _drug.value,
            oldDose = _oldDose.value.toDoubleOrNull() ?: 0.0,
            newDose = _newDose.value.toDoubleOrNull() ?: 0.0,
            reason = _reason.value,
        )
        viewModelScope.launch {
            if (_editingId.value == null) repository.insert(med) else repository.update(med)
            _showDialog.value = false
        }
    }

    fun delete(med: MedicationChange) {
        viewModelScope.launch { repository.delete(med.id) }
    }
}
