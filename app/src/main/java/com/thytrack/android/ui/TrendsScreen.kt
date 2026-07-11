package com.thytrack.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.thytrack.android.R

/** 趋势图（Phase 3：Vico 集成、参考带、异常着色、双轴、用药标记）。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_trends)) }) }) { inner ->
        Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.tab_trends))
        }
    }
}
