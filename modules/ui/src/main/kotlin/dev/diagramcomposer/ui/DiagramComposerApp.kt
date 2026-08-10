package dev.diagramcomposer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.diagramcomposer.core.command.AddEntityCommand
import dev.diagramcomposer.core.command.AddRelationshipCommand
import dev.diagramcomposer.core.command.EditEntityCommand
import dev.diagramcomposer.core.command.RemoveEntityCommand
import dev.diagramcomposer.core.command.RemoveRelationshipCommand
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.ui.components.AddEntityDialog
import dev.diagramcomposer.ui.components.AddRelationshipDialog
import dev.diagramcomposer.ui.components.EditEntityDialog
import dev.diagramcomposer.ui.components.ElementExplorer
import dev.diagramcomposer.ui.components.RelationshipList
import dev.diagramcomposer.ui.components.Toolbar
import dev.diagramcomposer.ui.components.ToolbarActions
import dev.diagramcomposer.ui.components.ToolbarState
import dev.diagramcomposer.ui.state.DiagramViewModel

private const val CONTENT_PADDING_DP = 16
private val CONTENT_PADDING = CONTENT_PADDING_DP.dp

/**
 * Root composable for the Diagram Composer UI. Milestone 7 provided a
 * read-only element tree and relationship list; Milestone 8
 * (docs/implementation-plan.md) adds a [Toolbar] and the dialogs needed to
 * build a small diagram from scratch — add/edit entity, add relationship,
 * remove entity/relationship, undo/redo — each dispatching a `core` command
 * through [viewModel].
 *
 * This composable owns which dialog (if any) is currently open as local
 * Compose state; [viewModel] owns the diagram and undo/redo history itself.
 * The workspace layout is split into [DiagramWorkspace] purely to keep this
 * function short; no canvas rendering is part of this UI yet
 * (docs/engineering.md §8's `Canvas` role — see `DiagramComposerApp`'s
 * Milestone 7 predecessor's doc comment for why the tree/list `Explorer`
 * shape was chosen instead).
 */
@Composable
fun diagramComposerApp(viewModel: DiagramViewModel) {
    var addEntityDialogOpen by remember { mutableStateOf(false) }
    var addRelationshipDialogOpen by remember { mutableStateOf(false) }
    var editingEntity by remember { mutableStateOf<Entity?>(null) }

    DiagramWorkspace(
        viewModel = viewModel,
        onAddEntity = { addEntityDialogOpen = true },
        onAddRelationship = { addRelationshipDialogOpen = true },
        onEditEntity = { editingEntity = it },
    )

    if (addEntityDialogOpen) {
        AddEntityDialog(
            diagram = viewModel.diagram,
            onDismiss = { addEntityDialogOpen = false },
            onConfirm = { entity ->
                viewModel.execute(AddEntityCommand(entity))
                addEntityDialogOpen = false
            },
        )
    }

    if (addRelationshipDialogOpen) {
        AddRelationshipDialog(
            diagram = viewModel.diagram,
            onDismiss = { addRelationshipDialogOpen = false },
            onConfirm = { relationship ->
                viewModel.execute(AddRelationshipCommand(relationship))
                addRelationshipDialogOpen = false
            },
        )
    }

    editingEntity?.let { entity ->
        EditEntityDialog(
            entity = entity,
            onDismiss = { editingEntity = null },
            onConfirm = { updated ->
                viewModel.execute(EditEntityCommand(updated))
                editingEntity = null
            },
        )
    }
}

/** Toolbar plus the side-by-side element/relationship views, wired to dispatch through [viewModel]. */
@Composable
private fun DiagramWorkspace(
    viewModel: DiagramViewModel,
    onAddEntity: () -> Unit,
    onAddRelationship: () -> Unit,
    onEditEntity: (Entity) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(CONTENT_PADDING)) {
        Toolbar(
            state =
                ToolbarState(
                    canAddRelationship = viewModel.diagram.entities.isNotEmpty(),
                    canUndo = viewModel.canUndo,
                    canRedo = viewModel.canRedo,
                ),
            actions =
                ToolbarActions(
                    onAddEntity = onAddEntity,
                    onAddRelationship = onAddRelationship,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                ),
        )
        Row(modifier = Modifier.fillMaxSize()) {
            ElementExplorer(
                nodes = viewModel.elementTree,
                onEditEntity = onEditEntity,
                onRemoveEntity = { viewModel.execute(RemoveEntityCommand(it.id)) },
                modifier = Modifier.weight(1f),
            )
            RelationshipList(
                rows = viewModel.relationshipRows,
                onRemoveRelationship = { row -> viewModel.execute(RemoveRelationshipCommand(row.id)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
