package dev.diagramcomposer.adapterapi

import dev.diagramcomposer.core.model.Diagram
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ParseResultTest {
    @Test
    fun `Success wraps a Diagram`() {
        val diagram = Diagram()

        val result = ParseResult.Success(diagram)

        assertEquals(diagram, result.diagram)
    }

    @Test
    fun `Failure wraps one or more ParseErrors`() {
        val errors = listOf(ParseError("bad syntax", line = 1))

        val result = ParseResult.Failure(errors)

        assertEquals(errors, result.errors)
    }

    @Test
    fun `Failure with no errors is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { ParseResult.Failure(emptyList()) }
    }
}
