package com.thytrack.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.thytrack.android.R
import com.thytrack.android.data.local.MetricDefinitions
import com.thytrack.android.data.local.ReferenceRanges
import com.thytrack.android.domain.model.RefRange
import com.thytrack.android.util.LabRecordFields
import com.thytrack.android.util.ValueValidator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(navController: NavController, recordId: String) {
    val vm: RecordDetailViewModel = hiltViewModel()
    val record by vm.record.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showDelete by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA) }

    LaunchedEffect(recordId) { vm.load(recordId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_detail)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("edit/$recordId") }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_edit),
                            contentDescription = stringResource(R.string.action_edit),
                        )
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_delete),
                            contentDescription = stringResource(R.string.action_delete),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (record == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val rec = record!!
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(sdf.format(rec.date), style = MaterialTheme.typography.headlineSmall)
                    if (rec.hospital.isNotBlank()) {
                        Text(rec.hospital, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            if (rec.notes.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(stringResource(R.string.field_notes), style = MaterialTheme.typography.labelMedium)
                            Text(rec.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            val fields = MetricDefinitions.ALL.mapNotNull { field ->
                val v = LabRecordFields.get(rec, field.key)
                if (v != null) field to v else null
            }
            if (fields.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.detail_no_metrics),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            } else {
                items(fields) { (field, value) ->
                    MetricDetailCard(field.label, field.unit, value, field.key, rec.customRefRanges)
                }
            }
            item { Spacer(Modifier.padding(8.dp)) }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    scope.launch { if (vm.delete()) navController.popBackStack() }
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.cancel)) } },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_message)) },
        )
    }
}

@Composable
private fun MetricDetailCard(
    label: String,
    unit: String,
    value: Double,
    key: String,
    customRefRanges: Map<String, RefRange>,
) {
    val range = customRefRanges[key] ?: (ReferenceRanges.DEFAULTS[key] ?: RefRange(0.0, 0.0))
    val flag = ValueValidator.abnormalFlag(value, key, customRefRanges)
    val abnormal = ValueValidator.isAbnormal(value, key, customRefRanges)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (abnormal) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                if (range.high > range.low) {
                    Text(
                        "参考 ${if (range.high <= range.low) "—" else "${range.low}–${range.high}"} $unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Text(
                "${value.format(2)} $unit ${if (flag.isNotEmpty()) flag else ""}",
                style = MaterialTheme.typography.titleMedium,
                color = if (abnormal) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun Double.format(digits: Int): String =
    String.format(Locale.CHINA, "%.${digits}f", this)
