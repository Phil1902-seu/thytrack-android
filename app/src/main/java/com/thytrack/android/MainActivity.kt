package com.thytrack.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.thytrack.android.navigation.ThyTrackApp
import com.thytrack.android.ui.CrashDialog
import com.thytrack.android.util.CrashReporter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashReporter.install(this)
        enableEdgeToEdge()
        setContent {
            var crashTrace by remember { mutableStateOf(CrashReporter.consume(this)) }
            ThyTrackApp()
            crashTrace?.let { CrashDialog(trace = it) { crashTrace = null } }
        }
    }
}
