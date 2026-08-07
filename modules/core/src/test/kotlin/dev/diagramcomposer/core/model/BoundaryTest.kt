package dev.diagramcomposer.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class BoundaryTest {
    private fun sampleBoundary() =
        Boundary(
            id = BoundaryId("payments-system"),
            name = "Payments System",
            type = BoundaryType.SYSTEM,
            children = listOf(BoundaryChildId.OfEntity(EntityId("payment-api"))),
        )

    @Test
    fun `boundaries with the same field values are equal`() {
        assertEquals(sampleBoundary(), sampleBoundary())
    }

    @Test
    fun `copy changes only the specified field`() {
        val renamed = sampleBoundary().copy(name = "Payments")

        assertEquals("Payments", renamed.name)
        assertEquals(sampleBoundary().children, renamed.children)
    }

    @Test
    fun `children can reference nested boundaries`() {
        val boundary =
            sampleBoundary().copy(
                children = listOf(BoundaryChildId.OfBoundary(BoundaryId("checkout-container"))),
            )

        assertEquals(
            listOf(BoundaryChildId.OfBoundary(BoundaryId("checkout-container"))),
            boundary.children,
        )
    }

    @Test
    fun `blank name is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Boundary(id = BoundaryId("b1"), name = "", type = BoundaryType.SYSTEM)
        }
    }
}
