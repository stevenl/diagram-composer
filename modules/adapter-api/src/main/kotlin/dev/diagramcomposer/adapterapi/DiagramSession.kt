package dev.diagramcomposer.adapterapi

import dev.diagramcomposer.core.command.Command
import dev.diagramcomposer.core.command.CommandHistory
import dev.diagramcomposer.core.model.Diagram

/**
 * Coordinates a [Diagram] with a specific [DiagramAdapter] and a
 * [CommandHistory], keeping generated source text in sync with the model
 * (docs/implementation-plan.md Milestone 6 task 1;
 * docs/architecture.md §6 "Synchronisation Architecture").
 *
 * ## Module placement
 *
 * docs/implementation-plan.md's Milestone 6 task 1 describes this
 * coordinator as living "in `core`". It instead lives here, in
 * `adapter-api`, because it necessarily depends on [DiagramAdapter] to do
 * its job (regenerating source after each command, reparsing on external
 * edits) — and `core`'s build file, docs/architecture.md §3.1, and
 * ai-context.md §6 all establish that `core` must have no dependency on
 * adapters or the adapter contract, so it can be reused by any future
 * language adapter without modification. `adapter-api` already depends on
 * `core` (`api(project(":modules:core"))`) and is where [DiagramAdapter]
 * itself is defined, making it the natural home for a type that exists
 * specifically to glue a [Diagram]/[CommandHistory] pair to an adapter.
 * This keeps `core` itself unchanged from Milestone 5, satisfying the
 * architectural boundary at the cost of a small deviation from the plan's
 * suggested (not mandated — "or similarly named") module.
 *
 * ## Two ways a diagram enters a session
 *
 * - From existing in-memory state — e.g. a freshly built [Diagram] with no
 *   corresponding file yet — via the primary constructor, which takes the
 *   [Diagram] directly.
 * - From source text — e.g. opening a real `.puml` file — via [open],
 *   which parses first and can fail, mirroring [DiagramAdapter.parse]'s
 *   own [ParseResult] rather than throwing.
 *
 * ## Reconciliation strategy for external edits
 *
 * [applyExternalEdit] implements the "apply external source edit" flow
 * (Milestone 6 task 2) as a **full replace**: the edited source is
 * reparsed from scratch into a brand-new [Diagram], which wholesale
 * replaces the session's current one. This mirrors the MVP-level decision
 * already made for [DiagramAdapter.generate] (docs/adapters.md §10, §13 —
 * adapters may fully regenerate rather than performing a targeted update),
 * and is the simplest strategy that satisfies "the model must always
 * reflect the actual source" without inventing a diff/merge algorithm that
 * nothing in the codebase needs yet (ai-context.md §5 "Keep It Simple").
 * The alternative — diffing the reparsed diagram against the current one
 * and merging field-by-field — would need to invent id-stability and
 * conflict-resolution rules that stay speculative until a concrete UI
 * workflow (Milestone 9) demonstrates full replace is actually
 * insufficient (e.g. because it discards in-flight visual-editor selection
 * state). A consequence of full replace: [CommandHistory.reset] clears
 * undo/redo, since the existing stack's mementos are meaningless against a
 * wholesale-replaced diagram (see that method's doc comment).
 */
class DiagramSession private constructor(
    initialDiagram: Diagram,
    private val adapter: DiagramAdapter,
    initialSourceText: String,
) {
    constructor(
        initialDiagram: Diagram,
        adapter: DiagramAdapter,
    ) : this(initialDiagram, adapter, adapter.generate(initialDiagram))

    private val history = CommandHistory(initialDiagram)

    /** The current diagram, reflecting every executed/undone/redone command. */
    val diagram: Diagram get() = history.diagram

    /** Source text generated from [diagram] as of the most recent change. */
    var sourceText: String = initialSourceText
        private set

    val canUndo: Boolean get() = history.canUndo
    val canRedo: Boolean get() = history.canRedo

    /** Applies [command] to the current diagram and regenerates [sourceText] from the result. */
    fun execute(command: Command): Diagram = history.execute(command).also { regenerateSourceFrom(it) }

    /** Reverses the most recently executed (or redone) command and regenerates [sourceText]. */
    fun undo(): Diagram = history.undo().also { regenerateSourceFrom(it) }

    /** Re-applies the most recently undone command and regenerates [sourceText]. */
    fun redo(): Diagram = history.redo().also { regenerateSourceFrom(it) }

    /**
     * Applies an edit made to the source outside this session (e.g. the
     * user typing directly into the text editor). Reparses
     * [newSourceText]; on success, wholesale-replaces the current diagram
     * and clears undo/redo (see this class's doc comment on
     * reconciliation strategy). On failure, leaves [diagram] and
     * [sourceText] untouched and returns the parse errors for the caller
     * to surface (Milestone 6 task 3).
     */
    fun applyExternalEdit(newSourceText: String): ApplyExternalEditResult =
        when (val result = adapter.parse(newSourceText)) {
            is ParseResult.Success -> {
                history.reset(result.diagram)
                sourceText = newSourceText
                ApplyExternalEditResult.Applied(result.diagram)
            }
            is ParseResult.Failure -> ApplyExternalEditResult.Rejected(result.errors)
        }

    private fun regenerateSourceFrom(diagram: Diagram) {
        sourceText = adapter.generate(diagram)
    }

    companion object {
        /**
         * Opens a session from [sourceText], parsing it with [adapter] first.
         * Returns [OpenResult.Failure] rather than throwing if [sourceText]
         * doesn't parse — there is no diagram to build a session around yet.
         */
        fun open(
            sourceText: String,
            adapter: DiagramAdapter,
        ): OpenResult =
            when (val result = adapter.parse(sourceText)) {
                is ParseResult.Success -> OpenResult.Success(DiagramSession(result.diagram, adapter, sourceText))
                is ParseResult.Failure -> OpenResult.Failure(result.errors)
            }
    }
}
