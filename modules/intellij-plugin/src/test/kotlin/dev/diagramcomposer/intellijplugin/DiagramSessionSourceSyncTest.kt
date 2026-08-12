package dev.diagramcomposer.intellijplugin

import dev.diagramcomposer.adapter.plantumlc4.PlantUmlC4Adapter
import dev.diagramcomposer.adapterapi.DiagramSession
import dev.diagramcomposer.core.command.AddEntityCommand
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.ui.state.SourceEditOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests [DiagramSessionSourceSync] against a real [DiagramSession]/
 * [PlantUmlC4Adapter] pair (docs/implementation-plan.md Milestone 10),
 * mirroring [dev.diagramcomposer.adapterapi.DiagramSessionTest]'s own
 * execute/undo/redo/applyExternalEdit coverage one layer up. Assertions on
 * generated text use `contains` rather than exact equality — the precise
 * PlantUML formatting is `PlantUmlC4Generator`'s concern, already covered
 * by its own tests; what matters here is that this class mirrors whatever
 * the session produces.
 */
class DiagramSessionSourceSyncTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.SYSTEM)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.SYSTEM)

    private fun newSync(): Pair<DiagramSession, DiagramSessionSourceSync> {
        val session = DiagramSession(Diagram(), PlantUmlC4Adapter())
        return session to DiagramSessionSourceSync(session)
    }

    @Test
    fun `execute applies the command to the session and returns its regenerated source`() {
        val (session, sync) = newSync()

        val text = sync.execute(AddEntityCommand(webApp))

        assertEquals(session.sourceText, text)
        assertTrue(text.contains("web"))
        assertEquals(listOf(webApp), session.diagram.entities)
    }

    @Test
    fun `undo reverses the session's most recent command`() {
        val (session, sync) = newSync()
        sync.execute(AddEntityCommand(webApp))
        sync.execute(AddEntityCommand(paymentApi))

        val text = sync.undo()

        assertEquals(session.sourceText, text)
        assertEquals(listOf(webApp), session.diagram.entities)
    }

    @Test
    fun `redo reapplies the session's most recently undone command`() {
        val (session, sync) = newSync()
        sync.execute(AddEntityCommand(webApp))
        sync.undo()

        val text = sync.redo()

        assertEquals(session.sourceText, text)
        assertEquals(listOf(webApp), session.diagram.entities)
    }

    @Test
    fun `applyExternalEdit reparses valid source and reports the new diagram`() {
        val (_, sync) = newSync()

        val outcome =
            sync.applyExternalEdit(
                """
                @startuml
                !include <C4/C4_Container>
                System(web, "Web App")
                @enduml
                """.trimIndent(),
            )

        val applied = outcome as? SourceEditOutcome.Applied ?: error("expected Applied, got $outcome")
        assertEquals(listOf(EntityId("web")), applied.diagram.entities.map { it.id })
    }

    @Test
    fun `applyExternalEdit with unparsable source reports errors without touching the session`() {
        val (session, sync) = newSync()
        sync.execute(AddEntityCommand(webApp))
        val diagramBefore = session.diagram

        val outcome = sync.applyExternalEdit("not valid plantuml c4 at all")

        val rejected = outcome as? SourceEditOutcome.Rejected ?: error("expected Rejected, got $outcome")
        assertTrue(rejected.errors.isNotEmpty())
        assertEquals(diagramBefore, session.diagram)
    }
}
