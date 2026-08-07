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
 * [rootChildren] records the declaration order of top-level entities and
 * boundaries — the entities/boundaries that are not a child of any
 * [Boundary] — the same way [Boundary.children] does for nested content.
 * Without it, the top level would have to fall back to *some* fixed
 * convention (e.g. "all entities, then all boundaries") whenever it's
 * regenerated, which loses whatever order the original source actually
 * declared them in and breaks source -> model -> source -> model
 * round-tripping for any diagram that interleaves top-level entities and
 * boundaries. It defaults to entities-then-boundaries (in [entities] /
 * [boundaries] list order) so existing callers that don't care about
 * top-level order don't need to specify it.
 *
 * Invariants enforced:
 * - [Entity], [Relationship], and [Boundary] ids are each unique within
 *   their own collection.
 * - Every [Relationship.sourceId] and [Relationship.targetId] refers to an
 *   entity present in [entities] (no dangling relationships).
 * - Every [Boundary.children] entry refers to an entity or boundary present
 *   in this diagram (no dangling boundary contents).
 * - Every entity and every boundary appears exactly once across
 *   [rootChildren] and every [Boundary.children] combined — never zero
 *   times (which would silently drop it from generated output) and never
 *   more than once (which would duplicate it).
 *
 * Metadata and styles (docs/architecture.md §4.1) are deferred to a later
 * milestone — no current spec section defines their shape yet, and adding
 * placeholder fields now would be speculative (ai-context.md §5).
 */
data class Diagram(
    val entities: List<Entity> = emptyList(),
    val relationships: List<Relationship> = emptyList(),
    val boundaries: List<Boundary> = emptyList(),
    val rootChildren: List<BoundaryChildId> = defaultRootChildren(entities, boundaries),
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
                val childExists =
                    when (child) {
                        is BoundaryChildId.OfEntity -> child.id in entityIds
                        is BoundaryChildId.OfBoundary -> child.id in boundaryIds
                    }
                require(childExists) {
                    "Boundary '${boundary.id}' references missing child '$child'"
                }
            }
        }
        for (child in rootChildren) {
            val childExists =
                when (child) {
                    is BoundaryChildId.OfEntity -> child.id in entityIds
                    is BoundaryChildId.OfBoundary -> child.id in boundaryIds
                }
            require(childExists) {
                "rootChildren references missing child '$child'"
            }
        }

        val allChildren = rootChildren + boundaries.flatMap { it.children }
        val entityChildCounts =
            allChildren
                .filterIsInstance<BoundaryChildId.OfEntity>()
                .map { it.id }
                .groupingBy { it }
                .eachCount()
        val boundaryChildCounts =
            allChildren
                .filterIsInstance<BoundaryChildId.OfBoundary>()
                .map { it.id }
                .groupingBy { it }
                .eachCount()
        for (entity in entities) {
            val count = entityChildCounts[entity.id] ?: 0
            require(count == 1) {
                "Entity '${entity.id}' must appear exactly once across rootChildren and boundary children, found $count"
            }
        }
        for (boundary in boundaries) {
            val count = boundaryChildCounts[boundary.id] ?: 0
            require(count == 1) {
                "Boundary '${boundary.id}' must appear once in root/boundary children; found $count"
            }
        }
    }

    private companion object {
        fun requireUniqueIds(
            ids: List<Any>,
            label: String,
        ) {
            val duplicates =
                ids
                    .groupingBy { it }
                    .eachCount()
                    .filterValues { it > 1 }
                    .keys
            require(duplicates.isEmpty()) { "$label ids must be unique; duplicates: $duplicates" }
        }

        fun defaultRootChildren(
            entities: List<Entity>,
            boundaries: List<Boundary>,
        ): List<BoundaryChildId> {
            val childEntityIds =
                boundaries
                    .asSequence()
                    .flatMap { it.children }
                    .filterIsInstance<BoundaryChildId.OfEntity>()
                    .map { it.id }
                    .toSet()
            val childBoundaryIds =
                boundaries
                    .asSequence()
                    .flatMap { it.children }
                    .filterIsInstance<BoundaryChildId.OfBoundary>()
                    .map { it.id }
                    .toSet()

            val topEntities = entities.filter { it.id !in childEntityIds }.map { BoundaryChildId.OfEntity(it.id) }
            val topBoundaries =
                boundaries.filter { it.id !in childBoundaryIds }.map { BoundaryChildId.OfBoundary(it.id) }
            return topEntities + topBoundaries
        }
    }
}
