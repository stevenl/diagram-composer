package dev.diagramcomposer.intellijplugin

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.diagramcomposer.adapter.plantumlc4.PlantUmlC4Adapter
import dev.diagramcomposer.adapterapi.DiagramSession
import dev.diagramcomposer.adapterapi.OpenResult
import dev.diagramcomposer.ui.state.DiagramViewModel

/**
 * Registers [DiagramComposerSplitEditor] for PlantUML C4 files
 * (docs/implementation-plan.md Milestone 10 tasks 1-3).
 *
 * ## Why a `FileEditorProvider`, not a tool window
 *
 * Milestone 10 task 1's literal wording ("register a tool window ... embed
 * the Compose UI inside it") conflicts with docs/engineering.md §2.5, which
 * specifies the split source/visual layout is "implemented as a
 * `FileEditorProvider` supplying a custom `FileEditor` ... not a standalone
 * window or dialog", and with docs/ui.md §3.1's split-editor layout. Per
 * ai-context.md §13 ("if implementation differs from the specifications,
 * the specifications take precedence unless instructed otherwise"), this
 * class follows engineering.md/ui.md. `docs/architecture.md` §8 separately
 * lists a tool window (entity explorer/navigation/search) as a *different*,
 * not-yet-scheduled piece of plugin functionality — unrelated to the main
 * editing surface built here.
 *
 * ## File association
 *
 * [accept] checks [VirtualFile.getName] against
 * [dev.diagramcomposer.adapterapi.AdapterMetadata.fileExtensions] directly,
 * rather than registering a competing `com.intellij.fileType` for
 * `.puml`/`.plantuml`. Those extensions are already commonly claimed by the
 * community PlantUML IntelliJ plugin; file-type *ownership* only needs to
 * be exclusive between file types, not between the *editors* different
 * plugins offer for the same file, so this sidesteps any registration
 * conflict entirely (task 2, task 4).
 *
 * ## Coexistence with a PlantUML rendering plugin (task 4)
 *
 * [getPolicy] hides the platform's plain default text editor (redundant
 * once [DiagramComposerSplitEditor] already embeds a full [TextEditor]),
 * but this has no effect on any *other* plugin's own `FileEditorProvider` —
 * a PlantUML rendering plugin registers and shows its own preview
 * completely independently. Per docs/architecture.md §9 ("Plugin updates
 * PlantUML source → PlantUML IntelliJ Plugin detects change → Preview
 * refreshes"), both editors simply watch the same
 * [com.intellij.openapi.editor.Document]/[VirtualFile]: a visual edit made
 * here regenerates and writes source text exactly like any other text
 * change, which any rendering plugin picks up the normal way. There is no
 * direct coupling between the two plugins, and the user can view both
 * side by side as separate editor tabs.
 *
 * ## Parse failure on open
 *
 * If [DiagramSession.open] fails against the file's initial content, the
 * visual side falls back to [DiagramParseErrorFileEditor] rather than
 * refusing to open the file at all — the source side stays fully usable so
 * the user can fix the syntax error (see that class's doc comment).
 */
class DiagramComposerEditorProvider : FileEditorProvider, DumbAware {
    private val adapter = PlantUmlC4Adapter()

    override fun accept(
        project: Project,
        file: VirtualFile,
    ): Boolean = adapter.metadata.fileExtensions.any { extension -> file.name.endsWith(extension) }

    override fun createEditor(
        project: Project,
        file: VirtualFile,
    ): FileEditor {
        val document =
            FileDocumentManager.getInstance().getDocument(file)
                ?: error("No Document available for ${file.path}")

        val textEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor
        val visualEditor = createVisualEditor(project, document, file)

        return DiagramComposerSplitEditor(textEditor, visualEditor)
    }

    private fun createVisualEditor(
        project: Project,
        document: Document,
        file: VirtualFile,
    ): FileEditor =
        when (val openResult = DiagramSession.open(document.text, adapter)) {
            is OpenResult.Success -> {
                val session = openResult.session
                val sourceSync = DocumentDiagramSourceSync(project, document, DiagramSessionSourceSync(session))
                val viewModel =
                    DiagramViewModel(
                        initialDiagram = session.diagram,
                        initialSourceText = session.sourceText,
                        sourceSync = sourceSync,
                    )
                document.addDocumentListener(
                    object : DocumentListener {
                        override fun documentChanged(event: DocumentEvent) {
                            if (!sourceSync.isApplyingProgrammaticChange) {
                                viewModel.applyExternalSourceEdit(document.text)
                            }
                        }
                    },
                )
                DiagramVisualFileEditor(file, viewModel)
            }
            is OpenResult.Failure -> DiagramParseErrorFileEditor(file, openResult.errors.map { it.toDisplayString() })
        }

    override fun getEditorTypeId(): String = EDITOR_TYPE_ID

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR

    companion object {
        const val EDITOR_TYPE_ID = "diagram-composer-editor"
    }
}
