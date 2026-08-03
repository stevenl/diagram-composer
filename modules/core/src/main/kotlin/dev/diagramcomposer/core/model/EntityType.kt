package dev.diagramcomposer.core.model

/**
 * The kind of architectural element an [Entity] represents
 * (docs/architecture.md §4.2, docs/product.md §6 "Entity Types").
 *
 * These are the language-independent core entity types that the PlantUML C4
 * adapter's element vocabulary (Person, System, Container, ContainerDb,
 * Component — docs/adapters.md §15) maps onto. Adding a new diagram language
 * must never require adding language-specific values here; unmappable
 * language constructs are an adapter concern, not a core concern.
 */
enum class EntityType {
    PERSON,
    SYSTEM,
    CONTAINER,
    COMPONENT,
    DATABASE,
}
