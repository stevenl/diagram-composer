package dev.diagramcomposer.core.model

/**
 * Represents something in the architecture being diagrammed — a person,
 * system, container, component, or database (docs/architecture.md §4.2).
 *
 * Entity is language-independent: it must never carry PlantUML- or
 * Mermaid-specific fields. Mapping to/from a specific diagram language's
 * syntax is an adapter concern (docs/adapters.md), not a core concern.
 */
data class Entity(
    val id: EntityId,
    val name: String,
    val type: EntityType,
    val description: String? = null,
    val technology: String? = null,
    val tags: List<String> = emptyList(),
    val properties: Map<String, String> = emptyMap(),
) {
    init {
        require(name.isNotBlank()) { "Entity name must not be blank (id=$id)" }
    }
}
