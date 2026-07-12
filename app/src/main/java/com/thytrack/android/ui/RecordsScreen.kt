package com.thytrack.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import android.widget.Toast
import com.thytrack.android.R
import com.thytrack.android.domain.model.LabRecord
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(navController: NavController, viewModel: RecordsViewModel = hiltViewModel()) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var searching by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.exportCsv(context, uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.importCsv(context, uri)
    }

    LaunchedEffect(Unit) {
        viewModel.toast.collect { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = if (searching) {
                {
                    OutlinedTextField(
                        value = search,
                        onValueChange = viewModel::setSearch,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.search_hint)) },
                    )
                }
            } else {
                { Text(stringResource(R.string.tab_records)) }
            },
            actions = {
                IconButton(onClick = { exportLauncher.launch("thytrack_records.csv") }) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_save),
                        contentDescription = stringResource(R.string.action_export_csv),
                    )
                }
                IconButton(onClick = { importLauncher.launch(arrayOf("text/csv", "*/*")) }) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_upload),
                        contentDescription = stringResource(R.string.action_import_csv),
                    )
                }
                IconButton(onClick = { searching = !searching; if (!searching) viewModel.setSearch("") }) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_search),
                        contentDescription = stringResource(R.string.action_search),
                    )
                }
                IconButton(onClick = { viewModel.toggleSort() }) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_sort_by_size),
                        contentDescription = stringResource(R.string.action_sort),
                    )
                }
            },
        )

        if (records.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.empty_records), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(records, key = { it.id }) { record ->
                    RecordListItem(
                        record = record,
                        onClick = { navController.navigate("detail/${record.id}") },
                        onDelete = { pendingDelete = record.id },
                    )
                }
                item { androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp)) }
            }
        }
    }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord(pendingDelete!!)
                    pendingDelete = null
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) } },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_message)) },
        )
    }
}

@Composable
private fun RecordListItem(record: LabRecord, onClick: () -> Unit, onDelete: () -> Unit) {
    val sdf = rememberSdf()
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(sdf.format(record.date), style = MaterialTheme.typography.titleMedium)
                Text(
                    buildString {
                        append(record.hospital.ifEmpty { "—" })
                        append("  ·  ")
                        append("TSH: ${record.tsh ?: "—"}  ")
                        append("FT4: ${record.ft4 ?: "—"}  ")
                        append("Tg: ${record.tg ?: "—"}")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_delete),
                    contentDescription = stringResource(R.string.action_delete),
                )
            }
        }
    }
}

@Composable
private fun rememberSdf(): SimpleDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.CHINA) }
