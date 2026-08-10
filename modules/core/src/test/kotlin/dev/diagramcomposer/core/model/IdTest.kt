package dev.diagramcomposer.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Covers [EntityId], [RelationshipId], and [BoundaryId]: construction,
 * equality, and blank-value rejection. They share identical behaviour by
 * design (each is a thin wrapper distinguishing one id namespace from
 * another at compile time), so one test class exercises all three rather
 * than duplicating the same cases per type.
 */
class IdTest {
    @Test
    fun `equal ids with the same value are equal`() {
        assertEquals(EntityId("payment-api"), EntityId("payment-api"))
        assertEquals(RelationshipId("rel-1"), RelationshipId("rel-1"))
        assertEquals(BoundaryId("boundary-1"), BoundaryId("boundary-1"))
    }

    @Test
    fun `toString returns the underlying value`() {
        assertEquals("payment-api", EntityId("payment-api").toString())
    }

    @Test
    fun `blank EntityId is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { EntityId(" ") }
    }

    @Test
    fun `blank RelationshipId is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { RelationshipId("") }
    }

    @Test
    fun `blank BoundaryId is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { BoundaryId("") }
    }
}
