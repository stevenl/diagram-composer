package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.BoundaryType
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RemoveEntityCommandTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)
    private val paymentDb = Entity(id = EntityId("payment-db"), name = "Payment DB", type = EntityType.DATABASE)

    @Test
    fun `execute removes a root-level entity`() {
        val diagram =
            Diagram(
                entities = listOf(webApp, paymentApi),
                rootChildren = listOf(BoundaryChildId.OfEntity(webApp.id), BoundaryChildId.OfEntity(paymentApi.id)),
            )

        val result = RemoveEntityCommand(paymentApi.id).execute(diagram)

        assertEquals(listOf(webApp), result.entities)
        assertEquals(listOf(BoundaryChildId.OfEntity(webApp.id)), result.rootChildren)
    }

    @Test
    fun `execute removes an entity from within a boundary`() {
        val boundary =
            Boundary(
                id = BoundaryId("payments-system"),
                name = "Payments System",
                type = BoundaryType.SYSTEM,
                children = listOf(BoundaryChildId.OfEntity(paymentApi.id)),
            )
        val diagram =
            Diagram(
                entities = listOf(paymentApi),
                boundaries = listOf(boundary),
                rootChildren = listOf(BoundaryChildId.OfBoundary(boundary.id)),
            )

        val result = RemoveEntityCommand(paymentApi.id).execute(diagram)

        assertEquals(emptyList<Entity>(), result.entities)
        assertEquals(emptyList<BoundaryChildId>(), result.boundaries.single().children)
    }

    @Test
    fun `execute cascades to remove relationships referencing the entity`() {
        val relOut = Relationship(id = RelationshipId("rel-out"), sourceId = paymentApi.id, targetId = paymentDb.id)
        val relIn = Relationship(id = RelationshipId("rel-in"), sourceId = webApp.id, targetId = paymentApi.id)
        val relUnrelated = Relationship(id = RelationshipId("rel-other"), sourceId = webApp.id, targetId = paymentDb.id)
        val diagram =
            Diagram(
                entities = listOf(webApp, paymentApi, paymentDb),
                relationships = listOf(relOut, relIn, relUnrelated),
                rootChildren =
                    listOf(
                        BoundaryChildId.OfEntity(webApp.id),
                        BoundaryChildId.OfEntity(paymentApi.id),
                        BoundaryChildId.OfEntity(paymentDb.id),
                    ),
            )

        val result = RemoveEntityCommand(paymentApi.id).execute(diagram)

        assertEquals(listOf(relUnrelated), result.relationships)
    }

    @Test
    fun `execute rejects an unknown entity id`() {
        assertThrows(IllegalArgumentException::class.java) {
            RemoveEntityCommand(EntityId("nonexistent")).execute(Diagram())
        }
    }

    @Test
    fun `undo restores a root-level entity to its original position`() {
        val diagram =
            Diagram(
                entities = listOf(webApp, paymentApi, paymentDb),
                rootChildren =
                    listOf(
                        BoundaryChildId.OfEntity(webApp.id),
                        BoundaryChildId.OfEntity(paymentApi.id),
                        BoundaryChildId.OfEntity(paymentDb.id),
                    ),
            )
        val command = RemoveEntityCommand(paymentApi.id)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }

    @Test
    fun `undo restores an entity removed from within a boundary`() {
        val boundary =
            Boundary(
                id = BoundaryId("payments-system"),
                name = "Payments System",
                type = BoundaryType.SYSTEM,
                children = listOf(BoundaryChildId.OfEntity(paymentApi.id), BoundaryChildId.OfEntity(paymentDb.id)),
            )
        val diagram =
            Diagram(
                entities = listOf(paymentApi, paymentDb),
                boundaries = listOf(boundary),
                rootChildren = listOf(BoundaryChildId.OfBoundary(boundary.id)),
            )
        val command = RemoveEntityCommand(paymentApi.id)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }

    @Test
    fun `undo restores cascaded relationships at their original positions`() {
        val relA = Relationship(id = RelationshipId("rel-a"), sourceId = webApp.id, targetId = paymentApi.id)
        val relB = Relationship(id = RelationshipId("rel-b"), sourceId = webApp.id, targetId = paymentDb.id)
        val relC = Relationship(id = RelationshipId("rel-c"), sourceId = paymentApi.id, targetId = paymentDb.id)
        val diagram =
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
        val command = RemoveEntityCommand(paymentApi.id)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
        assertEquals(listOf(relA, relB, relC), afterUndo.relationships)
    }

    @Test
    fun `undo before execute throws`() {
        assertThrows(IllegalStateException::class.java) {
            RemoveEntityCommand(paymentApi.id).undo(Diagram())
        }
    }
}
