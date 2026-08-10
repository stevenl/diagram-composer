package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.RelationshipId
import dev.diagramcomposer.core.model.RelationshipType

/**
 * A single relationship, resolved for display
 * (docs/implementation-plan.md Milestone 7 task 3): [sourceName] and
 * [targetName] replace the raw entity ids a
 * [dev.diagramcomposer.core.model.Relationship] stores, since the UI shows
 * entity names, not ids.
 */
data class RelationshipRow(
    val id: RelationshipId,
    val sourceName: String,
    val targetName: String,
    val description: String?,
    val technology: String?,
    val type: RelationshipType,
)

/**
 * Builds a [RelationshipRow] for every relationship in [diagram], in
 * declaration order. Falls back to the raw id string if a name lookup ever
 * misses — [Diagram]'s invariants mean this can't happen for a [diagram]
 * that exists at all, but the fallback keeps this a total function rather
 * than one that trusts an invariant it doesn't itself enforce.
 */
fun buildRelationshipRows(diagram: Diagram): List<RelationshipRow> {
    val namesById = diagram.entities.associate { it.id to it.name }
    return diagram.relationships.map { relationship ->
        RelationshipRow(
            id = relationship.id,
            sourceName = namesById[relationship.sourceId] ?: relationship.sourceId.toString(),
            targetName = namesById[relationship.targetId] ?: relationship.targetId.toString(),
            description = relationship.description,
            technology = relationship.technology,
            type = relationship.type,
        )
    }
}
