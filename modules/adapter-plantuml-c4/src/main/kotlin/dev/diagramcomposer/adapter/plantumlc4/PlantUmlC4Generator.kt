package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.BoundaryType
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipType

/**
 * Generates PlantUML C4 source text from a [Diagram] (docs/adapters.md §15,
 * §10 "Generation Requirements").
 *
 * ## Output shape
 *
 * ```text
 * @startuml
 * !include <C4/C4_Container>
 *
 * <top-level entities and boundaries, in Diagram.rootChildren order>
 *
 * <relationships>
 * @enduml
 * ```
 *
 * Entities/boundaries that are a child of some [Boundary] (per
 * [Boundary.children]) are only emitted once, nested inside that boundary's
 * `{ }` block (recursively, for nested boundaries); everything else is a
 * top-level element, emitted in [Diagram.rootChildren] order. Relationships
 * otherwise follow [Diagram.relationships] list order — deterministic, but
 * not necessarily the order of any source this diagram was originally
 * parsed from (docs/adapters.md §10 only requires *semantic* round-trip
 * stability, not byte-identical output). Top-level entity/boundary order
 * *is* preserved exactly, via [Diagram.rootChildren] — see its doc comment
 * for why that one is load-bearing rather than cosmetic.
 *
 * ## Argument style
 *
 * Every optional field (technology, description, tags, and any leftover
 * `properties`) is always emitted as a `$name="value"` named argument
 * rather than a positional one. Real C4-PlantUML macros accept both styles,
 * and always using named arguments avoids the ambiguity of positional
 * "gaps" (e.g. a technology-less entity that still has a description) —
 * simpler and unambiguous beats mimicking exactly how a human would
 * typically hand-write the same diagram (ai-context.md §5 "Keep It
 * Simple").
 *
 * ## Relationship ids
 *
 * PlantUML's `Rel`-family macros have no natural id/alias slot, so
 * [Relationship.id] is round-tripped via this adapter's own `$id=` named
 * argument (recognised by [PlantUmlC4Parser]) — see
 * [PlantUmlC4Generator.renderRelationship].
 *
 * ## Known limitation
 *
 * Quoted values escape embedded `"` as `\"`, matching [PlantUmlC4Parser]'s
 * unescaping. A literal backslash inside a value is not separately escaped
 * (PlantUML C4 source doesn't otherwise need one), so a value containing a
 * backslash will not round-trip byte-for-byte; this is an accepted MVP gap,
 * not a correctness requirement of docs/adapters.md §10.
 */
internal object PlantUmlC4Generator {
    fun generate(diagram: Diagram): String {
        val entitiesById = diagram.entities.associateBy { it.id }
        val boundariesById = diagram.boundaries.associateBy { it.id }

        val body = StringBuilder()
        for (child in diagram.rootChildren) {
            when (child) {
                is BoundaryChildId.OfEntity -> {
                    body.appendLine(renderEntity(entitiesById.getValue(child.id), indent = 0))
                }

                is BoundaryChildId.OfBoundary -> {
                    renderBoundary(
                        boundariesById.getValue(child.id),
                        boundariesById,
                        entitiesById,
                        indent = 0,
                        out = body,
                    )
                }
            }
        }

        return buildString {
            appendLine("@startuml")
            appendLine("!include <C4/C4_Container>")
            appendLine()
            append(body)
            if (diagram.relationships.isNotEmpty()) {
                appendLine()
                for (relationship in diagram.relationships) {
                    appendLine(renderRelationship(relationship))
                }
            }
            appendLine("@enduml")
        }
    }

    private fun renderEntity(
        entity: Entity,
        indent: Int,
    ): String {
        val macroName = macroNameFor(entity)
        val args = mutableListOf(entity.id.toString(), quote(entity.name))
        entity.technology?.let { args += namedArg("techn", it) }
        entity.description?.let { args += namedArg("descr", it) }
        if (entity.tags.isNotEmpty()) args += namedArg("tags", entity.tags.joinToString(","))
        for ((key, value) in entity.properties) {
            if (key !in RESERVED_ENTITY_PROPERTY_KEYS) args += namedArg(key, value)
        }
        return indentText(indent) + "$macroName(${args.joinToString(", ")})"
    }

    private fun renderBoundary(
        boundary: Boundary,
        boundariesById: Map<BoundaryId, Boundary>,
        entitiesById: Map<EntityId, Entity>,
        indent: Int,
        out: StringBuilder,
    ) {
        val macroName = BOUNDARY_TYPE_TO_MACRO.getValue(boundary.type)
        out.appendLine(indentText(indent) + "$macroName(${boundary.id}, ${quote(boundary.name)}) {")
        for (child in boundary.children) {
            when (child) {
                is BoundaryChildId.OfEntity -> {
                    out.appendLine(renderEntity(entitiesById.getValue(child.id), indent + 1))
                }

                is BoundaryChildId.OfBoundary -> {
                    renderBoundary(boundariesById.getValue(child.id), boundariesById, entitiesById, indent + 1, out)
                }
            }
        }
        out.appendLine(indentText(indent) + "}")
    }

    private fun renderRelationship(relationship: Relationship): String {
        val direction = relationship.properties["direction"]
        val macroName =
            when {
                relationship.type == RelationshipType.BIDIRECTIONAL -> "BiRel"
                direction == "up" -> "Rel_U"
                direction == "down" -> "Rel_D"
                direction == "left" -> "Rel_L"
                direction == "right" -> "Rel_R"
                else -> "Rel"
            }

        val args = mutableListOf(relationship.sourceId.toString(), relationship.targetId.toString())
        relationship.description?.let { args += namedArg("descr", it) }
        relationship.technology?.let { args += namedArg("techn", it) }
        args += namedArg("id", relationship.id.toString())
        for ((key, value) in relationship.properties) {
            if (key !in RESERVED_RELATIONSHIP_PROPERTY_KEYS) args += namedArg(key, value)
        }

        return "$macroName(${args.joinToString(", ")})"
    }

    // Person/System have an `_Ext` macro variant for entity.external = true
    // (docs/adapters.md §15 "Supported Entities"); Container/ContainerDb/
    // Component don't (no Container_Ext etc. in the supported macro set), so
    // external is silently not representable for those types via this
    // adapter today — a documented MVP gap, not a bug, since the parser can
    // never produce that combination (EXTERNAL_ENTITY_MACROS in
    // PlantUmlC4Parser only sets external=true for Person_Ext/System_Ext).
    private fun macroNameFor(entity: Entity): String {
        if (entity.external) {
            EXTERNAL_ENTITY_TYPE_TO_MACRO[entity.type]?.let { return it }
        }
        return ENTITY_TYPE_TO_MACRO.getValue(entity.type)
    }

    private fun namedArg(
        name: String,
        value: String,
    ): String = "\$$name=${quote(value)}"

    private fun quote(value: String): String = "\"" + value.replace("\"", "\\\"") + "\""

    private fun indentText(level: Int): String = "    ".repeat(level)

    private val RESERVED_ENTITY_PROPERTY_KEYS = setOf("descr", "techn", "tags")
    private val RESERVED_RELATIONSHIP_PROPERTY_KEYS = setOf("direction", "id", "descr", "techn")

    private val ENTITY_TYPE_TO_MACRO: Map<EntityType, String> =
        mapOf(
            EntityType.PERSON to "Person",
            EntityType.SYSTEM to "System",
            EntityType.CONTAINER to "Container",
            EntityType.DATABASE to "ContainerDb",
            EntityType.COMPONENT to "Component",
        )

    private val EXTERNAL_ENTITY_TYPE_TO_MACRO: Map<EntityType, String> =
        mapOf(
            EntityType.PERSON to "Person_Ext",
            EntityType.SYSTEM to "System_Ext",
        )

    private val BOUNDARY_TYPE_TO_MACRO: Map<BoundaryType, String> =
        mapOf(
            BoundaryType.ENTERPRISE to "Enterprise_Boundary",
            BoundaryType.SYSTEM to "System_Boundary",
            BoundaryType.CONTAINER to "Container_Boundary",
        )
}
