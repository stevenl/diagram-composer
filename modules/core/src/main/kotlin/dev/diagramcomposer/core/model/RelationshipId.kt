package dev.diagramcomposer.core.model

/**
 * Uniquely identifies a [Relationship] within a [Diagram].
 */
@JvmInline
value class RelationshipId(val value: String) {
    init {
        require(value.isNotBlank()) { "RelationshipId must not be blank" }
    }

    override fun toString(): String = value
}
