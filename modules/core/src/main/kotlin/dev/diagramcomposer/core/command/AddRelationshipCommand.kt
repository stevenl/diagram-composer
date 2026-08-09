package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Relationship

/**
 * Adds [relationship] to a diagram's [Diagram.relationships] at [index]
 * (appended to the end if `null`). Unlike entities/boundaries, a
 * relationship has no separate "declaration order" list — the generator
 * reads [Diagram.relationships] directly (`PlantUmlC4Generator`) — so
 * [index] positions it in that list itself.
 *
 * Purely additive: [undo] removes exactly what [execute] added using only
 * the constructor arguments, no memento capture needed (see [Command]'s
 * doc comment).
 */
class AddRelationshipCommand(
    private val relationship: Relationship,
    private val index: Int? = null,
) : Command {
    override fun execute(diagram: Diagram): Diagram =
        diagram.copy(relationships = insertAt(diagram.relationships, relationship, index))

    override fun undo(diagram: Diagram): Diagram =
        diagram.copy(relationships = diagram.relationships.filterNot { it.id == relationship.id })
}
