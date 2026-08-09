package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity

/**
 * Adds [entity] to a diagram, placing it in [parent]'s children —
 * [BoundaryParent.Root] for the top level, or a specific boundary — at
 * [index] (appended to the end if `null`).
 *
 * [index] only controls *declaration order* (via [Diagram.rootChildren] or
 * [dev.diagramcomposer.core.model.Boundary.children]), which is what the
 * PlantUML C4 generator actually reads to decide emission order
 * (`PlantUmlC4Generator`) — [Diagram.entities] list order itself has no
 * effect on generated output, so [entity] is simply appended there.
 *
 * Purely additive: [undo] removes exactly what [execute] added using only
 * the constructor arguments, no memento capture needed (see [Command]'s
 * doc comment).
 */
class AddEntityCommand(
    private val entity: Entity,
    private val parent: BoundaryParent = BoundaryParent.Root,
    private val index: Int? = null,
) : Command {
    override fun execute(diagram: Diagram): Diagram {
        val childId = BoundaryChildId.OfEntity(entity.id)
        return diagram.copy(
            entities = diagram.entities + entity,
            rootChildren =
                if (parent is BoundaryParent.Root) {
                    insertAt(diagram.rootChildren, childId, index)
                } else {
                    diagram.rootChildren
                },
            boundaries = diagram.boundaries.map { boundary -> insertIntoParent(boundary, childId) },
        )
    }

    override fun undo(diagram: Diagram): Diagram {
        val childId = BoundaryChildId.OfEntity(entity.id)
        return diagram.copy(
            entities = diagram.entities.filterNot { it.id == entity.id },
            rootChildren = diagram.rootChildren.filterNot { it == childId },
            boundaries =
                diagram.boundaries.map { boundary ->
                    if (parent is BoundaryParent.InBoundary && boundary.id == parent.id) {
                        boundary.copy(children = boundary.children.filterNot { it == childId })
                    } else {
                        boundary
                    }
                },
        )
    }

    private fun insertIntoParent(
        boundary: Boundary,
        childId: BoundaryChildId,
    ) = if (parent is BoundaryParent.InBoundary && boundary.id == parent.id) {
        boundary.copy(children = insertAt(boundary.children, childId, index))
    } else {
        boundary
    }
}
