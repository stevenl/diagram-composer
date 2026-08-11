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
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipType
import dev.diagramcomposer.ui.state.suggestRelationshipId

/** Mutable form state for [AddRelationshipDialog], factored out to keep that composable short. */
private class AddRelationshipFormState(
    initialEntity: Entity,
) {
    var source by mutableStateOf(initialEntity)
    var target by mutableStateOf(initialEntity)
    var type by mutableStateOf(RelationshipType.DEFAULT)
    var description by mutableStateOf("")
    var technology by mutableStateOf("")
}

private fun AddRelationshipFormState.toRelationship(diagram: Diagram) =
    Relationship(
        id = suggestRelationshipId(diagram),
        sourceId = source.id,
        targetId = target.id,
        description = description.ifBlank { null },
        technology = technology.ifBlank { null },
        type = type,
    )

/**
 * "Add relationship" dialog (docs/implementation-plan.md Milestone 8 task 3;
 * docs/ui.md §9 "Relationship Creation"). Source and target are picked from
 * [diagram]'s existing entities (self-relationships are allowed — see
 * `Relationship`'s doc comment, which intentionally doesn't reject
 * `sourceId == targetId`) rather than a free-text field, so the resulting
 * [Relationship] can never reference an entity [diagram] doesn't contain.
 *
 * Callers must only show this dialog when [diagram] has at least one entity
 * to pick from; [DiagramComposerApp] enforces that by disabling the
 * "Add Relationship" toolbar action otherwise.
 */
@Composable
fun AddRelationshipDialog(
    diagram: Diagram,
    onDismiss: () -> Unit,
    onConfirm: (Relationship) -> Unit,
) {
    require(diagram.entities.isNotEmpty()) { "AddRelationshipDialog requires at least one entity in the diagram" }

    val form = remember(diagram) { AddRelationshipFormState(diagram.entities.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Relationship") },
        text = { AddRelationshipFields(form, diagram.entities) },
        confirmButton = {
            TextButton(onClick = { onConfirm(form.toRelationship(diagram)) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AddRelationshipFields(
    form: AddRelationshipFormState,
    entities: List<Entity>,
) {
    Column {
        DropdownField(
            spec = DropdownFieldSpec("From", entities, form.source, Entity::name),
            onSelected = { form.source = it },
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownField(
            spec = DropdownFieldSpec("To", entities, form.target, Entity::name),
            onSelected = { form.target = it },
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownField(
            spec = DropdownFieldSpec("Type", RelationshipType.entries, form.type) { it.name.lowercase() },
            onSelected = { form.type = it },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.description,
            onValueChange = { form.description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.technology,
            onValueChange = { form.technology = it },
            label = { Text("Technology") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
