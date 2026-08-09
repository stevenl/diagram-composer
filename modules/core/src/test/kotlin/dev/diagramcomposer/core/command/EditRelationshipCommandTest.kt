package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipId
import dev.diagramcomposer.core.model.RelationshipType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class EditRelationshipCommandTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)
    private val rel = Relationship(id = RelationshipId("rel-1"), sourceId = webApp.id, targetId = paymentApi.id)

    private fun baseDiagram() =
        Diagram(
            entities = listOf(webApp, paymentApi),
            relationships = listOf(rel),
            rootChildren = listOf(BoundaryChildId.OfEntity(webApp.id), BoundaryChildId.OfEntity(paymentApi.id)),
        )

    @Test
    fun `execute replaces the relationship in place`() {
        val edited = rel.copy(description = "Calls", type = RelationshipType.BIDIRECTIONAL)

        val result = EditRelationshipCommand(edited).execute(baseDiagram())

        assertEquals(listOf(edited), result.relationships)
    }

    @Test
    fun `execute rejects an id that does not match any existing relationship`() {
        val unknown = rel.copy(id = RelationshipId("nonexistent"))

        assertThrows(IllegalArgumentException::class.java) {
            EditRelationshipCommand(unknown).execute(baseDiagram())
        }
    }

    @Test
    fun `undo restores the previous relationship value`() {
        val diagram = baseDiagram()
        val edited = rel.copy(description = "Calls")
        val command = EditRelationshipCommand(edited)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }

    @Test
    fun `undo before execute throws`() {
        assertThrows(IllegalStateException::class.java) {
            EditRelationshipCommand(rel).undo(baseDiagram())
        }
    }
}
