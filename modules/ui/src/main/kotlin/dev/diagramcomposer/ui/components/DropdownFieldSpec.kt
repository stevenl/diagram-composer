package dev.diagramcomposer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * What a [DropdownField] offers and currently shows, bundled into one type
 * so the composable itself stays under detekt's `LongParameterList`
 * threshold (6).
 */
data class DropdownFieldSpec<T>(
    val label: String,
    val options: List<T>,
    val selected: T,
    val optionLabel: (T) -> String,
)

/**
 * A labelled single-choice dropdown built from plain [DropdownMenu] (rather
 * than the experimental `ExposedDropdownMenuBox`), shared by the Milestone 8
 * editing dialogs (docs/implementation-plan.md Milestone 8 tasks 1-3) for
 * picking an [EntityType][dev.diagramcomposer.core.model.EntityType], a
 * [RelationshipType][dev.diagramcomposer.core.model.RelationshipType], or a
 * source/target entity.
 */
@Composable
fun <T> DropdownField(
    spec: DropdownFieldSpec<T>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("${spec.label}: ${spec.optionLabel(spec.selected)}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            spec.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(spec.optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
