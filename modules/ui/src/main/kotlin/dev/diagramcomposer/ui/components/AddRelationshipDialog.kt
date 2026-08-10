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

    var source by remember(diagram) { mutableStateOf(diagram.entities.first()) }
    var target by remember(diagram) { mutableStateOf(diagram.entities.first()) }
    var type by remember { mutableStateOf(RelationshipType.DEFAULT) }
    var description by remember { mutableStateOf("") }
    var technology by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Relationship") },
        text = {
            Column {
                DropdownField(
                    label = "From",
                    options = diagram.entities,
                    selected = source,
                    onSelected = { source = it },
                    optionLabel = Entity::name,
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownField(
                    label = "To",
                    options = diagram.entities,
                    selected = target,
                    onSelected = { target = it },
                    optionLabel = Entity::name,
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownField(
                    label = "Type",
                    options = RelationshipType.entries,
                    selected = type,
                    onSelected = { type = it },
                    optionLabel = { it.name.lowercase() },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = technology,
                    onValueChange = { technology = it },
                    label = { Text("Technology") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        Relationship(
                            id = suggestRelationshipId(diagram),
                            sourceId = source.id,
                            targetId = target.id,
                            description = description.ifBlank { null },
                            technology = technology.ifBlank { null },
                            type = type,
                        ),
                    )
                },
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
