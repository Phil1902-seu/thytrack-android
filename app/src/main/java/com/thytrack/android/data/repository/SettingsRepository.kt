package com.thytrack.android.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thytrack.android.domain.model.PatientInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "thytrack_settings")

interface SettingsRepository {
    fun observePatientInfo(): Flow<PatientInfo>
    suspend fun savePatientInfo(info: PatientInfo)

    val sortAscending: Flow<Boolean>
    suspend fun setSortAscending(asc: Boolean)

    val darkMode: Flow<Boolean>
    suspend fun setDarkMode(on: Boolean)

    val followUpIntervalMonths: Flow<Int>
    suspend fun setFollowUpIntervalMonths(months: Int)

    val followUpAdvanceDays: Flow<Int>
    suspend fun setFollowUpAdvanceDays(days: Int)

    val ocrConsentGiven: Flow<Boolean>
    suspend fun setOcrConsentGiven(given: Boolean)

    val reminderEnabled: Flow<Boolean>
    suspend fun setReminderEnabled(on: Boolean)

    val language: Flow<String>
    suspend fun setLanguage(lang: String)

    val webdavUrl: Flow<String>
    suspend fun setWebdavUrl(url: String)

    val webdavUser: Flow<String>
    suspend fun setWebdavUser(user: String)

    val webdavPass: Flow<String>
    suspend fun setWebdavPass(pass: String)
}

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private val patientName = stringPreferencesKey("patient_name")
    private val patientAge = intPreferencesKey("patient_age")
    private val patientSurgery = longPreferencesKey("patient_surgery_date")
    private val patientPathology = stringPreferencesKey("patient_pathology")
    private val sortAsc = booleanPreferencesKey("sort_ascending")
    private val dark = booleanPreferencesKey("dark_mode")
    private val fuInterval = intPreferencesKey("followup_interval_months")
    private val fuAdvance = intPreferencesKey("followup_advance_days")
    private val ocrConsent = booleanPreferencesKey("ocr_consent")
    private val reminderOn = booleanPreferencesKey("reminder_on")
    private val lang = stringPreferencesKey("language")
    private val wdUrl = stringPreferencesKey("webdav_url")
    private val wdUser = stringPreferencesKey("webdav_user")
    private val wdPass = stringPreferencesKey("webdav_pass")

    override fun observePatientInfo(): Flow<PatientInfo> =
        context.dataStore.data.map { p ->
            PatientInfo(
                name = p[patientName] ?: "",
                age = p[patientAge] ?: 0,
                surgeryDate = Date(p[patientSurgery] ?: Date().time),
                pathology = p[patientPathology] ?: "",
            )
        }

    override suspend fun savePatientInfo(info: PatientInfo) {
        context.dataStore.edit { p ->
            p[patientName] = info.name
            p[patientAge] = info.age
            p[patientSurgery] = info.surgeryDate.time
            p[patientPathology] = info.pathology
        }
    }

    override val sortAscending = context.dataStore.data.map { it[sortAsc] ?: false }
    override suspend fun setSortAscending(asc: Boolean) {
        context.dataStore.edit { it[sortAsc] = asc }
    }

    override val darkMode = context.dataStore.data.map { it[dark] ?: false }
    override suspend fun setDarkMode(on: Boolean) {
        context.dataStore.edit { it[dark] = on }
    }

    override val followUpIntervalMonths = context.dataStore.data.map { it[fuInterval] ?: 6 }
    override suspend fun setFollowUpIntervalMonths(months: Int) {
        context.dataStore.edit { it[fuInterval] = months }
    }

    override val followUpAdvanceDays = context.dataStore.data.map { it[fuAdvance] ?: 7 }
    override suspend fun setFollowUpAdvanceDays(days: Int) {
        context.dataStore.edit { it[fuAdvance] = days }
    }

    override val ocrConsentGiven = context.dataStore.data.map { it[ocrConsent] ?: false }
    override suspend fun setOcrConsentGiven(given: Boolean) {
        context.dataStore.edit { it[ocrConsent] = given }
    }

    override val reminderEnabled = context.dataStore.data.map { it[reminderOn] ?: true }
    override suspend fun setReminderEnabled(on: Boolean) {
        context.dataStore.edit { it[reminderOn] = on }
    }

    override val language = context.dataStore.data.map { it[lang] ?: "zh" }
    override suspend fun setLanguage(l: String) {
        context.dataStore.edit { it[lang] = l }
    }

    override val webdavUrl = context.dataStore.data.map { it[wdUrl] ?: "" }
    override suspend fun setWebdavUrl(url: String) {
        context.dataStore.edit { it[wdUrl] = url }
    }

    override val webdavUser = context.dataStore.data.map { it[wdUser] ?: "" }
    override suspend fun setWebdavUser(user: String) {
        context.dataStore.edit { it[wdUser] = user }
    }

    override val webdavPass = context.dataStore.data.map { it[wdPass] ?: "" }
    override suspend fun setWebdavPass(pass: String) {
        context.dataStore.edit { it[wdPass] = pass }
    }
}
