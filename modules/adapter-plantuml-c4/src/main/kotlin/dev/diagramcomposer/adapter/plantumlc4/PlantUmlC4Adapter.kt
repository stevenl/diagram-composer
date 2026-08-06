package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.AdapterMetadata
import dev.diagramcomposer.adapterapi.DiagramAdapter
import dev.diagramcomposer.adapterapi.ParseResult
import dev.diagramcomposer.core.model.Diagram

/**
 * [DiagramAdapter] implementation for PlantUML C4 (docs/adapters.md §15) —
 * the project's first concrete language adapter.
 *
 * Parsing is implemented as of Milestone 3 ([PlantUmlC4Parser]). Generation
 * is Milestone 4 scope; [generate] throws [NotImplementedError] until then,
 * matching the plan in `docs/implementation-plan.md` Milestone 3 task 1.
 */
class PlantUmlC4Adapter : DiagramAdapter {
    override val metadata =
        AdapterMetadata(
            languageId = "plantuml-c4",
            displayName = "PlantUML C4",
            fileExtensions = listOf(".puml", ".plantuml"),
        )

    override fun parse(source: String): ParseResult = PlantUmlC4Parser.parse(source)

    override fun generate(diagram: Diagram): String = throw NotImplementedError("PlantUmlC4Adapter.generate is implemented in Milestone 4")
}
