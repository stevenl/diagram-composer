package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseError
import dev.diagramcomposer.adapterapi.ParseResult
import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.BoundaryType
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipId
import dev.diagramcomposer.core.model.RelationshipType

/**
 * Parses PlantUML C4 source text into a [Diagram] (docs/adapters.md §15).
 *
 * ## Scope (Milestone 3)
 *
 * The parser recognises PlantUML C4 macro calls, one per source line:
 *
 * - Elements: `Person`, `Person_Ext`, `System`, `System_Ext`, `Container`,
 *   `ContainerDb`, `Component` (docs/adapters.md §15 "Supported Entities").
 * - Boundaries: `Enterprise_Boundary`, `System_Boundary`, `Container_Boundary`,
 *   opened with a trailing `{` and closed by a `}` line on its own,
 *   including nesting (docs/adapters.md §15 "Supported Boundaries").
 * - Relationships: `Rel`, `Rel_U`, `Rel_D`, `Rel_L`, `Rel_R`, `BiRel`
 *   (docs/adapters.md §15 "Supported Relationships").
 *
 * A macro call is assumed to occupy a single line — real-world C4-PlantUML
 * source is written this way in practice, and multi-line macro calls are
 * not handled in this milestone; this is a documented assumption, not a
 * spec requirement.
 *
 * ## Ignored lines
 *
 * Lines that are blank, PlantUML comments (`'...`), `@startuml`/`@enduml`,
 * preprocessor directives (`!include`, `!define`, ...), `title ...`, or
 * calls to known presentation-only macros (styling/legend macros such as
 * `LAYOUT_WITH_LEGEND()`, `HIDE_STEREOTYPES()`, `AddRelTag(...)`,
 * `UpdateElementStyle(...)`) are skipped without error. These constructs are
 * out of scope for the MVP (docs/adapters.md §15 "Unsupported Features") and
 * carry no information the core model represents, so skipping them (rather
 * than erroring) lets the parser handle realistic files. Full source
 * preservation for round-trip generation is a Milestone 4+ concern.
 *
 * Everything else must match one of the macros above, or parsing fails with
 * a [ParseError] pinpointing the offending line (docs/adapters.md §9 — the
 * parser "must never silently discard information", so unrecognised
 * constructs are reported, not dropped).
 */
internal object PlantUmlC4Parser {
    fun parse(source: String): ParseResult {
        val errors = mutableListOf<ParseError>()
        val entities = mutableListOf<Entity>()
        val relationships = mutableListOf<Relationship>()
        val completedBoundaries = mutableListOf<Boundary>()
        val rootChildren = mutableListOf<BoundaryChildId>()
        val boundaryStack = ArrayDeque<OpenBoundary>()
        var relationshipCounter = 0

        // Always returns a sink to record declaration order into: the innermost
        // open boundary's children, or rootChildren at the top level. This is
        // what lets top-level entities and boundaries round-trip in their
        // original relative order (see Diagram.rootChildren).
        fun currentChildren(): MutableList<BoundaryChildId> = boundaryStack.lastOrNull()?.children ?: rootChildren

        source.lineSequence().forEachIndexed { index, rawLine ->
            val lineNumber = index + 1
            val line = rawLine.trim()

            if (line.isEmpty() || isIgnorableLine(line)) return@forEachIndexed

            if (line == "}") {
                val closed = boundaryStack.removeLastOrNull()
                if (closed == null) {
                    errors += ParseError("unexpected '}' with no open boundary", line = lineNumber)
                } else {
                    val boundary =
                        Boundary(
                            id = BoundaryId(closed.id),
                            name = closed.name,
                            type = closed.type,
                            children = closed.children,
                        )
                    completedBoundaries += boundary
                    currentChildren().add(BoundaryChildId.OfBoundary(boundary.id))
                }
                return@forEachIndexed
            }

            val call = MACRO_CALL.matchEntire(line)
            if (call == null) {
                errors += ParseError("unrecognized syntax: '$line'", line = lineNumber)
                return@forEachIndexed
            }

            val macroName = call.groupValues[1]
            val argsSource = call.groupValues[2]
            val opensBoundary = call.groupValues[3] == "{"
            val args = PlantUmlArgs.parse(argsSource)

            try {
                when {
                    macroName in ENTITY_MACROS -> {
                        if (opensBoundary) {
                            errors += ParseError("'$macroName' cannot open a block with '{'", line = lineNumber)
                            return@forEachIndexed
                        }
                        val entity = parseEntity(macroName, args)
                        entities += entity
                        currentChildren().add(BoundaryChildId.OfEntity(entity.id))
                    }

                    macroName in BOUNDARY_MACROS -> {
                        if (!opensBoundary) {
                            errors +=
                                ParseError(
                                    "'$macroName' must open a block with a trailing '{'",
                                    line = lineNumber,
                                )
                            return@forEachIndexed
                        }
                        require(args.positional.size >= 2) {
                            "'$macroName' requires an alias and a label, got ${args.positional}"
                        }
                        boundaryStack.addLast(
                            OpenBoundary(
                                id = args.positional[0],
                                name = args.positional[1],
                                type = BOUNDARY_MACROS.getValue(macroName),
                            ),
                        )
                    }

                    macroName in RELATIONSHIP_MACROS -> {
                        if (opensBoundary) {
                            errors += ParseError("'$macroName' cannot open a block with '{'", line = lineNumber)
                            return@forEachIndexed
                        }
                        relationshipCounter++
                        val relationship = parseRelationship(macroName, args, relationshipCounter)
                        relationships += relationship
                    }

                    else -> {
                        errors += ParseError("unrecognized macro '$macroName'", line = lineNumber)
                    }
                }
            } catch (e: IllegalArgumentException) {
                errors += ParseError(e.message ?: "invalid syntax on line: '$line'", line = lineNumber)
            }
        }

        for (open in boundaryStack) {
            errors += ParseError("boundary '${open.id}' was never closed with '}'")
        }

        if (errors.isNotEmpty()) {
            return ParseResult.Failure(errors)
        }

        return try {
            ParseResult.Success(
                Diagram(
                    entities = entities,
                    relationships = relationships,
                    boundaries = completedBoundaries,
                    rootChildren = rootChildren,
                ),
            )
        } catch (e: IllegalArgumentException) {
            ParseResult.Failure(listOf(ParseError(e.message ?: "invalid diagram")))
        }
    }

    private fun parseEntity(
        macroName: String,
        args: PlantUmlArgs,
    ): Entity {
        require(args.positional.size >= 2) {
            "'$macroName' requires an alias and a label, got ${args.positional}"
        }
        val alias = args.positional[0]
        val label = args.positional[1]
        // Container/ContainerDb/Component take an optional technology as their
        // 3rd positional argument, then an optional description as their 4th.
        // Person/System/*_Ext take only an optional description as their 3rd.
        val (technology, description) =
            if (macroName in TECHNOLOGY_BEARING_MACROS) {
                args.positional.getOrNull(2) to args.positional.getOrNull(3)
            } else {
                null to args.positional.getOrNull(2)
            }

        return Entity(
            id = EntityId(alias),
            name = label,
            type = ENTITY_MACROS.getValue(macroName),
            description = args.named["descr"] ?: description,
            technology = args.named["techn"] ?: technology,
            tags = args.named["tags"]?.splitTags() ?: emptyList(),
            properties = args.named.filterKeys { name -> name !in setOf("descr", "techn", "tags", "id") },
            external = macroName in EXTERNAL_ENTITY_MACROS,
        )
    }

    private fun parseRelationship(
        macroName: String,
        args: PlantUmlArgs,
        index: Int,
    ): Relationship {
        require(args.positional.size >= 2) {
            "'$macroName' requires a source and target entity, got ${args.positional}"
        }
        val sourceId = args.positional[0]
        val targetId = args.positional[1]
        val description = args.named["descr"] ?: args.positional.getOrNull(2)
        val technology = args.named["techn"] ?: args.positional.getOrNull(3)
        val direction = RELATIONSHIP_MACROS.getValue(macroName)

        val properties =
            buildMap {
                if (direction != null) put("direction", direction)
                for ((name, value) in args.named) {
                    if (name !in setOf("descr", "techn", "id")) put(name, value)
                }
            }

        return Relationship(
            // PlantUML relationships have no natural id/alias slot. `$id=` is this
            // adapter's own extension (emitted by PlantUmlC4Generator) so that a
            // generate()-then-parse() round-trip recovers the same RelationshipId
            // rather than renumbering; hand-written source without `$id` falls
            // back to a sequential id, as before.
            id = args.named["id"]?.let { RelationshipId(it) } ?: RelationshipId("rel-$index"),
            sourceId = EntityId(sourceId),
            targetId = EntityId(targetId),
            description = description,
            technology = technology,
            type =
                if (macroName in
                    BIDIRECTIONAL_RELATIONSHIP_MACROS
                ) {
                    RelationshipType.BIDIRECTIONAL
                } else {
                    RelationshipType.DEFAULT
                },
            properties = properties,
        )
    }

    private fun isIgnorableLine(line: String): Boolean {
        if (line.startsWith("'")) return true
        if (line.startsWith("@")) return true
        if (line.startsWith("!")) return true
        if (line.startsWith("title", ignoreCase = true) &&
            (line.length == 5 || line[5].isWhitespace())
        ) {
            return true
        }

        val call = MACRO_CALL.matchEntire(line) ?: return false
        val macroName = call.groupValues[1]
        return IGNORABLE_MACRO_PREFIXES.any { macroName.startsWith(it) }
    }

    private data class OpenBoundary(
        val id: String,
        val name: String,
        val type: BoundaryType,
        val children: MutableList<BoundaryChildId> = mutableListOf(),
    )

    // Matches a macro call: `Name(args)` or `Name(args) {` (trailing block opener).
    private val MACRO_CALL = Regex("^([A-Za-z_][A-Za-z0-9_]*)\\s*\\((.*)\\)\\s*(\\{)?\\s*$")

    private val ENTITY_MACROS: Map<String, EntityType> =
        mapOf(
            "Person" to EntityType.PERSON,
            "Person_Ext" to EntityType.PERSON,
            "System" to EntityType.SYSTEM,
            "System_Ext" to EntityType.SYSTEM,
            "Container" to EntityType.CONTAINER,
            "ContainerDb" to EntityType.DATABASE,
            "Component" to EntityType.COMPONENT,
        )

    private val TECHNOLOGY_BEARING_MACROS = setOf("Container", "ContainerDb", "Component")

    // Macros that set Entity.external = true (docs/adapters.md §15 "Supported
    // Properties" — "External/Internal"). Both map to the same EntityType as
    // their non-Ext counterpart in ENTITY_MACROS above; external-ness is a
    // property of the entity, not a different kind of entity.
    private val EXTERNAL_ENTITY_MACROS = setOf("Person_Ext", "System_Ext")

    private val BOUNDARY_MACROS: Map<String, BoundaryType> =
        mapOf(
            "Enterprise_Boundary" to BoundaryType.ENTERPRISE,
            "System_Boundary" to BoundaryType.SYSTEM,
            "Container_Boundary" to BoundaryType.CONTAINER,
        )

    // Value is the direction stashed into Relationship.properties["direction"], or null for Rel/BiRel.
    private val RELATIONSHIP_MACROS: Map<String, String?> =
        mapOf(
            "Rel" to null,
            "Rel_U" to "up",
            "Rel_D" to "down",
            "Rel_L" to "left",
            "Rel_R" to "right",
            "BiRel" to null,
        )

    private val BIDIRECTIONAL_RELATIONSHIP_MACROS = setOf("BiRel")

    private val IGNORABLE_MACRO_PREFIXES = listOf("LAYOUT_", "HIDE_", "SHOW_", "Update", "Add", "Set")
}
