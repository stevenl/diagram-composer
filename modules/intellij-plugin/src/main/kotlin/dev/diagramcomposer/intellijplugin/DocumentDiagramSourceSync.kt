package dev.diagramcomposer.intellijplugin

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import dev.diagramcomposer.core.command.Command
import dev.diagramcomposer.ui.state.DiagramSourceSync
import dev.diagramcomposer.ui.state.SourceEditOutcome

/**
 * [DiagramSourceSync] decorator that keeps a real IntelliJ [Document] — not
 * just [delegate]'s in-memory `sourceText` — synchronised with every visual
 * edit (docs/implementation-plan.md Milestone 10 task 3).
 *
 * Per docs/engineering.md §2.5 ("WriteCommandAction boundaries are what
 * IntelliJ's undo stack actually records ... a thin wrapper around a
 * single WriteCommandAction, not a parallel undo mechanism competing with
 * the platform's"), each [execute]/[undo]/[redo] call here is exactly one
 * `WriteCommandAction`, so IntelliJ's own undo stack gets one entry per
 * `core` [Command] — the same granularity `CommandHistory` already uses on
 * the model side. Saving back to disk (task 3) needs no extra code beyond
 * this: IntelliJ persists a modified [Document] through the platform's
 * normal save flow once it differs from the [com.intellij.openapi.vfs.VirtualFile] it's bound to.
 *
 * [isApplyingProgrammaticChange] lets [DiagramComposerEditorProvider]'s
 * `DocumentListener` tell a programmatic write made here apart from a
 * genuine external edit (the user typing into the source editor), so it
 * only forwards the latter to [dev.diagramcomposer.ui.state.DiagramViewModel.applyExternalSourceEdit].
 *
 * Not unit tested directly — it has nothing but IntelliJ platform calls
 * ([WriteCommandAction], [Document]) around [delegate], which is what
 * carries all the actual logic and is tested in isolation
 * ([DiagramSessionSourceSync]). See Milestone 10 implementation notes for
 * why platform-level tests are deferred.
 */
class DocumentDiagramSourceSync(
    private val project: Project,
    private val document: Document,
    private val delegate: DiagramSourceSync,
) : DiagramSourceSync {
    var isApplyingProgrammaticChange: Boolean = false
        private set

    override fun execute(command: Command): String = writeThrough { delegate.execute(command) }

    override fun undo(): String = writeThrough { delegate.undo() }

    override fun redo(): String = writeThrough { delegate.redo() }

    /** Not written back to [document] — the user's own edit is already there; only [delegate] needs updating. */
    override fun applyExternalEdit(sourceText: String): SourceEditOutcome = delegate.applyExternalEdit(sourceText)

    private inline fun writeThrough(applyToDelegate: () -> String): String {
        val newText = applyToDelegate()
        if (document.text != newText) {
            isApplyingProgrammaticChange = true
            try {
                WriteCommandAction.runWriteCommandAction(project) {
                    document.setText(newText)
                }
            } finally {
                isApplyingProgrammaticChange = false
            }
        }
        return newText
    }
}
