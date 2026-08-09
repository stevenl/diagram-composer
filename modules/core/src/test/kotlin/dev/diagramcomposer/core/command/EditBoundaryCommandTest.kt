package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.BoundaryType
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class EditBoundaryCommandTest {
    private val system =
        Boundary(id = BoundaryId("payments-system"), name = "Payments System", type = BoundaryType.SYSTEM)

    private fun baseDiagram() =
        Diagram(
            boundaries = listOf(system),
            rootChildren = listOf(BoundaryChildId.OfBoundary(system.id)),
        )

    @Test
    fun `execute replaces the boundary in place`() {
        val renamed = system.copy(name = "Payments", type = BoundaryType.CONTAINER)

        val result = EditBoundaryCommand(renamed).execute(baseDiagram())

        assertEquals(listOf(renamed), result.boundaries)
    }

    @Test
    fun `execute allows reordering the boundary's own children`() {
        val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)
        val paymentDb = Entity(id = EntityId("payment-db"), name = "Payment DB", type = EntityType.DATABASE)
        val populated =
            system.copy(
                children = listOf(BoundaryChildId.OfEntity(paymentApi.id), BoundaryChildId.OfEntity(paymentDb.id)),
            )
        val diagram =
            Diagram(
                entities = listOf(paymentApi, paymentDb),
                boundaries = listOf(populated),
                rootChildren = listOf(BoundaryChildId.OfBoundary(populated.id)),
            )
        val reordered = populated.copy(children = populated.children.reversed())

        val result = EditBoundaryCommand(reordered).execute(diagram)

        assertEquals(populated.children.reversed(), result.boundaries.single().children)
    }

    @Test
    fun `execute rejects an id that does not match any existing boundary`() {
        val unknown = system.copy(id = BoundaryId("nonexistent"))

        assertThrows(IllegalArgumentException::class.java) {
            EditBoundaryCommand(unknown).execute(baseDiagram())
        }
    }

    @Test
    fun `undo restores the previous boundary value`() {
        val diagram = baseDiagram()
        val renamed = system.copy(name = "Payments")
        val command = EditBoundaryCommand(renamed)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }

    @Test
    fun `undo before execute throws`() {
        assertThrows(IllegalStateException::class.java) {
            EditBoundaryCommand(system).undo(baseDiagram())
        }
    }
}
