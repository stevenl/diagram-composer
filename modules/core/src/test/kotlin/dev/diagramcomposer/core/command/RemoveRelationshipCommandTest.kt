package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RemoveRelationshipCommandTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)
    private val paymentDb = Entity(id = EntityId("payment-db"), name = "Payment DB", type = EntityType.DATABASE)

    private val relA = Relationship(id = RelationshipId("rel-a"), sourceId = webApp.id, targetId = paymentApi.id)
    private val relB = Relationship(id = RelationshipId("rel-b"), sourceId = webApp.id, targetId = paymentDb.id)
    private val relC = Relationship(id = RelationshipId("rel-c"), sourceId = paymentApi.id, targetId = paymentDb.id)

    private fun baseDiagram() =
        Diagram(
            entities = listOf(webApp, paymentApi, paymentDb),
            relationships = listOf(relA, relB, relC),
            rootChildren =
                listOf(
                    BoundaryChildId.OfEntity(webApp.id),
                    BoundaryChildId.OfEntity(paymentApi.id),
                    BoundaryChildId.OfEntity(paymentDb.id),
                ),
        )

    @Test
    fun `execute removes the relationship`() {
        val result = RemoveRelationshipCommand(relB.id).execute(baseDiagram())

        assertEquals(listOf(relA, relC), result.relationships)
    }

    @Test
    fun `execute rejects an unknown relationship id`() {
        assertThrows(IllegalArgumentException::class.java) {
            RemoveRelationshipCommand(RelationshipId("nonexistent")).execute(baseDiagram())
        }
    }

    @Test
    fun `undo restores the relationship at its original position`() {
        val diagram = baseDiagram()
        val command = RemoveRelationshipCommand(relB.id)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
        assertEquals(listOf(relA, relB, relC), afterUndo.relationships)
    }

    @Test
    fun `undo before execute throws`() {
        assertThrows(IllegalStateException::class.java) {
            RemoveRelationshipCommand(relB.id).undo(baseDiagram())
        }
    }
}
