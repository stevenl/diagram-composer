package dev.diagramcomposer.core.model

/**
 * The kind of logical grouping a [Boundary] represents
 * (docs/architecture.md §4.4), mirroring PlantUML C4's
 * `Enterprise_Boundary` / `System_Boundary` / `Container_Boundary`
 * (docs/adapters.md §15).
 */
enum class BoundaryType {
    ENTERPRISE,
    SYSTEM,
    CONTAINER,
}
