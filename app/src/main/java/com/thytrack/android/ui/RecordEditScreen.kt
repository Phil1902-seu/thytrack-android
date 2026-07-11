package com.thytrack.android.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.thytrack.android.R
import com.thytrack.android.data.local.MetricDefinitions
import com.thytrack.android.data.local.ReferenceRanges
import com.thytrack.android.domain.model.MetricField
import com.thytrack.android.domain.model.RefRange
import com.thytrack.android.util.ValueValidator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun formatRef(range: RefRange): String =
    if (range.high <= range.low) "—" else "${range.low}–${range.high}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordEditScreen(navController: NavController, recordId: String?) {
    val vm: RecordEditViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA) }

    LaunchedEffect(recordId) { vm.load(recordId) }

    val record by vm.record.collectAsStateWithLifecycle()
    val metricText by vm.metricText.collectAsStateWithLifecycle()
    val isNew by vm.isNew.collectAsStateWithLifecycle()
    val saveError by vm.saveError.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isNew) R.string.title_add_record else R.string.title_edit_record)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        scope.launch { if (vm.save()) navController.popBackStack() }
                    }) { Text(stringResource(R.string.save)) }
                },
            )
        },
    ) { padding ->
        if (record == null) {
            androidx.compose.foundation.layout.Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }
        val rec = record!!
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                ) {
                    Text(
                        stringResource(R.string.field_date),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = {
                        val cal = Calendar.getInstance().apply { time = rec.date }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                vm.setDate(
                                    Calendar.getInstance().apply { set(y, m, d) }.time.time,
                                )
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    }) { Text(sdf.format(rec.date)) }
                }
            }
            item {
                OutlinedTextField(
                    value = rec.hospital,
                    onValueChange = vm::setHospital,
                    label = { Text(stringResource(R.string.field_hospital)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )
            }
            item {
                OutlinedTextField(
                    value = rec.notes,
                    onValueChange = vm::setNotes,
                    label = { Text(stringResource(R.string.field_notes)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    minLines = 2,
                )
            }

            for ((groupKey, fields) in MetricDefinitions.GROUPS) {
                item {
                    Text(
                        MetricDefinitions.GROUP_DISPLAY_NAMES[groupKey] ?: groupKey,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                }
                items(fields) { field ->
                    MetricRow(
                        field = field,
                        text = metricText[field.key] ?: "",
                        customRefRanges = rec.customRefRanges,
                        onValueChange = { vm.setMetric(field.key, it) },
                    )
                }
            }
            item { Spacer(Modifier.padding(24.dp)) }
        }
    }

    if (saveError != null) {
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            confirmButton = { TextButton(onClick = { vm.clearError() }) { Text(stringResource(R.string.ok)) } },
            title = { Text(stringResource(R.string.input_invalid)) },
            text = { Text(saveError ?: "") },
        )
    }
}

@Composable
private fun MetricRow(
    field: MetricField,
    text: String,
    customRefRanges: Map<String, RefRange>,
    onValueChange: (String) -> Unit,
) {
    val parsed = text.toDoubleOrNull()
    val abnormal = parsed?.let { ValueValidator.isAbnormal(it, field.key, customRefRanges) } ?: false
    val flag = parsed?.let { ValueValidator.abnormalFlag(it, field.key, customRefRanges) } ?: ""
    val range = customRefRanges[field.key] ?: (ReferenceRanges.DEFAULTS[field.key] ?: RefRange(0.0, 0.0))
    val invalidNumber = text.isNotBlank() && parsed == null

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${field.label} (${field.unit})", style = MaterialTheme.typography.bodyMedium)
            if (range.high > range.low) {
                Text(
                    "参考 ${formatRef(range)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.width(120.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = abnormal || invalidNumber,
            trailingIcon = if (flag.isNotEmpty() || invalidNumber) {
                {
                    Text(
                        if (invalidNumber) "!" else flag,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else null,
        )
    }
}
