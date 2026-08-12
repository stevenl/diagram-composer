package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.command.Command
import dev.diagramcomposer.core.model.Diagram

/**
 * Bridges [DiagramViewModel]'s command dispatch and manual source edits to
 * whatever actually regenerates/reparses source text — a `DiagramSession`
 * (`adapter-api`) in practice — without `ui` importing `adapter-api`
 * itself (docs/architecture.md §3.2; see `DiagramViewModel`'s doc comment
 * on why it holds its own `CommandHistory` rather than a `DiagramSession`).
 *
 * [DiagramViewModel] already re-applies every dispatched [Command] to its
 * own internal history (Milestone 8). [execute]/[undo]/[redo] mirror those
 * same operations against the caller's `DiagramSession` so the two stay in
 * lockstep, returning the session's freshly regenerated source text each
 * time (docs/architecture.md §6.2 "Visual → Text"). [applyExternalEdit]
 * implements the reverse direction, §6.1 "Text → Visual"
 * (docs/implementation-plan.md Milestone 9 task 2) — it is the `ui`-facing
 * mirror of `DiagramSession.applyExternalEdit`, with [ParseError] narrowed
 * to plain `String` messages so this interface's signature stays
 * expressible using only `core` types.
 *
 * `dev.diagramcomposer.ui.preview.PreviewApp` supplies the
 * `DiagramSession`-backed implementation today; `intellij-plugin` is
 * expected to do the same from Milestone 10 onward.
 */
interface DiagramSourceSync {
    /** Applies [command] to the session and returns its regenerated source text. */
    fun execute(command: Command): String

    /** Reverses the session's most recent command and returns its regenerated source text. */
    fun undo(): String

    /** Re-applies the session's most recently undone command and returns its regenerated source text. */
    fun redo(): String

    /** Attempts to reconcile a manual edit to [sourceText] (docs/implementation-plan.md Milestone 9 task 2). */
    fun applyExternalEdit(sourceText: String): SourceEditOutcome
}

/**
 * Outcome of [DiagramSourceSync.applyExternalEdit] — the `ui`-facing mirror
 * of `adapter-api`'s `ApplyExternalEditResult`, kept expressible using only
 * `core`/`kotlin.String` types (see [DiagramSourceSync]'s doc comment).
 */
sealed interface SourceEditOutcome {
    /** The edited source parsed successfully; [diagram] is now the session's diagram. */
    data class Applied(
        val diagram: Diagram,
    ) : SourceEditOutcome

    /** The edited source failed to parse; the session's model and source are unchanged. */
    data class Rejected(
        val errors: List<String>,
    ) : SourceEditOutcome {
        init {
            require(errors.isNotEmpty()) { "Rejected must contain at least one error" }
        }
    }
}
