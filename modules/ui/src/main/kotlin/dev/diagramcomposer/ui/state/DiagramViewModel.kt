package dev.diagramcomposer.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.diagramcomposer.core.command.Command
import dev.diagramcomposer.core.command.CommandHistory
import dev.diagramcomposer.core.model.Diagram

/**
 * Compose state holder for a single diagram, backed by a [CommandHistory]
 * (docs/implementation-plan.md Milestone 7 task 1; Milestone 8: "dispatching
 * `core` commands").
 *
 * ## Why this holds a `CommandHistory`, not a `DiagramSession`
 *
 * Milestone 8's UI editing flows need somewhere to dispatch [Command]s and
 * track undo/redo — [CommandHistory] already does exactly this
 * (docs/architecture.md §7). `DiagramSession` (`adapter-api`) additionally
 * regenerates source text from every change, but `ui` must communicate only
 * with the core model (docs/architecture.md §3.2), the same reasoning
 * Milestone 7 already established for this class. [CommandHistory] lives in
 * `core.command`, so wrapping it here keeps that boundary intact.
 * Synchronising a `DiagramSession`'s generated source text with UI-driven
 * edits is Milestone 9's "Source View & Two-Way Sync", not this milestone's
 * concern — see `dev.diagramcomposer.ui.preview.PreviewApp`, which still
 * only reads a session's initial diagram and does not push edits back into
 * it.
 *
 * [elementTree] and [relationshipRows] are derived from [diagram] on read
 * rather than stored independently, so they can never drift out of sync
 * with it.
 */
class DiagramViewModel(
    initialDiagram: Diagram,
) {
    private val history = CommandHistory(initialDiagram)

    var diagram: Diagram by mutableStateOf(initialDiagram)
        private set

    var canUndo: Boolean by mutableStateOf(history.canUndo)
        private set

    var canRedo: Boolean by mutableStateOf(history.canRedo)
        private set

    val elementTree: List<ElementTreeNode> get() = buildElementTree(diagram)
    val relationshipRows: List<RelationshipRow> get() = buildRelationshipRows(diagram)

    /** Applies [command] to the current diagram (Milestone 8 tasks 1-4). */
    fun execute(command: Command) {
        history.execute(command)
        syncFromHistory()
    }

    /** Reverses the most recently executed (or redone) command (Milestone 8 task 5). */
    fun undo() {
        history.undo()
        syncFromHistory()
    }

    /** Re-applies the most recently undone command (Milestone 8 task 5). */
    fun redo() {
        history.redo()
        syncFromHistory()
    }

    /**
     * Replaces the displayed diagram wholesale, clearing undo/redo — e.g.
     * after an external source edit is reconciled (mirrors
     * [CommandHistory.reset]'s own rationale: a prior command stack is
     * meaningless once the diagram it applied to has been replaced outright).
     */
    fun refresh(newDiagram: Diagram) {
        history.reset(newDiagram)
        syncFromHistory()
    }

    private fun syncFromHistory() {
        diagram = history.diagram
        canUndo = history.canUndo
        canRedo = history.canRedo
    }
}
