package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class EditEntityCommandTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)

    private fun baseDiagram() =
        Diagram(
            entities = listOf(webApp, paymentApi),
            rootChildren = listOf(BoundaryChildId.OfEntity(webApp.id), BoundaryChildId.OfEntity(paymentApi.id)),
        )

    @Test
    fun `execute replaces the entity in place, preserving list position`() {
        val renamed = paymentApi.copy(name = "Payments API")

        val result = EditEntityCommand(renamed).execute(baseDiagram())

        assertEquals(listOf(webApp, renamed), result.entities)
    }

    @Test
    fun `execute can change type, description, and other properties`() {
        val edited = paymentApi.copy(type = EntityType.COMPONENT, description = "Handles payments", external = true)

        val result = EditEntityCommand(edited).execute(baseDiagram())

        assertEquals(edited, result.entities.last())
    }

    @Test
    fun `execute rejects an id that does not match any existing entity`() {
        val unknown = Entity(id = EntityId("nonexistent"), name = "Ghost", type = EntityType.SYSTEM)

        assertThrows(IllegalArgumentException::class.java) {
            EditEntityCommand(unknown).execute(baseDiagram())
        }
    }

    @Test
    fun `undo restores the previous entity value`() {
        val diagram = baseDiagram()
        val edited = paymentApi.copy(name = "Payments API", type = EntityType.COMPONENT)
        val command = EditEntityCommand(edited)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }

    @Test
    fun `undo before execute throws`() {
        assertThrows(IllegalStateException::class.java) {
            EditEntityCommand(paymentApi).undo(baseDiagram())
        }
    }
}
