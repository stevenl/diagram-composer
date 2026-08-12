package dev.diagramcomposer.intellijplugin

import dev.diagramcomposer.adapterapi.ApplyExternalEditResult
import dev.diagramcomposer.adapterapi.DiagramSession
import dev.diagramcomposer.adapterapi.ParseError
import dev.diagramcomposer.core.command.Command
import dev.diagramcomposer.ui.state.DiagramSourceSync
import dev.diagramcomposer.ui.state.SourceEditOutcome

/**
 * [DiagramSourceSync] backed by a real [DiagramSession] — the
 * `intellij-plugin`-owned counterpart docs/architecture.md §3.2 anticipates
 * ("`intellij-plugin` is expected to supply its own from Milestone 10").
 *
 * Deliberately has no IntelliJ platform import, so it can be unit tested
 * the same way `DiagramSessionTest` (`adapter-api`) already is, without
 * needing the IntelliJ platform test framework this project hasn't set up
 * yet (docs/implementation-plan.md Milestone 10 notes). Everything
 * `Document`/`WriteCommandAction`-specific — the part that actually can't
 * be exercised outside a running IDE — is pushed into
 * [DocumentDiagramSourceSync], a thin decorator around this class.
 */
class DiagramSessionSourceSync(
    private val session: DiagramSession,
) : DiagramSourceSync {
    override fun execute(command: Command): String {
        session.execute(command)
        return session.sourceText
    }

    override fun undo(): String {
        session.undo()
        return session.sourceText
    }

    override fun redo(): String {
        session.redo()
        return session.sourceText
    }

    override fun applyExternalEdit(sourceText: String): SourceEditOutcome =
        when (val result = session.applyExternalEdit(sourceText)) {
            is ApplyExternalEditResult.Applied -> SourceEditOutcome.Applied(result.diagram)
            is ApplyExternalEditResult.Rejected -> SourceEditOutcome.Rejected(result.errors.map { it.toDisplayString() })
        }
}

/** Renders a [ParseError] as a single line for [DiagramViewModel.sourceParseErrors]-style display. */
internal fun ParseError.toDisplayString(): String {
    val location = if (line != null) "line $line${column?.let { ":$it" } ?: ""}: " else ""
    return "$location$message"
}
