package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.AdapterMetadata
import dev.diagramcomposer.adapterapi.DiagramAdapter
import dev.diagramcomposer.adapterapi.ParseResult
import dev.diagramcomposer.core.model.Diagram

/**
 * [DiagramAdapter] implementation for PlantUML C4 (docs/adapters.md §15) —
 * the project's first concrete language adapter.
 *
 * Parsing ([PlantUmlC4Parser], Milestone 3) and generation
 * ([PlantUmlC4Generator], Milestone 4) are both implemented.
 */
class PlantUmlC4Adapter : DiagramAdapter {
    override val metadata =
        AdapterMetadata(
            languageId = "plantuml-c4",
            displayName = "PlantUML C4",
            fileExtensions = listOf(".puml", ".plantuml"),
        )

    override fun parse(source: String): ParseResult = PlantUmlC4Parser.parse(source)

    override fun generate(diagram: Diagram): String = PlantUmlC4Generator.generate(diagram)
}
