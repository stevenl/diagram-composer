package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommandHistoryTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)

    @Test
    fun `a fresh history cannot undo or redo`() {
        val history = CommandHistory(Diagram())

        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun `execute applies the command and becomes undoable`() {
        val history = CommandHistory(Diagram())

        val result = history.execute(AddEntityCommand(webApp))

        assertEquals(listOf(webApp), result.entities)
        assertEquals(listOf(webApp), history.diagram.entities)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun `undo reverses the last command and becomes redoable`() {
        val history = CommandHistory(Diagram())
        history.execute(AddEntityCommand(webApp))

        val result = history.undo()

        assertEquals(Diagram(), result)
        assertFalse(history.canUndo)
        assertTrue(history.canRedo)
    }

    @Test
    fun `redo reapplies the undone command`() {
        val history = CommandHistory(Diagram())
        history.execute(AddEntityCommand(webApp))
        history.undo()

        val result = history.redo()

        assertEquals(listOf(webApp), result.entities)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun `multiple undo calls step back through history in reverse order`() {
        val history = CommandHistory(Diagram())
        history.execute(AddEntityCommand(webApp))
        history.execute(AddEntityCommand(paymentApi))

        assertEquals(listOf(webApp), history.undo().entities)
        assertEquals(emptyList<Entity>(), history.undo().entities)
        assertFalse(history.canUndo)
    }

    @Test
    fun `executing a new command after undo clears the redo stack`() {
        val history = CommandHistory(Diagram())
        history.execute(AddEntityCommand(webApp))
        history.undo()

        history.execute(AddEntityCommand(paymentApi))

        assertFalse(history.canRedo)
        assertEquals(listOf(paymentApi), history.diagram.entities)
    }

    @Test
    fun `undo with an empty undo stack throws`() {
        assertThrows(IllegalStateException::class.java) { CommandHistory(Diagram()).undo() }
    }

    @Test
    fun `redo with an empty redo stack throws`() {
        assertThrows(IllegalStateException::class.java) { CommandHistory(Diagram()).redo() }
    }
}
