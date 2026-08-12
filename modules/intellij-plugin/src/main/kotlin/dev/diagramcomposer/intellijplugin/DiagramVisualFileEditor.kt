package dev.diagramcomposer.intellijplugin

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.awt.ComposePanel
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import dev.diagramcomposer.ui.DiagramComposerApp
import dev.diagramcomposer.ui.state.DiagramViewModel
import java.beans.PropertyChangeListener
import javax.swing.JComponent

/**
 * The visual side of [DiagramComposerSplitEditor] — [DiagramComposerApp]
 * (`ui`, unchanged since Milestone 9) embedded into a Swing [JComponent]
 * via Compose Desktop's [ComposePanel], the standard mechanism for hosting
 * Compose UI inside a Swing host (docs/implementation-plan.md Milestone 10
 * task 1). [viewModel] is exposed so [DiagramComposerEditorProvider] can
 * wire its `DocumentListener` to
 * [DiagramViewModel.applyExternalSourceEdit] without this class needing to
 * know anything about `Document`s itself.
 *
 * Bundling Compose Desktop's runtime (skia/skiko native libraries) inside
 * an IntelliJ plugin classloader is new territory for this project and
 * hasn't been build-verified in this sandbox (`ai-context.md`'s existing
 * "Claude cannot run `./gradlew`" constraint) — flagged as the primary risk
 * to check first when building this milestone locally.
 */
class DiagramVisualFileEditor(
    private val file: VirtualFile,
    val viewModel: DiagramViewModel,
) : UserDataHolderBase(), FileEditor {
    private val composePanel =
        ComposePanel().apply {
            setContent {
                MaterialTheme {
                    DiagramComposerApp(viewModel)
                }
            }
        }

    override fun getComponent(): JComponent = composePanel

    override fun getPreferredFocusedComponent(): JComponent = composePanel

    override fun getName(): String = "Diagram Composer"

    override fun setState(state: FileEditorState) = Unit

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = file.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun removePropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun getCurrentLocation(): FileEditorLocation? = null

    override fun dispose() {
        composePanel.dispose()
    }
}
