package dev.diagramcomposer.core.model

/**
 * Identifies something a [Boundary] can contain: either an [Entity] or a
 * nested [Boundary] (docs/architecture.md §4.4 — "children").
 *
 * docs/engineering.md §5 sketches `children` as `List<String>` with a
 * comment explaining the ids may refer to entities or nested boundaries.
 * This sealed interface expresses that same "entity id or boundary id"
 * union in a way [Diagram]'s dangling-reference check can verify at compile
 * time, rather than relying on a comment. See AGENTS.md's "Kotlin
 * Guidelines" preference for sealed interfaces over untyped strings.
 */
sealed interface BoundaryChildId {
    data class OfEntity(val id: EntityId) : BoundaryChildId
    data class OfBoundary(val id: BoundaryId) : BoundaryChildId
}
