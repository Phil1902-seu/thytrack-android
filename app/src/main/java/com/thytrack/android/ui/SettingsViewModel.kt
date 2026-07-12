package com.thytrack.android.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thytrack.android.data.repository.SettingsRepository
import com.thytrack.android.domain.model.PatientInfo
import com.thytrack.android.worker.FollowUpNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/** 设置 ViewModel（Phase 4）：汇聚 SettingsRepository 各偏好，并负责复诊提醒调度。 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val darkMode: StateFlow<Boolean> = settings.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val reminderEnabled: StateFlow<Boolean> = settings.reminderEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val followUpIntervalMonths: StateFlow<Int> = settings.followUpIntervalMonths.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6)
    val followUpAdvanceDays: StateFlow<Int> = settings.followUpAdvanceDays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    val ocrConsentGiven: StateFlow<Boolean> = settings.ocrConsentGiven.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val language: StateFlow<String> = settings.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "zh")
    val webdavUrl: StateFlow<String> = settings.webdavUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val webdavUser: StateFlow<String> = settings.webdavUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val webdavPass: StateFlow<String> = settings.webdavPass.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val patientInfo: StateFlow<PatientInfo> = settings.observePatientInfo().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PatientInfo())

    fun setDarkMode(on: Boolean) = viewModelScope.launch { settings.setDarkMode(on) }
    fun setOcrConsent(on: Boolean) = viewModelScope.launch { settings.setOcrConsentGiven(on) }
    fun setLanguage(lang: String) = viewModelScope.launch { settings.setLanguage(lang) }
    fun setFollowUpInterval(months: Int) = viewModelScope.launch { settings.setFollowUpIntervalMonths(months) }
    fun setFollowUpAdvance(days: Int) = viewModelScope.launch { settings.setFollowUpAdvanceDays(days) }
    fun setWebdavUrl(url: String) = viewModelScope.launch { settings.setWebdavUrl(url) }
    fun setWebdavUser(user: String) = viewModelScope.launch { settings.setWebdavUser(user) }
    fun setWebdavPass(pass: String) = viewModelScope.launch { settings.setWebdavPass(pass) }

    fun toggleReminder(on: Boolean) {
        viewModelScope.launch {
            settings.setReminderEnabled(on)
            if (on) FollowUpNotificationScheduler.schedule(appContext)
            else FollowUpNotificationScheduler.cancel(appContext)
        }
    }

    fun savePatient(name: String, age: Int, surgeryDate: Date, pathology: String) {
        viewModelScope.launch { settings.savePatientInfo(PatientInfo(name, age, surgeryDate, pathology)) }
    }
}
