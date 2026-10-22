package com.devqissy.proxystar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devqissy.proxystar.ui.theme.Cyan
import com.devqissy.proxystar.ui.theme.TextDim
import com.devqissy.proxystar.ui.theme.TextFaint

private val DialogSurface = Color(0xFF0C1220)

private val DialogShape = RoundedCornerShape(24.dp)

@Composable
internal fun ConfirmSwitchDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    skipLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onSkip: () -> Unit
) {
    if (!visible) return

    var dontShow by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogSurface,
        shape = DialogShape,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { dontShow = !dontShow }
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontShow,
                        onCheckedChange = { dontShow = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Cyan,
                            uncheckedColor = TextFaint,
                            checkmarkColor = DialogSurface
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        skipLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (dontShow) onSkip()
                    onConfirm()
                }
            ) {
                Text(
                    confirmLabel,
                    color = Cyan,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    dismissLabel,
                    color = TextDim,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}
