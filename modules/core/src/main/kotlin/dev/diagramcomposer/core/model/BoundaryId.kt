package dev.diagramcomposer.core.model

/**
 * Uniquely identifies a [Boundary] within a [Diagram].
 */
@JvmInline
value class BoundaryId(val value: String) {
    init {
        require(value.isNotBlank()) { "BoundaryId must not be blank" }
    }

    override fun toString(): String = value
}
