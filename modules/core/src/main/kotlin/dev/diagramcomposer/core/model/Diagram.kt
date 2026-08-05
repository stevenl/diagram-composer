package dev.diagramcomposer.core.model

/**
 * Aggregate root for a single architecture diagram (docs/architecture.md §4.1).
 *
 * A Diagram is immutable and self-validating: constructing one — including
 * indirectly via [copy], since Kotlin routes `copy()` back through the
 * primary constructor — enforces the invariants below by throwing
 * [IllegalArgumentException]. Modelling "invalid diagram" as a state that
 * simply cannot be constructed is simpler than a parallel valid/invalid
 * representation that every consumer of `core` would otherwise have to
 * check for (ai-context.md §5, "Keep It Simple").
 *
 * Invariants enforced:
 * - [Entity], [Relationship], and [Boundary] ids are each unique within
 *   their own collection.
 * - Every [Relationship.sourceId] and [Relationship.targetId] refers to an
 *   entity present in [entities] (no dangling relationships).
 * - Every [Boundary.children] entry refers to an entity or boundary present
 *   in this diagram (no dangling boundary contents).
 *
 * Metadata and styles (docs/architecture.md §4.1) are deferred to a later
 * milestone — no current spec section defines their shape yet, and adding
 * placeholder fields now would be speculative (ai-context.md §5).
 */
data class Diagram(
    val entities: List<Entity> = emptyList(),
    val relationships: List<Relationship> = emptyList(),
    val boundaries: List<Boundary> = emptyList(),
) {
    init {
        requireUniqueIds(entities.map { it.id }, "Entity")
        requireUniqueIds(relationships.map { it.id }, "Relationship")
        requireUniqueIds(boundaries.map { it.id }, "Boundary")

        val entityIds = entities.map { it.id }.toSet()
        for (relationship in relationships) {
            require(relationship.sourceId in entityIds) {
                "Relationship '${relationship.id}' references missing source entity '${relationship.sourceId}'"
            }
            require(relationship.targetId in entityIds) {
                "Relationship '${relationship.id}' references missing target entity '${relationship.targetId}'"
            }
        }

        val boundaryIds = boundaries.map { it.id }.toSet()
        for (boundary in boundaries) {
            for (child in boundary.children) {
                val childExists = when (child) {
                    is BoundaryChildId.OfEntity -> child.id in entityIds
                    is BoundaryChildId.OfBoundary -> child.id in boundaryIds
                }
                require(childExists) {
                    "Boundary '${boundary.id}' references missing child '$child'"
                }
            }
        }
    }

    private companion object {
        fun requireUniqueIds(ids: List<Any>, label: String) {
            val duplicates = ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
            require(duplicates.isEmpty()) { "$label ids must be unique; duplicates: $duplicates" }
        }
    }
}
