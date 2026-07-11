package com.thytrack.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thytrack.android.R
import com.thytrack.android.domain.model.LabRecord
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RecordsScreen(viewModel: RecordsViewModel = hiltViewModel()) {
    val records by viewModel.records.collectAsStateWithLifecycle()

    if (records.isEmpty()) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            Text(stringResource(R.string.empty_records))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(records, key = { it.id }) { record ->
            RecordListItem(record)
        }
    }
}

@Composable
private fun RecordListItem(record: LabRecord) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Column(Modifier.padding(12.dp)) {
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
            )
        }
    }
}
