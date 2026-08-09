package dev.diagramcomposer.core.model

/**
 * Represents something in the architecture being diagrammed — a person,
 * system, container, component, or database (docs/architecture.md §4.2).
 *
 * Entity is language-independent: it must never carry PlantUML- or
 * Mermaid-specific fields. Mapping to/from a specific diagram language's
 * syntax is an adapter concern (docs/adapters.md), not a core concern.
 *
 * [external] records whether this entity sits outside the system being
 * documented (docs/adapters.md §15 "Supported Properties" —
 * "External/Internal"), e.g. a PlantUML `Person_Ext`/`System_Ext`. This is
 * deliberately a property on [Entity] rather than a distinct [EntityType]
 * value: it is an orthogonal attribute of a person/system, not a different
 * *kind* of element, and [EntityType] is meant to stay minimal and
 * language-independent (see its doc comment).
 */
data class Entity(
    val id: EntityId,
    val name: String,
    val type: EntityType,
    val description: String? = null,
    val technology: String? = null,
    val tags: List<String> = emptyList(),
    val properties: Map<String, String> = emptyMap(),
    val external: Boolean = false,
) {
    init {
        require(name.isNotBlank()) { "Entity name must not be blank (id=$id)" }
    }
}
