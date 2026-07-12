package com.thytrack.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thytrack.android.R
import com.thytrack.android.domain.model.Drug
import com.thytrack.android.domain.model.MedicationChange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val medDateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

/** 用药时间线（Phase 4）：时间线 + 新增/编辑/删除。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationTimelineScreen(viewModel: MedicationViewModel = hiltViewModel()) {
    val meds by viewModel.medications.collectAsStateWithLifecycle()
    val showDialog by viewModel.showDialog.collectAsStateWithLifecycle()
    val drug by viewModel.drug.collectAsStateWithLifecycle()
    val oldDose by viewModel.oldDose.collectAsStateWithLifecycle()
    val newDose by viewModel.newDose.collectAsStateWithLifecycle()
    val reason by viewModel.reason.collectAsStateWithLifecycle()
    val dateMillis by viewModel.dateMillis.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<MedicationChange?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_medications)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openNew) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_add),
                    contentDescription = stringResource(R.string.action_add),
                )
            }
        },
    ) { inner ->
        if (meds.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.empty_medications))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(meds, key = { it.id }) { med ->
                    MedCard(med, onEdit = viewModel::openEdit, onDelete = { pendingDelete = med })
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismiss,
            confirmButton = { TextButton(onClick = viewModel::save) { Text(stringResource(R.string.save)) } },
            dismissButton = { TextButton(onClick = viewModel::dismiss) { Text(stringResource(R.string.cancel)) } },
            title = { Text(stringResource(R.string.title_medication)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    var drugExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = drugExpanded,
                        onExpandedChange = { drugExpanded = it },
                    ) {
                        TextField(
                            readOnly = true,
                            value = drug.displayName,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.field_drug)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = drugExpanded) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = drugExpanded,
                            onDismissRequest = { drugExpanded = false },
                        ) {
                            Drug.entries.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d.displayName) },
                                    onClick = { viewModel.setDrug(d); drugExpanded = false },
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = medDateFmt.format(Date(dateMillis)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.field_date)) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_today),
                                    contentDescription = stringResource(R.string.field_date),
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = oldDose,
                        onValueChange = viewModel::setOldDose,
                        label = { Text(stringResource(R.string.field_old_dose)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = newDose,
                        onValueChange = viewModel::setNewDose,
                        label = { Text(stringResource(R.string.field_new_dose)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = viewModel::setReason,
                        label = { Text(stringResource(R.string.field_reason)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDateMillis(state.selectedDateMillis ?: dateMillis)
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    pendingDelete?.let { med ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(med); pendingDelete = null }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) } },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_message)) },
        )
    }
}

@Composable
private fun MedCard(
    med: MedicationChange,
    onEdit: (MedicationChange) -> Unit,
    onDelete: (MedicationChange) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(medDateFmt.format(med.date), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Text(
                    "${med.drug.displayName}: ${med.oldDose} → ${med.newDose}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                )
                if (med.reason.isNotBlank()) {
                    Text(
                        med.reason,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                    )
                }
            }
            IconButton(onClick = { onEdit(med) }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_edit),
                    contentDescription = stringResource(R.string.action_edit),
                )
            }
            IconButton(onClick = { onDelete(med) }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_delete),
                    contentDescription = stringResource(R.string.action_delete),
                )
            }
        }
    }
}

private val MedCardColor = Color.Unspecified
