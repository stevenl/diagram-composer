package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.Relationship

/**
 * Removes the entity identified by [entityId], cascading to remove every
 * [Relationship] that references it as source or target
 * (docs/implementation-plan.md Milestone 5 task 3). [Diagram] would reject
 * a relationship left dangling by a naive removal anyway; cascading here
 * means callers don't have to enumerate affected relationships themselves
 * before removing an entity.
 *
 * [execute] captures a memento of everything it removes — the entity
 * itself, where it sat in the containment tree, its position in
 * [Diagram.entities], and the cascaded relationships with their original
 * [Diagram.relationships] positions — so [undo] restores the diagram to
 * exactly its prior state (`undo(execute(diagram)) == diagram`), not just
 * an equivalent one. See [Command]'s doc comment: [undo] requires a prior
 * [execute] call.
 */
class RemoveEntityCommand(
    private val entityId: EntityId,
) : Command {
    private var removedEntity: Entity? = null
    private var removedEntityListIndex: Int = -1
    private var removedFrom: BoundaryParent? = null
    private var removedChildIndex: Int = -1
    private var removedRelationships: List<IndexedValue<Relationship>> = emptyList()

    override fun execute(diagram: Diagram): Diagram {
        val entityListIndex = diagram.entities.indexOfFirst { it.id == entityId }
        require(entityListIndex >= 0) { "Entity '$entityId' not found" }
        val entity = diagram.entities[entityListIndex]

        val childId = BoundaryChildId.OfEntity(entityId)
        val rootIndex = diagram.rootChildren.indexOf(childId)
        val (parent, childIndex) =
            if (rootIndex >= 0) {
                BoundaryParent.Root to rootIndex
            } else {
                val owner = diagram.boundaries.first { childId in it.children }
                BoundaryParent.InBoundary(owner.id) to owner.children.indexOf(childId)
            }

        val cascaded =
            diagram.relationships
                .withIndex()
                .filter { (_, relationship) -> relationship.sourceId == entityId || relationship.targetId == entityId }
                .toList()
        val cascadedIds = cascaded.map { it.value.id }.toSet()

        removedEntity = entity
        removedEntityListIndex = entityListIndex
        removedFrom = parent
        removedChildIndex = childIndex
        removedRelationships = cascaded

        return diagram.copy(
            entities = diagram.entities.filterNot { it.id == entityId },
            relationships = diagram.relationships.filterNot { it.id in cascadedIds },
            rootChildren =
                if (parent is BoundaryParent.Root) {
                    diagram.rootChildren.filterNot { it == childId }
                } else {
                    diagram.rootChildren
                },
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

    override fun undo(diagram: Diagram): Diagram {
        val entity = checkNotNull(removedEntity) { "undo() called before execute()" }
        val parent = checkNotNull(removedFrom) { "undo() called before execute()" }
        val childId = BoundaryChildId.OfEntity(entity.id)

        val restoredRelationships = diagram.relationships.toMutableList()
        for (indexed in removedRelationships.sortedBy { it.index }) {
            restoredRelationships.add(indexed.index.coerceIn(0, restoredRelationships.size), indexed.value)
        }

        return diagram.copy(
            entities = insertAt(diagram.entities, entity, removedEntityListIndex),
            relationships = restoredRelationships,
            rootChildren =
                if (parent is BoundaryParent.Root) {
                    insertAt(diagram.rootChildren, childId, removedChildIndex)
                } else {
                    diagram.rootChildren
                },
            boundaries =
                diagram.boundaries.map { boundary ->
                    if (parent is BoundaryParent.InBoundary && boundary.id == parent.id) {
                        boundary.copy(children = insertAt(boundary.children, childId, removedChildIndex))
                    } else {
                        boundary
                    }
                },
        )
    }
}
