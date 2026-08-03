package dev.diagramcomposer.core.model

/**
 * Represents a logical grouping of entities and/or nested boundaries
 * (docs/architecture.md §4.4) — for example a PlantUML C4
 * `System_Boundary` or `Container_Boundary`.
 *
 * Like [Relationship], a Boundary only records the ids of what it contains;
 * [Diagram] is responsible for rejecting boundaries whose [children]
 * reference an entity or boundary it doesn't contain.
 */
data class Boundary(
    val id: BoundaryId,
    val name: String,
    val type: BoundaryType,
    val children: List<BoundaryChildId> = emptyList(),
) {
    init {
        require(name.isNotBlank()) { "Boundary name must not be blank (id=$id)" }
    }
}
