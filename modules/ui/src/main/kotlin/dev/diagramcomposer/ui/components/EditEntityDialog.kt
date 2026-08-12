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
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityType

/**
 * "Edit entity properties" dialog (docs/implementation-plan.md Milestone 8
 * task 2; docs/ui.md §7.2 "Entity Properties"). Pre-fills from [entity] and
 * hands the caller back an updated [Entity] to dispatch via
 * `EditEntityCommand.`
 *
 * The identifier is not editable here: `EditEntityCommand` looks the target
 * entity up by [Entity.id] and replaces it in place (see that command's doc
 * comment — changing the id is modelled as remove-then-add instead, which
 * this dialog doesn't offer).
 */
@Composable
fun EditEntityDialog(
    entity: Entity,
    onDismiss: () -> Unit,
    onConfirm: (Entity) -> Unit,
) {
    var name by remember(entity) { mutableStateOf(entity.name) }
    var type by remember(entity) { mutableStateOf(entity.type) }
    var technology by remember(entity) { mutableStateOf(entity.technology.orEmpty()) }
    var description by remember(entity) { mutableStateOf(entity.description.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${entity.name}") },
        text = {
            Column {
                Text("Identifier: ${entity.id}")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
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
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirm(
                        entity.copy(
                            name = name.trim(),
                            type = type,
                            technology = technology.ifBlank { null },
                            description = description.ifBlank { null },
                        ),
                    )
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
