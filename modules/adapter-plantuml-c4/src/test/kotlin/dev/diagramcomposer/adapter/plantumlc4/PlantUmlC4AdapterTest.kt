package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseResult
import dev.diagramcomposer.core.model.Diagram
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlantUmlC4AdapterTest {
    private val adapter = PlantUmlC4Adapter()

    @Test
    fun `metadata identifies the language and supported file extensions`() {
        assertEquals("plantuml-c4", adapter.metadata.languageId)
        assertEquals("PlantUML C4", adapter.metadata.displayName)
        assertEquals(listOf(".puml", ".plantuml"), adapter.metadata.fileExtensions)
    }

    @Test
    fun `generating an empty diagram produces source that reparses to an empty diagram`() {
        val source = adapter.generate(Diagram())

        val result = adapter.parse(source)
        assertTrue(result is ParseResult.Success)
        assertEquals(Diagram(), (result as ParseResult.Success).diagram)
    }
}
