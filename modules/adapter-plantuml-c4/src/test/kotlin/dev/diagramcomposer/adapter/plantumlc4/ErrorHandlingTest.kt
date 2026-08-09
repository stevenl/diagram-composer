package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Malformed/unrecognized input handling
 * (docs/adapters.md §9 — the parser "must never silently discard
 * information" and must "report validation issues").
 */
class ErrorHandlingTest {
    private val adapter = PlantUmlC4Adapter()

    @Test
    fun `empty source parses to an empty diagram`() {
        val result = adapter.parse("")

        assertTrue(result is ParseResult.Success)
        val diagram = (result as ParseResult.Success).diagram
        assertTrue(diagram.entities.isEmpty() && diagram.relationships.isEmpty() && diagram.boundaries.isEmpty())
    }

    @Test
    fun `an entirely unknown macro fails with a location`() {
        val result = adapter.parse("MysteryMacro(a, b)")

        assertTrue(result is ParseResult.Failure)
        val error = (result as ParseResult.Failure).errors.single()
        assertTrue(error.message.contains("MysteryMacro"))
        assertEquals(1, error.line)
    }

    @Test
    fun `free text that isn't a macro call fails with a location`() {
        val result = adapter.parse("this is not plantuml at all")

        assertTrue(result is ParseResult.Failure)
        assertEquals(1, (result as ParseResult.Failure).errors.single().line)
    }

    @Test
    fun `errors from multiple lines are all reported, not just the first`() {
        val result =
            adapter.parse(
                """
                Person(customer, "Customer")
                NotARealMacro(a, b)
                System(banking)
                """.trimIndent(),
            )

        assertTrue(result is ParseResult.Failure)
        val errors = (result as ParseResult.Failure).errors
        assertEquals(listOf(2, 3), errors.map { it.line })
    }

    @Test
    fun `duplicate entity ids fail to parse`() {
        val result =
            adapter.parse(
                """
                Person(customer, "Customer")
                Person(customer, "Another Customer")
                """.trimIndent(),
            )

        assertTrue(result is ParseResult.Failure)
    }

    @Test
    fun `known presentation-only macros are ignored without error`() {
        val result =
            adapter.parse(
                """
                LAYOUT_WITH_LEGEND()
                HIDE_STEREOTYPES()
                Person(customer, "Customer")
                AddRelTag("async", ${'$'}lineStyle = "DashedLine()")
                """.trimIndent(),
            )

        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).diagram.entities.size)
    }

    @Test
    fun `startuml, enduml, includes, and titles are ignored without error`() {
        val result =
            adapter.parse(
                """
                @startuml
                !include <C4/C4_Container>
                title Internet Banking System
                Person(customer, "Customer")
                @enduml
                """.trimIndent(),
            )

        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).diagram.entities.size)
    }

    @Test
    fun `comment lines are ignored without error`() {
        val result =
            adapter.parse(
                """
                ' this whole diagram describes the internet banking system
                Person(customer, "Customer")
                """.trimIndent(),
            )

        assertTrue(result is ParseResult.Success)
    }
}
