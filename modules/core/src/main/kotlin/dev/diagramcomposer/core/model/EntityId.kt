package dev.diagramcomposer.core.model

/**
 * Uniquely identifies an [Entity] within a [Diagram].
 *
 * Wrapping the raw id string in a value class (rather than passing `String`
 * around directly) prevents accidentally mixing entity, relationship, and
 * boundary ids at compile time — see [RelationshipId], [BoundaryId].
 */
@JvmInline
value class EntityId(val value: String) {
    init {
        require(value.isNotBlank()) { "EntityId must not be blank" }
    }

    override fun toString(): String = value
}
