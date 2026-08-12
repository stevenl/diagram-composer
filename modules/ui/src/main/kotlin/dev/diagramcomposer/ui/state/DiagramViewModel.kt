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
 * Milestone 9 needs this class to stay in sync with a `DiagramSession`
 * anyway (source view & two-way sync) — see [DiagramSourceSync] for how
 * that's done without pulling `adapter-api` into this class's dependencies.
 *
 * [elementTree] and [relationshipRows] are derived from [diagram] on read
 * rather than stored independently, so they can never drift out of sync
 * with it.
 *
 * ## Source text (Milestone 9)
 *
 * [sourceText]/[sourceParseErrors] give `ui` a source panel to display
 * without importing `adapter-api` (docs/architecture.md §3.2): the actual
 * regeneration/reparsing is delegated to an injected [DiagramSourceSync],
 * supplied by whoever owns a real `DiagramSession` (`PreviewApp` today,
 * `intellij-plugin` from Milestone 10). When [sourceSync] is `null` (every
 * existing single-diagram-argument construction, including all Milestone
 * 7/8 tests), source stays at [initialSourceText] and
 * [applyExternalSourceEdit] is a no-op — there is nothing to regenerate
 * against.
 */
class DiagramViewModel(
    initialDiagram: Diagram,
    initialSourceText: String = "",
    private val sourceSync: DiagramSourceSync? = null,
) {
    private val history = CommandHistory(initialDiagram)

    var diagram: Diagram by mutableStateOf(initialDiagram)
        private set

    var canUndo: Boolean by mutableStateOf(history.canUndo)
        private set

    var canRedo: Boolean by mutableStateOf(history.canRedo)
        private set

    /** Source text generated from [diagram], kept in sync via [sourceSync]. */
    var sourceText: String by mutableStateOf(initialSourceText)
        private set

    /** Parse errors from the most recent rejected [applyExternalSourceEdit], if any. */
    var sourceParseErrors: List<String> by mutableStateOf(emptyList())
        private set

    val elementTree: List<ElementTreeNode> get() = buildElementTree(diagram)
    val relationshipRows: List<RelationshipRow> get() = buildRelationshipRows(diagram)

    /** Applies [command] to the current diagram (Milestone 8 tasks 1-4). */
    fun execute(command: Command) {
        history.execute(command)
        syncFromHistory()
        sourceSync?.let { sourceText = it.execute(command) }
    }

    /** Reverses the most recently executed (or redone) command (Milestone 8 task 5). */
    fun undo() {
        history.undo()
        syncFromHistory()
        sourceSync?.let { sourceText = it.undo() }
    }

    /** Re-applies the most recently undone command (Milestone 8 task 5). */
    fun redo() {
        history.redo()
        syncFromHistory()
        sourceSync?.let { sourceText = it.redo() }
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

    /**
     * Applies a manual edit made directly to the source panel
     * (docs/implementation-plan.md Milestone 9 task 2). On success, the
     * diagram is replaced wholesale (mirroring [refresh]) and [sourceText]
     * is set to exactly [newSourceText] — not regenerated — so the user's
     * own formatting survives a round trip. On failure, [diagram] and
     * [sourceText] are left untouched and the parse errors are surfaced via
     * [sourceParseErrors] (task 3) rather than corrupting existing state.
     * A no-op if no [sourceSync] was supplied.
     */
    fun applyExternalSourceEdit(newSourceText: String) {
        val sync = sourceSync ?: return
        when (val outcome = sync.applyExternalEdit(newSourceText)) {
            is SourceEditOutcome.Applied -> {
                history.reset(outcome.diagram)
                syncFromHistory()
                sourceText = newSourceText
                sourceParseErrors = emptyList()
            }
            is SourceEditOutcome.Rejected -> {
                sourceParseErrors = outcome.errors
            }
        }
    }

    private fun syncFromHistory() {
        diagram = history.diagram
        canUndo = history.canUndo
        canRedo = history.canRedo
    }
}
