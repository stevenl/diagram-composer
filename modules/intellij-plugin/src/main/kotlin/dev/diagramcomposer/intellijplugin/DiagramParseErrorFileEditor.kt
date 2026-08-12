package dev.diagramcomposer.intellijplugin

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * Fallback visual-side [FileEditor] shown when a file's *initial* content
 * fails to parse (docs/adapters.md §9's "must never silently discard
 * information" applies to opening a file, not just to external edits made
 * while a session is already running — see
 * [DiagramComposerEditorProvider]). The source side of the split editor (a
 * real [com.intellij.openapi.fileEditor.TextEditor]) remains fully usable
 * regardless, so the file's content is never touched by this editor; fixing
 * the syntax error and reopening the file is what brings the visual editor
 * back. Milestone 10's definition of done only requires that content is
 * never corrupted, not that an unparsable file recovers a visual editor
 * without a reopen.
 */
class DiagramParseErrorFileEditor(
    private val file: VirtualFile,
    errors: List<String>,
) : UserDataHolderBase(),
    FileEditor {
    private val panel =
        JPanel(BorderLayout()).apply {
            val body = errors.joinToString("<br>") { "• ${it.escapeHtml()}" }
            add(
                JLabel(
                    "<html><b>Diagram Composer could not parse this file:</b><br>$body</html>",
                    SwingConstants.CENTER,
                ),
                BorderLayout.CENTER,
            )
        }

    override fun getComponent(): JComponent = panel

    override fun getPreferredFocusedComponent(): JComponent? = null

    override fun getName(): String = "Diagram Composer"

    override fun setState(state: FileEditorState) = Unit

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = file.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun removePropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun getCurrentLocation(): FileEditorLocation? = null

    override fun dispose() = Unit
}

private fun String.escapeHtml(): String = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
