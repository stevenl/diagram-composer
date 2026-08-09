package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.Diagram

/**
 * Identifies where in a [Diagram]'s containment tree an entity or nested
 * boundary lives: directly at the top level ([Root], i.e.
 * [Diagram.rootChildren]) or inside a specific [Boundary] ([InBoundary],
 * i.e. that boundary's [Boundary.children]).
 *
 * Shared by every command that places or locates something in that tree
 * ([AddEntityCommand], [RemoveEntityCommand], [AddBoundaryCommand]) rather
 * than each redefining the same "root or boundary" choice.
 */
sealed interface BoundaryParent {
    data object Root : BoundaryParent

    data class InBoundary(
        val id: BoundaryId,
    ) : BoundaryParent
}
