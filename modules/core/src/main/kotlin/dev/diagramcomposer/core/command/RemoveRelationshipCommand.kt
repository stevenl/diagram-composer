package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipId

/**
 * Removes the relationship identified by [relationshipId].
 *
 * [execute] captures the removed [Relationship] and its original
 * [Diagram.relationships] index, so [undo] can reinsert it at exactly that
 * position (see [Command]'s doc comment: [undo] requires a prior [execute]
 * call).
 */
class RemoveRelationshipCommand(
    private val relationshipId: RelationshipId,
) : Command {
    private var removed: Relationship? = null
    private var removedIndex: Int = -1

    override fun execute(diagram: Diagram): Diagram {
        val index = diagram.relationships.indexOfFirst { it.id == relationshipId }
        require(index >= 0) { "Relationship '$relationshipId' not found" }

        removed = diagram.relationships[index]
        removedIndex = index

        return diagram.copy(relationships = diagram.relationships.filterNot { it.id == relationshipId })
    }

    override fun undo(diagram: Diagram): Diagram {
        val relationship = checkNotNull(removed) { "undo() called before execute()" }
        return diagram.copy(relationships = insertAt(diagram.relationships, relationship, removedIndex))
    }
}
