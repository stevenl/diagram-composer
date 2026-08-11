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

/** Mutable form state for [AddEntityDialog], factored out to keep that composable short. */
private class AddEntityFormState {
    var name by mutableStateOf("")
    var manualId by mutableStateOf("")
    var idEditedByUser by mutableStateOf(false)
    var type by mutableStateOf(EntityType.CONTAINER)
    var technology by mutableStateOf("")
    var description by mutableStateOf("")
}

private fun AddEntityFormState.toEntity(id: String) =
    Entity(
        id = EntityId(id),
        name = name.trim(),
        type = type,
        description = description.ifBlank { null },
        technology = technology.ifBlank { null },
    )

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
 * uniqueness check up to date; it is not mutated here.
 */
@Composable
fun AddEntityDialog(
    diagram: Diagram,
    onDismiss: () -> Unit,
    onConfirm: (Entity) -> Unit,
) {
    val form = remember { AddEntityFormState() }
    val existingIds = remember(diagram) { diagram.entities.map { it.id.value }.toSet() }
    val effectiveId = form.manualId.ifBlank { suggestEntityId(form.name, diagram).value }
    val idTaken = effectiveId in existingIds
    val confirmEnabled = form.name.isNotBlank() && effectiveId.isNotBlank() && !idTaken

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Entity") },
        text = { AddEntityFields(form, effectiveId, idTaken) },
        confirmButton = {
            TextButton(enabled = confirmEnabled, onClick = { onConfirm(form.toEntity(effectiveId)) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AddEntityFields(
    form: AddEntityFormState,
    effectiveId: String,
    idTaken: Boolean,
) {
    Column {
        OutlinedTextField(
            value = form.name,
            onValueChange = {
                form.name = it
                if (!form.idEditedByUser) form.manualId = ""
            },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = effectiveId,
            onValueChange = {
                form.manualId = it
                form.idEditedByUser = true
            },
            label = { Text("Identifier") },
            isError = idTaken,
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownField(
            spec = DropdownFieldSpec("Type", EntityType.entries, form.type) { it.name.lowercase() },
            onSelected = { form.type = it },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.technology,
            onValueChange = { form.technology = it },
            label = { Text("Technology") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.description,
            onValueChange = { form.description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
