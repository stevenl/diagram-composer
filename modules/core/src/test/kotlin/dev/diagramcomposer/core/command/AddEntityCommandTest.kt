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

class AddEntityCommandTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)
    private val boundary =
        Boundary(id = BoundaryId("payments-system"), name = "Payments System", type = BoundaryType.SYSTEM)

    private fun diagramWithBoundaryAtRoot() =
        Diagram(boundaries = listOf(boundary), rootChildren = listOf(BoundaryChildId.OfBoundary(boundary.id)))

    @Test
    fun `execute appends a root-level entity to entities and rootChildren`() {
        val diagram = Diagram(entities = listOf(webApp), rootChildren = listOf(BoundaryChildId.OfEntity(webApp.id)))

        val result = AddEntityCommand(paymentApi).execute(diagram)

        assertEquals(listOf(webApp, paymentApi), result.entities)
        assertEquals(
            listOf(BoundaryChildId.OfEntity(webApp.id), BoundaryChildId.OfEntity(paymentApi.id)),
            result.rootChildren,
        )
    }

    @Test
    fun `execute inserts a root-level entity at the given index`() {
        val diagram = Diagram(entities = listOf(webApp), rootChildren = listOf(BoundaryChildId.OfEntity(webApp.id)))

        val result = AddEntityCommand(paymentApi, index = 0).execute(diagram)

        assertEquals(
            listOf(BoundaryChildId.OfEntity(paymentApi.id), BoundaryChildId.OfEntity(webApp.id)),
            result.rootChildren,
        )
    }

    @Test
    fun `execute adds an entity into a specific boundary`() {
        val diagram = diagramWithBoundaryAtRoot()

        val result = AddEntityCommand(paymentApi, parent = BoundaryParent.InBoundary(boundary.id)).execute(diagram)

        assertEquals(listOf(paymentApi), result.entities)
        assertEquals(listOf(BoundaryChildId.OfEntity(paymentApi.id)), result.boundaries.single().children)
        // Not also listed at the root:
        assertEquals(listOf(BoundaryChildId.OfBoundary(boundary.id)), result.rootChildren)
    }

    @Test
    fun `execute rejects a boundary parent that does not exist`() {
        val command = AddEntityCommand(paymentApi, parent = BoundaryParent.InBoundary(BoundaryId("nonexistent")))

        assertThrows(IllegalArgumentException::class.java) { command.execute(Diagram()) }
    }

    @Test
    fun `undo reverses a root-level add`() {
        val diagram = Diagram(entities = listOf(webApp), rootChildren = listOf(BoundaryChildId.OfEntity(webApp.id)))
        val command = AddEntityCommand(paymentApi)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }

    @Test
    fun `undo reverses an add into a boundary`() {
        val diagram = diagramWithBoundaryAtRoot()
        val command = AddEntityCommand(paymentApi, parent = BoundaryParent.InBoundary(boundary.id))

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }
}
