package dev.diagramcomposer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val ROW_SPACING = 4.dp

/**
 * Source text panel (docs/implementation-plan.md Milestone 9 task 1-3;
 * docs/ui.md §3.2, §15). A stand-in for the real IntelliJ text editor that
 * `intellij-plugin` will substitute from Milestone 10 onward
 * (docs/ui.md §3.2 "the source editor is the standard IntelliJ text
 * editor") — this Compose text field only needs to prove the two-way sync
 * itself works.
 *
 * [sourceText] is the source generated from the current diagram (task 1,
 * "live-updates from `DiagramSession` after visual edits"); editing the
 * field only updates a local buffer, reset to [sourceText] whenever it
 * changes externally, so a visual edit elsewhere never silently clobbers
 * unsaved manual edits without the user noticing the buffer no longer
 * matches. [onApply] fires with the buffer's contents once the user
 * explicitly applies it (task 2), mirroring the confirm/dismiss pattern
 * the existing dialogs already use rather than reparsing on every
 * keystroke. [parseErrors] renders inline when a manual edit was rejected
 * (task 3; docs/ui.md §15.2); the visual editor remains usable regardless,
 * since [DiagramViewModel.diagram] is left untouched on rejection.
 */
@Composable
fun sourcePanel(
    sourceText: String,
    parseErrors: List<String>,
    onApply: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var buffer by remember(sourceText) { mutableStateOf(sourceText) }

    Column(modifier = modifier.fillMaxSize().padding(ROW_SPACING)) {
        Text("Source")
        OutlinedTextField(
            value = buffer,
            onValueChange = { buffer = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
        parseErrors.forEach { error ->
            Text("⚠ $error", color = MaterialTheme.colorScheme.error)
        }
        TextButton(onClick = { onApply(buffer) }, enabled = buffer != sourceText) {
            Text("Apply")
        }
    }
}
