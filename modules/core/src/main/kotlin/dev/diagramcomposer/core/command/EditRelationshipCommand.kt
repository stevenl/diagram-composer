package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Relationship

/**
 * Replaces the relationship with id [newRelationship.id][Relationship.id]
 * with [newRelationship] in place — see [EditEntityCommand]'s doc comment
 * for why a full-value replace is used instead of narrower per-field
 * commands, and why the id itself can't change through this command.
 *
 * [execute] captures the replaced relationship's prior value for [undo]
 * (see [Command]'s doc comment).
 */
class EditRelationshipCommand(
    private val newRelationship: Relationship,
) : Command {
    private var previousRelationship: Relationship? = null

    override fun execute(diagram: Diagram): Diagram {
        val index = diagram.relationships.indexOfFirst { it.id == newRelationship.id }
        require(index >= 0) { "Relationship '${newRelationship.id}' not found" }

        previousRelationship = diagram.relationships[index]
        val updated = diagram.relationships.toMutableList().apply { this[index] = newRelationship }
        return diagram.copy(relationships = updated)
    }

    override fun undo(diagram: Diagram): Diagram {
        val previous = checkNotNull(previousRelationship) { "undo() called before execute()" }
        val index = diagram.relationships.indexOfFirst { it.id == newRelationship.id }
        check(index >= 0) { "Relationship '${newRelationship.id}' not found" }

        val restored = diagram.relationships.toMutableList().apply { this[index] = previous }
        return diagram.copy(relationships = restored)
    }
}
