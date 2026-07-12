package com.thytrack.android.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thytrack.android.BuildConfig
import com.thytrack.android.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

/** 设置（Phase 4/5 准备）：通用 / 随访提醒 / OCR / 备份 / 关于 五面板。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val reminder by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val fuInterval by viewModel.followUpIntervalMonths.collectAsStateWithLifecycle()
    val fuAdvance by viewModel.followUpAdvanceDays.collectAsStateWithLifecycle()
    val ocrConsent by viewModel.ocrConsentGiven.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val webdavUrl by viewModel.webdavUrl.collectAsStateWithLifecycle()
    val webdavUser by viewModel.webdavUser.collectAsStateWithLifecycle()
    val webdavPass by viewModel.webdavPass.collectAsStateWithLifecycle()
    val patient by viewModel.patientInfo.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // 运行时申请通知权限（Android 13+ 开启提醒时）
    val postNotificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.toggleReminder(true) }

    LaunchedEffect(Unit) {
        viewModel.toast.collect { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_settings)) }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // —— 通用 ——
            SectionCard(stringResource(R.string.title_settings_general)) {
                SwitchRow(stringResource(R.string.setting_dark_mode), darkMode) { viewModel.setDarkMode(it) }
                LanguageRow(language) { code -> viewModel.setLanguage(code); activity?.recreate() }
                HorizontalDivider()
                PatientInfoEditor(
                    patient = patient,
                    onSave = { name, age, surgery, path -> viewModel.savePatient(name, age, surgery, path) },
                )
            }

            // —— 随访提醒 ——
            SectionCard(stringResource(R.string.title_follow_up)) {
                SwitchRow(stringResource(R.string.setting_reminder), reminder) { on ->
                    if (on) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            postNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleReminder(true)
                        }
                    } else {
                        viewModel.toggleReminder(false)
                    }
                }
                NumberRow(stringResource(R.string.setting_follow_up_interval), fuInterval) { viewModel.setFollowUpInterval(it) }
                NumberRow(stringResource(R.string.setting_follow_up_advance), fuAdvance) { viewModel.setFollowUpAdvance(it) }
            }

            // —— OCR ——
            SectionCard(stringResource(R.string.title_ocr)) {
                SwitchRow(stringResource(R.string.setting_ocr_consent), ocrConsent) { viewModel.setOcrConsent(it) }
            }

            // —— 备份 ——
            SectionCard(stringResource(R.string.title_backup)) {
                OutlinedField(stringResource(R.string.setting_webdav_url), webdavUrl) { viewModel.setWebdavUrl(it) }
                OutlinedField(stringResource(R.string.setting_webdav_user), webdavUser) { viewModel.setWebdavUser(it) }
                OutlinedField(
                    stringResource(R.string.setting_webdav_pass),
                    webdavPass,
                    keyboardType = KeyboardType.Password,
                    visualPassword = true,
                ) { viewModel.setWebdavPass(it) }
                Text(
                    stringResource(R.string.setting_backup_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = viewModel::backupToWebDav, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_backup_now))
                    }
                    OutlinedButton(onClick = viewModel::restoreFromWebDav, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_restore_now))
                    }
                }
            }

            // —— 关于 ——
            SectionCard(stringResource(R.string.title_about)) {
                InfoRow(stringResource(R.string.setting_about_version), BuildConfig.VERSION_NAME)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp),
            )
            content()
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NumberRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it.filter { c -> c.isDigit() }
            text.toIntOrNull()?.let(onValueChange)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
private fun OutlinedField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualPassword: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (visualPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageRow(current: String, onSelected: (String) -> Unit) {
    val options = listOf("zh" to "简体中文", "en" to "English")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        TextField(
            readOnly = true,
            value = options.firstOrNull { it.first == current }?.second ?: current,
            onValueChange = {},
            label = { Text(stringResource(R.string.setting_language)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (code, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { onSelected(code); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientInfoEditor(
    patient: com.thytrack.android.domain.model.PatientInfo,
    onSave: (String, Int, Date, String) -> Unit,
) {
    var name by remember(patient) { mutableStateOf(patient.name) }
    var ageText by remember(patient) { mutableStateOf(if (patient.age == 0) "" else patient.age.toString()) }
    var surgeryText by remember(patient) { mutableStateOf(isoFmt.format(patient.surgeryDate)) }
    var surgeryMillis by remember(patient) { mutableStateOf(patient.surgeryDate.time) }
    var pathology by remember(patient) { mutableStateOf(patient.pathology) }
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedField(stringResource(R.string.patient_name), name) { name = it }
    NumberField(stringResource(R.string.patient_age), ageText) { ageText = it }
    // 手术日期（只读 + 日期选择器）
    OutlinedTextField(
        value = surgeryText,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.patient_surgery)) },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_today),
                    contentDescription = stringResource(R.string.patient_surgery),
                )
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    )
    OutlinedField(stringResource(R.string.patient_pathology), pathology) { pathology = it }

    Button(
        onClick = {
            onSave(
                name,
                ageText.toIntOrNull() ?: 0,
                Date(surgeryMillis),
                pathology,
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(stringResource(R.string.action_save_patient))
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = surgeryMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        surgeryMillis = it
                        surgeryText = isoFmt.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    )
}
