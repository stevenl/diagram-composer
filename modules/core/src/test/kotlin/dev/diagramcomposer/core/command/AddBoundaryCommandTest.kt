package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.BoundaryType
import dev.diagramcomposer.core.model.Diagram
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AddBoundaryCommandTest {
    private val enterprise =
        Boundary(id = BoundaryId("enterprise"), name = "Enterprise", type = BoundaryType.ENTERPRISE)
    private val system =
        Boundary(id = BoundaryId("payments-system"), name = "Payments System", type = BoundaryType.SYSTEM)

    private fun diagramWithEnterpriseAtRoot() =
        Diagram(boundaries = listOf(enterprise), rootChildren = listOf(BoundaryChildId.OfBoundary(enterprise.id)))

    @Test
    fun `execute appends a root-level boundary`() {
        val diagram = diagramWithEnterpriseAtRoot()

        val result = AddBoundaryCommand(system).execute(diagram)

        assertEquals(listOf(enterprise, system), result.boundaries)
        assertEquals(
            listOf(BoundaryChildId.OfBoundary(enterprise.id), BoundaryChildId.OfBoundary(system.id)),
            result.rootChildren,
        )
    }

    @Test
    fun `execute nests a boundary inside another boundary`() {
        val diagram = diagramWithEnterpriseAtRoot()

        val result = AddBoundaryCommand(system, parent = BoundaryParent.InBoundary(enterprise.id)).execute(diagram)

        val updatedEnterprise = result.boundaries.first { it.id == enterprise.id }
        assertEquals(listOf(BoundaryChildId.OfBoundary(system.id)), updatedEnterprise.children)
        // Not also listed at the root:
        assertEquals(listOf(BoundaryChildId.OfBoundary(enterprise.id)), result.rootChildren)
    }

    @Test
    fun `execute rejects a non-empty boundary`() {
        val nonEmpty = system.copy(children = listOf(BoundaryChildId.OfBoundary(BoundaryId("some-child"))))

        assertThrows(IllegalArgumentException::class.java) { AddBoundaryCommand(nonEmpty) }
    }

    @Test
    fun `constructing with the boundary as its own parent is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AddBoundaryCommand(system, parent = BoundaryParent.InBoundary(system.id))
        }
    }

    @Test
    fun `execute rejects a boundary parent that does not exist`() {
        val command = AddBoundaryCommand(system, parent = BoundaryParent.InBoundary(BoundaryId("nonexistent")))

        assertThrows(IllegalArgumentException::class.java) { command.execute(Diagram()) }
    }

    @Test
    fun `undo reverses a root-level add`() {
        val diagram = diagramWithEnterpriseAtRoot()
        val command = AddBoundaryCommand(system)

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }

    @Test
    fun `undo reverses nesting a boundary inside another`() {
        val diagram = diagramWithEnterpriseAtRoot()
        val command = AddBoundaryCommand(system, parent = BoundaryParent.InBoundary(enterprise.id))

        val afterExecute = command.execute(diagram)
        val afterUndo = command.undo(afterExecute)

        assertEquals(diagram, afterUndo)
    }
}
