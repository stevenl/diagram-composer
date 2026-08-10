package dev.diagramcomposer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.ui.state.suggestEntityId

/**
 * "Add entity" dialog (docs/implementation-plan.md Milestone 8 task 1;
 * docs/ui.md §5.2 "Creation Dialog"). Collects a name, an editable
 * auto-suggested identifier (docs/ui.md §5.3), a type, and optional
 * technology/description, then hands the caller a fully-formed [Entity] to
 * dispatch via `AddEntityCommand` — this composable never touches
 * [DiagramViewModel][dev.diagramcomposer.ui.state.DiagramViewModel] or
 * `core.command` directly, keeping command dispatch centralised in
 * `DiagramComposerApp`.
 *
 * [diagram] is used only to keep the suggested identifier and the
 * uniqueness check in [confirmEnabled] up to date; it is not mutated here.
 */
@Composable
fun AddEntityDialog(
    diagram: Diagram,
    onDismiss: () -> Unit,
    onConfirm: (Entity) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var idEditedByUser by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(EntityType.CONTAINER) }
    var technology by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val existingIds = remember(diagram) { diagram.entities.map { it.id.value }.toSet() }
    val effectiveId = id.ifBlank { suggestEntityId(name, diagram).value }
    val confirmEnabled = name.isNotBlank() && effectiveId.isNotBlank() && effectiveId !in existingIds

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Entity") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (!idEditedByUser) id = ""
                    },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = effectiveId,
                    onValueChange = {
                        id = it
                        idEditedByUser = true
                    },
                    label = { Text("Identifier") },
                    isError = effectiveId.isNotBlank() && effectiveId in existingIds,
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownField(
                    label = "Type",
                    options = EntityType.entries,
                    selected = type,
                    onSelected = { type = it },
                    optionLabel = { it.name.lowercase() },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = technology,
                    onValueChange = { technology = it },
                    label = { Text("Technology") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = confirmEnabled,
                onClick = {
                    onConfirm(
                        Entity(
                            id = EntityId(effectiveId),
                            name = name.trim(),
                            type = type,
                            description = description.ifBlank { null },
                            technology = technology.ifBlank { null },
                        ),
                    )
                },
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
