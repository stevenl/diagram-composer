package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity

/**
 * A node in the read-only element tree the UI displays
 * (docs/implementation-plan.md Milestone 7 task 2; docs/engineering.md §8's
 * "Explorer").
 *
 * Mirrors the entity-or-nested-boundary shape [BoundaryChildId] already
 * expresses in `core`, but resolved to the actual [Entity]/[Boundary]
 * instances — and, for boundaries, their resolved children — so the UI
 * doesn't have to re-look anything up by id while rendering.
 */
sealed interface ElementTreeNode {
    data class EntityNode(
        val entity: Entity,
    ) : ElementTreeNode

    data class BoundaryNode(
        val boundary: Boundary,
        val children: List<ElementTreeNode>,
    ) : ElementTreeNode
}

/**
 * Builds the top-level [ElementTreeNode] list for [diagram], grouping
 * entities under their containing boundary the same way [diagram] itself
 * does: via [Diagram.rootChildren] for the top level and
 * [Boundary.children] for anything nested. Declaration order is preserved
 * throughout, since both of those lists are themselves order-preserving.
 *
 * Every id referenced by [Diagram.rootChildren]/[Boundary.children] is
 * guaranteed to resolve — [Diagram]'s own invariants (see its doc comment)
 * reject dangling references at construction time — so [getValue] here can
 * never throw for a [diagram] that exists at all.
 */
fun buildElementTree(diagram: Diagram): List<ElementTreeNode> {
    val entitiesById = diagram.entities.associateBy { it.id }
    val boundariesById = diagram.boundaries.associateBy { it.id }

    fun resolve(children: List<BoundaryChildId>): List<ElementTreeNode> =
        children.map { child ->
            when (child) {
                is BoundaryChildId.OfEntity ->
                    ElementTreeNode.EntityNode(entitiesById.getValue(child.id))
                is BoundaryChildId.OfBoundary -> {
                    val boundary = boundariesById.getValue(child.id)
                    ElementTreeNode.BoundaryNode(boundary, resolve(boundary.children))
                }
            }
        }

    return resolve(diagram.rootChildren)
}
