package com.thytrack.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.menuAnchor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thytrack.android.R
import com.thytrack.android.data.local.MetricDefinitions

/** 趋势图界面（Phase 3）：指标选择器 + Vico 图表 + 图例。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(viewModel: TrendsViewModel = hiltViewModel()) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val selectedKey by viewModel.selectedKey.collectAsStateWithLifecycle()

    val field = MetricDefinitions.ALL.firstOrNull { it.key == selectedKey }
        ?: MetricDefinitions.ALL.first()

    var expanded by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_trends)) }) }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                TextField(
                    readOnly = true,
                    value = field.label,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.title_select_metric)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    MetricDefinitions.ALL.forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f.label) },
                            onClick = {
                                viewModel.setSelectedKey(f.key)
                                expanded = false
                            },
                        )
                    }
                }
            }

            MetricChart(
                records = records,
                medications = medications,
                metricKey = selectedKey,
                modifier = Modifier.fillMaxWidth(),
            )

            // 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${field.label}（${field.unit}）",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "参考 ${field.defaultRefRange.low}–${field.defaultRefRange.high}",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LegendDot(Color(0xFF4A90D9), stringResource(R.string.legend_normal))
                LegendDot(Color(0xFFE53935), stringResource(R.string.legend_abnormal))
                if (selectedKey == "tsh") {
                    LegendDot(Color(0xFF8E24AA), stringResource(R.string.legend_dose))
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.padding(2.dp)) {
            drawCircle(color, radius = 5.dp.toPx())
        }
        Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
    }
}
