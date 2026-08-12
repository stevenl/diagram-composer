package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.command.Command
import dev.diagramcomposer.core.command.CommandHistory
import dev.diagramcomposer.core.model.Diagram

/**
 * Test double for [DiagramSourceSync] that doesn't need a real
 * `DiagramSession`/`DiagramAdapter` (both live in `adapter-api`, which `ui`
 * doesn't depend on outside `PreviewApp` — see [DiagramSourceSync]'s doc
 * comment). Mirrors a real session's [execute]/[undo]/[redo] behaviour
 * using its own [CommandHistory], and lets tests script the outcome of
 * [applyExternalEdit] directly via [externalEditResult].
 */
class FakeDiagramSourceSync(
    initialDiagram: Diagram,
    private val generate: (Diagram) -> String,
) : DiagramSourceSync {
    private val history = CommandHistory(initialDiagram)

    var externalEditResult: SourceEditOutcome =
        SourceEditOutcome.Rejected(listOf("FakeDiagramSourceSync.externalEditResult not configured"))

    override fun execute(command: Command): String {
        history.execute(command)
        return generate(history.diagram)
    }

    override fun undo(): String {
        history.undo()
        return generate(history.diagram)
    }

    override fun redo(): String {
        history.redo()
        return generate(history.diagram)
    }

    override fun applyExternalEdit(sourceText: String): SourceEditOutcome = externalEditResult
}
