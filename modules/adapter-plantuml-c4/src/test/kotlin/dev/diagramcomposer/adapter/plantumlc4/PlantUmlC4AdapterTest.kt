package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.core.model.Diagram
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
    fun `generate is not yet implemented (Milestone 4)`() {
        assertThrows(NotImplementedError::class.java) {
            adapter.generate(Diagram())
        }
    }
}
