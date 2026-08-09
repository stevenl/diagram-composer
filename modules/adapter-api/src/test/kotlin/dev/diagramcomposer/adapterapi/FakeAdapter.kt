package dev.diagramcomposer.adapterapi

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType

/**
 * A minimal, in-memory [DiagramAdapter] test double.
 *
 * FakeAdapter parses/generates a trivial line-based format of its own
 * devising ("ENTITY <id> <name>" per line) rather than real PlantUML C4
 * syntax. This lets [DiagramAdapterContractTest] exercise the
 * [DiagramAdapter] contract without depending on `adapter-plantuml-c4` —
 * which doesn't exist yet at this milestone, and which would invert the
 * intended module dependency direction (adapters depend on adapter-api, not
 * the reverse) even once it does exist
 * (docs/implementation-plan.md Milestone 2 task 5).
 */
class FakeAdapter : DiagramAdapter {
    override val metadata =
        AdapterMetadata(
            languageId = "fake",
            displayName = "Fake Adapter",
            fileExtensions = listOf(".fake"),
        )

    override fun parse(source: String): ParseResult {
        if (source == FAILING_SOURCE) {
            return ParseResult.Failure(listOf(ParseError("simulated parse failure", line = 1)))
        }

        val entities = mutableListOf<Entity>()
        val errors = mutableListOf<ParseError>()

        source
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEachIndexed { index, line ->
                val parts = line.removePrefix("ENTITY ").trim().split(" ", limit = 2)
                if (line.startsWith("ENTITY ") && parts.size == 2) {
                    entities += Entity(id = EntityId(parts[0]), name = parts[1], type = EntityType.SYSTEM)
                } else {
                    errors += ParseError("unrecognized line: '$line'", line = index + 1)
                }
            }

        return if (errors.isEmpty()) {
            ParseResult.Success(Diagram(entities = entities))
        } else {
            ParseResult.Failure(errors)
        }
    }

    override fun generate(diagram: Diagram): String =
        diagram.entities.joinToString(separator = "\n") {
            "ENTITY ${it.id} ${it.name}"
        }

    companion object {
        /** Passed to [parse] to force a [ParseResult.Failure], for tests. */
        const val FAILING_SOURCE = "FAIL"
    }
}
