package dev.diagramcomposer.adapterapi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ParseErrorTest {
    @Test
    fun `constructs with just a message`() {
        val error = ParseError("unexpected token")

        assertEquals("unexpected token", error.message)
        assertNull(error.line)
        assertNull(error.column)
    }

    @Test
    fun `constructs with message, line, and column`() {
        val error = ParseError("unexpected token", line = 3, column = 7)

        assertEquals(3, error.line)
        assertEquals(7, error.column)
    }

    @Test
    fun `blank message is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { ParseError(" ") }
    }

    @Test
    fun `line below 1 is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { ParseError("x", line = 0) }
    }

    @Test
    fun `column below 1 is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { ParseError("x", column = 0) }
    }
}
