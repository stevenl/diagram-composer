package dev.diagramcomposer.core.model

/**
 * The kind of communication or dependency a [Relationship] represents
 * (docs/architecture.md §4.3).
 *
 * Directionality (up/down/left/right, per PlantUML's `Rel_U`/`Rel_D`/etc. —
 * docs/adapters.md §15) is layout guidance, not a distinct relationship
 * *type*, so it is not modelled here; it belongs in [Relationship.properties]
 * if and when the UI needs to preserve it.
 */
enum class RelationshipType {
    DEFAULT,
    BIDIRECTIONAL,
}
