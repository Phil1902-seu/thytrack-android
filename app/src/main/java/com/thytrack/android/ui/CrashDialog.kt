package com.thytrack.android.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

/**
 * 崩溃栈展示弹窗：展示 CrashReporter 捕获的栈，并提供「复制」按钮。
 */
@Composable
fun CrashDialog(trace: String, onDismiss: () -> Unit) {
    val clipboard: ClipboardManager = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                clipboard.setText(AnnotatedString(trace))
                onDismiss()
            }) { Text("复制并关闭") }
        },
        title = { Text("上一次崩溃信息（请复制发我定位）") },
        text = {
            Text(
                trace,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            )
        },
    )
}
