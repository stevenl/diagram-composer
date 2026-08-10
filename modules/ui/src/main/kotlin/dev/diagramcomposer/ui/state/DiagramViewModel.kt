package dev.diagramcomposer.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.diagramcomposer.core.model.Diagram

/**
 * Read-only Compose state holder for a single [Diagram] snapshot
 * (docs/implementation-plan.md Milestone 7 task 1).
 *
 * ## Why this holds a `Diagram`, not a `DiagramSession`
 *
 * Milestone 7 task 1 describes this as a state holder that "observes a
 * `DiagramSession`", but `DiagramSession` lives in `adapter-api`, and `ui`
 * must communicate only with the core model (docs/architecture.md §3.2) so
 * it stays reusable by every future language adapter unchanged, the same
 * reasoning that keeps `core` itself adapter-free. This class therefore
 * knows about [Diagram] only. Whoever owns a `DiagramSession` — currently
 * `dev.diagramcomposer.ui.preview.PreviewApp`; from Milestone 10 onward,
 * `intellij-plugin` — is expected to call [refresh] with `session.diagram`
 * after every change, the same way `DiagramSession.execute`/`undo`/`redo`/
 * `applyExternalEdit` each hand the caller back a fresh `Diagram`. This is
 * "observing" in the sense that matters here (the displayed diagram tracks
 * the session's), without `ui` importing `adapter-api` for it. See
 * `modules/ui/build.gradle.kts` for the one place that dependency exists
 * anyway (the manual-testing preview), and its rationale.
 *
 * No editing capability exists yet (Milestone 8), so [refresh] has no
 * production caller today; `PreviewApp` calls it indirectly by constructing
 * this class with the session's initial diagram.
 *
 * [elementTree] and [relationshipRows] are derived from [diagram] on read
 * rather than stored independently, so they can never drift out of sync
 * with it.
 */
class DiagramViewModel(
    initialDiagram: Diagram,
) {
    var diagram: Diagram by mutableStateOf(initialDiagram)
        private set

    val elementTree: List<ElementTreeNode> get() = buildElementTree(diagram)
    val relationshipRows: List<RelationshipRow> get() = buildRelationshipRows(diagram)

    /** Replaces the displayed [diagram], e.g. after a `DiagramSession` change. */
    fun refresh(newDiagram: Diagram) {
        diagram = newDiagram
    }
}
