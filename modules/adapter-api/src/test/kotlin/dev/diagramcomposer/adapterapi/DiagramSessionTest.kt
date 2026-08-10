package dev.diagramcomposer.adapterapi

import dev.diagramcomposer.core.command.AddEntityCommand
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * End-to-end tests for [DiagramSession] covering the full
 * edit -> regenerate -> reparse loop without any UI involved
 * (docs/implementation-plan.md Milestone 6, "Definition of done").
 *
 * Uses [FakeAdapter] rather than `adapter-plantuml-c4`, matching
 * [DiagramAdapterContractTest]'s existing rationale: `adapter-api`'s own
 * tests must not depend on a concrete adapter module, which would invert
 * the intended dependency direction.
 */
class DiagramSessionTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.SYSTEM)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.SYSTEM)

    @Test
    fun `constructing from an in-memory diagram generates its source immediately`() {
        val session = DiagramSession(Diagram(entities = listOf(webApp)), FakeAdapter())

        assertEquals("ENTITY web Web App", session.sourceText)
        assertEquals(listOf(webApp), session.diagram.entities)
    }

    @Test
    fun `open parses source into a new session`() {
        val result = DiagramSession.open("ENTITY web Web App", FakeAdapter())

        val session = assertIsSuccess(result)
        assertEquals(listOf(webApp), session.diagram.entities)
        assertEquals("ENTITY web Web App", session.sourceText)
    }

    @Test
    fun `open surfaces parse errors instead of throwing`() {
        val result = DiagramSession.open(FakeAdapter.FAILING_SOURCE, FakeAdapter())

        val failure = result as? OpenResult.Failure ?: error("expected OpenResult.Failure, got $result")
        assertTrue(failure.errors.isNotEmpty())
    }

    @Test
    fun `execute applies a command and regenerates source from the result`() {
        val session = DiagramSession(Diagram(), FakeAdapter())

        val diagram = session.execute(AddEntityCommand(webApp))

        assertEquals(listOf(webApp), diagram.entities)
        assertEquals(listOf(webApp), session.diagram.entities)
        assertEquals("ENTITY web Web App", session.sourceText)
        assertTrue(session.canUndo)
    }

    @Test
    fun `a sequence of visual edits is reflected in generated source at each step`() {
        val session = DiagramSession(Diagram(), FakeAdapter())

        session.execute(AddEntityCommand(webApp))
        assertEquals("ENTITY web Web App", session.sourceText)

        session.execute(AddEntityCommand(paymentApi))
        assertEquals("ENTITY web Web App\nENTITY payment-api Payment API", session.sourceText)
    }

    @Test
    fun `undo reverses a command and regenerates source`() {
        val session = DiagramSession(Diagram(), FakeAdapter())
        session.execute(AddEntityCommand(webApp))
        session.execute(AddEntityCommand(paymentApi))

        session.undo()

        assertEquals(listOf(webApp), session.diagram.entities)
        assertEquals("ENTITY web Web App", session.sourceText)
        assertTrue(session.canRedo)
    }

    @Test
    fun `redo reapplies an undone command and regenerates source`() {
        val session = DiagramSession(Diagram(), FakeAdapter())
        session.execute(AddEntityCommand(webApp))
        session.undo()

        session.redo()

        assertEquals(listOf(webApp), session.diagram.entities)
        assertEquals("ENTITY web Web App", session.sourceText)
    }

    @Test
    fun `applyExternalEdit reparses valid source and replaces the diagram`() {
        val session = DiagramSession(Diagram(), FakeAdapter())
        session.execute(AddEntityCommand(webApp))

        val result = session.applyExternalEdit("ENTITY payment-api Payment API")

        val applied = result as? ApplyExternalEditResult.Applied ?: error("expected Applied, got $result")
        assertEquals(listOf(paymentApi), applied.diagram.entities)
        assertEquals(listOf(paymentApi), session.diagram.entities)
        assertEquals("ENTITY payment-api Payment API", session.sourceText)
    }

    @Test
    fun `applyExternalEdit clears undo and redo history`() {
        val session = DiagramSession(Diagram(), FakeAdapter())
        session.execute(AddEntityCommand(webApp))

        session.applyExternalEdit("ENTITY payment-api Payment API")

        assertFalse(session.canUndo)
        assertFalse(session.canRedo)
    }

    @Test
    fun `applyExternalEdit with unparsable source leaves the model and source unchanged`() {
        val session = DiagramSession(Diagram(), FakeAdapter())
        session.execute(AddEntityCommand(webApp))
        val diagramBefore = session.diagram
        val sourceBefore = session.sourceText

        val result = session.applyExternalEdit(FakeAdapter.FAILING_SOURCE)

        val rejected = result as? ApplyExternalEditResult.Rejected ?: error("expected Rejected, got $result")
        assertTrue(rejected.errors.isNotEmpty())
        assertEquals(diagramBefore, session.diagram)
        assertEquals(sourceBefore, session.sourceText)
        assertTrue(session.canUndo)
    }

    @Test
    fun `applyExternalEdit with a partially invalid source rejects instead of silently discarding lines`() {
        val session = DiagramSession(Diagram(), FakeAdapter())

        val result = session.applyExternalEdit("ENTITY web Web App\nnot a valid line")

        val rejected = result as? ApplyExternalEditResult.Rejected ?: error("expected Rejected, got $result")
        assertTrue(rejected.errors.any { it.message.contains("not a valid line") })
        assertEquals(Diagram(), session.diagram)
    }

    private fun assertIsSuccess(result: OpenResult): DiagramSession =
        (result as? OpenResult.Success ?: error("expected OpenResult.Success, got $result")).session
}
