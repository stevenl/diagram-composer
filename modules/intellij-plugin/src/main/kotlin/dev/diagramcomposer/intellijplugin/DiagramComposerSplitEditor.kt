package dev.diagramcomposer.intellijplugin

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview

/**
 * The split source/visual editor itself (docs/ui.md §3.1: "the primary
 * editing experience is a split editor"; docs/implementation-plan.md
 * Milestone 10 task 1). A thin subclass of the platform's own
 * [TextEditorWithPreview] — the same base class family editors like
 * Markdown's split preview use — rather than a hand-rolled `JBSplitter`,
 * per docs/engineering.md §2.5 ("conceptually similar to the platform's
 * `TextEditorWithPreview`"). [textEditor] is the real IntelliJ text editor
 * (docs/ui.md §3.2 — full syntax highlighting/completion/refactoring
 * support, none of it reimplemented here); [visualEditor] is either
 * [DiagramVisualFileEditor] or, if the file didn't initially parse,
 * [DiagramParseErrorFileEditor].
 */
class DiagramComposerSplitEditor(
    textEditor: TextEditor,
    visualEditor: FileEditor,
) : TextEditorWithPreview(textEditor, visualEditor, "Diagram Composer")
