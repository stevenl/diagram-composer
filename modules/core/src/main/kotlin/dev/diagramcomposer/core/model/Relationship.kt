package dev.diagramcomposer.core.model

/**
 * Represents communication or dependency between two entities
 * (docs/architecture.md §4.3).
 *
 * A Relationship only records the two [EntityId]s it connects; it does not
 * hold references to the [Entity] instances themselves, which keeps it
 * independent of any particular [Diagram] instance. [Diagram] is
 * responsible for rejecting relationships whose [sourceId]/[targetId] don't
 * resolve to an entity it contains.
 *
 * Self-relationships (`sourceId == targetId`) are intentionally not
 * rejected here — see docs/implementation-plan.md Milestone 11, which
 * treats that as an edge case to review once the rest of the model exists,
 * not an MVP invariant.
 */
data class Relationship(
    val id: RelationshipId,
    val sourceId: EntityId,
    val targetId: EntityId,
    val description: String? = null,
    val technology: String? = null,
    val type: RelationshipType = RelationshipType.DEFAULT,
    val properties: Map<String, String> = emptyMap(),
)
