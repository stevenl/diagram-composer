package dev.diagramcomposer.adapterapi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AdapterMetadataTest {
    @Test
    fun `constructs with valid fields`() {
        val metadata =
            AdapterMetadata(
                languageId = "plantuml-c4",
                displayName = "PlantUML C4",
                fileExtensions = listOf(".puml", ".plantuml"),
            )

        assertEquals("plantuml-c4", metadata.languageId)
        assertEquals("PlantUML C4", metadata.displayName)
        assertEquals(listOf(".puml", ".plantuml"), metadata.fileExtensions)
    }

    @Test
    fun `blank languageId is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AdapterMetadata(languageId = " ", displayName = "X", fileExtensions = listOf(".x"))
        }
    }

    @Test
    fun `blank displayName is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AdapterMetadata(languageId = "x", displayName = "", fileExtensions = listOf(".x"))
        }
    }

    @Test
    fun `empty fileExtensions is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AdapterMetadata(languageId = "x", displayName = "X", fileExtensions = emptyList())
        }
    }

    @Test
    fun `blank entry in fileExtensions is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AdapterMetadata(languageId = "x", displayName = "X", fileExtensions = listOf(".x", " "))
        }
    }
}
