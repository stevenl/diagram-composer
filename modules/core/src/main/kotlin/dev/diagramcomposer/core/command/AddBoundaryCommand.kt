package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.Diagram

/**
 * Adds [boundary] to a diagram, placing it in [parent]'s children —
 * [BoundaryParent.Root] for the top level, or a specific (different)
 * boundary — at [index] (appended to the end if `null`). See
 * [AddEntityCommand]'s doc comment for why [index] only affects
 * [Diagram.rootChildren] / [Boundary.children], not [Diagram.boundaries]
 * list order.
 *
 * [boundary] must be added empty ([Boundary.children] must be empty) —
 * moving *existing* entities/boundaries into a boundary as part of adding
 * it is a distinct "restructure the tree" operation that isn't in this
 * milestone's scope (docs/implementation-plan.md Milestone 5 only lists
 * Add/Edit, not Move); adding it speculatively now would guess at an API
 * shape that milestone doesn't define yet (ai-context.md §5). Assigning
 * entities to this boundary is done by adding them directly into it via
 * [AddEntityCommand]'s `parent` argument.
 *
 * Purely additive: [undo] removes exactly what [execute] added using only
 * the constructor arguments, no memento capture needed (see [Command]'s
 * doc comment).
 */
class AddBoundaryCommand(
    private val boundary: Boundary,
    private val parent: BoundaryParent = BoundaryParent.Root,
    private val index: Int? = null,
) : Command {
    init {
        require(boundary.children.isEmpty()) {
            "AddBoundaryCommand only supports adding an empty boundary (id=${boundary.id}); " +
                "add its contents afterwards via AddEntityCommand/AddBoundaryCommand"
        }
        if (parent is BoundaryParent.InBoundary) {
            require(parent.id != boundary.id) { "Boundary '${boundary.id}' cannot be its own parent" }
        }
    }

    override fun execute(diagram: Diagram): Diagram {
        val childId = BoundaryChildId.OfBoundary(boundary.id)
        return diagram.copy(
            boundaries = insertBoundary(diagram.boundaries, childId),
            rootChildren =
                if (parent is BoundaryParent.Root) {
                    insertAt(diagram.rootChildren, childId, index)
                } else {
                    diagram.rootChildren
                },
        )
    }

    override fun undo(diagram: Diagram): Diagram {
        val childId = BoundaryChildId.OfBoundary(boundary.id)
        return diagram.copy(
            boundaries =
                diagram.boundaries
                    .filterNot { it.id == boundary.id }
                    .map { existing ->
                        if (parent is BoundaryParent.InBoundary && existing.id == parent.id) {
                            existing.copy(children = existing.children.filterNot { it == childId })
                        } else {
                            existing
                        }
                    },
            rootChildren = diagram.rootChildren.filterNot { it == childId },
        )
    }

    private fun insertBoundary(
        boundaries: List<Boundary>,
        childId: BoundaryChildId,
    ): List<Boundary> {
        val withNewBoundary = boundaries + boundary
        return if (parent is BoundaryParent.InBoundary) {
            withNewBoundary.map { existing ->
                if (existing.id == parent.id) {
                    existing.copy(children = insertAt(existing.children, childId, index))
                } else {
                    existing
                }
            }
        } else {
            withNewBoundary
        }
    }
}
