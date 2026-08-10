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

class AddRelationshipCommandTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)
    private val rel = Relationship(id = RelationshipId("rel-1"), sourceId = webApp.id, targetId = paymentApi.id)

    private fun baseDiagram() =
        Diagram(
            entities = listOf(webApp, paymentApi),
            rootChildren = listOf(BoundaryChildId.OfEntity(webApp.id), BoundaryChildId.OfEntity(paymentApi.id)),
        )

    @Test
    fun `execute appends a relationship`() {
        val diagram = baseDiagram()

        val result = AddRelationshipCommand(rel).execute(diagram)

        assertEquals(listOf(rel), result.relationships)
    }

    @Test
    fun `execute inserts a relationship at the given index`() {
        val existing = rel.copy(id = RelationshipId("rel-existing"))
        val diagram = baseDiagram().copy(relationships = listOf(existing))

        val result = AddRelationshipCommand(rel, index = 0).execute(diagram)

        assertEquals(listOf(rel, existing), result.relationships)
    }

    @Test
    fun `execute rejects a relationship referencing a missing entity`() {
        val diagram = Diagram()

        assertThrows(IllegalArgumentException::class.java) {
            AddRelationshipCommand(rel).execute(diagram)
        }
    }

    @Test
    fun `undo reverses an add`() {
        val diagram = baseDiagram()
        val command = AddRelationshipCommand(rel)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }
}
