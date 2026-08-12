package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.command.AddEntityCommand
import dev.diagramcomposer.core.command.RemoveEntityCommand
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiagramViewModelTest {
    private val web = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val api = Entity(id = EntityId("api"), name = "API", type = EntityType.CONTAINER)

    @Test
    fun `exposes the diagram it was constructed with`() {
        val diagram = Diagram(entities = listOf(web))
        val viewModel = DiagramViewModel(diagram)

        assertEquals(diagram, viewModel.diagram)
        assertEquals(listOf(ElementTreeNode.EntityNode(web)), viewModel.elementTree)
        assertEquals(emptyList<RelationshipRow>(), viewModel.relationshipRows)
        assertFalse(viewModel.canUndo)
        assertFalse(viewModel.canRedo)
    }

    @Test
    fun `execute applies a command and updates the diagram and derived views`() {
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)))

        viewModel.execute(AddEntityCommand(api))

        assertEquals(Diagram(entities = listOf(web, api)), viewModel.diagram)
        assertEquals(
            listOf(ElementTreeNode.EntityNode(web), ElementTreeNode.EntityNode(api)),
            viewModel.elementTree,
        )
        assertTrue(viewModel.canUndo)
        assertFalse(viewModel.canRedo)
    }

    @Test
    fun `undo reverses the most recent command and enables redo`() {
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)))
        viewModel.execute(AddEntityCommand(api))

        viewModel.undo()

        assertEquals(Diagram(entities = listOf(web)), viewModel.diagram)
        assertFalse(viewModel.canUndo)
        assertTrue(viewModel.canRedo)
    }

    @Test
    fun `redo re-applies the most recently undone command`() {
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)))
        viewModel.execute(AddEntityCommand(api))
        viewModel.undo()

        viewModel.redo()

        assertEquals(Diagram(entities = listOf(web, api)), viewModel.diagram)
        assertTrue(viewModel.canUndo)
        assertFalse(viewModel.canRedo)
    }

    @Test
    fun `a fresh command after undo clears the redo stack`() {
        val extra = Entity(id = EntityId("db"), name = "Database", type = EntityType.DATABASE)
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)))
        viewModel.execute(AddEntityCommand(api))
        viewModel.undo()

        viewModel.execute(AddEntityCommand(extra))

        assertEquals(Diagram(entities = listOf(web, extra)), viewModel.diagram)
        assertFalse(viewModel.canRedo)
    }

    @Test
    fun `remove command dispatched through execute updates the diagram`() {
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web, api)))

        viewModel.execute(RemoveEntityCommand(api.id))

        assertEquals(Diagram(entities = listOf(web)), viewModel.diagram)
        assertEquals(listOf(ElementTreeNode.EntityNode(web)), viewModel.elementTree)
    }

    @Test
    fun `refresh replaces the displayed diagram, its derived views, and clears undo-redo`() {
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)))
        viewModel.execute(AddEntityCommand(api))

        viewModel.refresh(Diagram(entities = listOf(web, api)))

        assertEquals(Diagram(entities = listOf(web, api)), viewModel.diagram)
        assertEquals(
            listOf(ElementTreeNode.EntityNode(web), ElementTreeNode.EntityNode(api)),
            viewModel.elementTree,
        )
        assertFalse(viewModel.canUndo)
        assertFalse(viewModel.canRedo)
    }

    @Test
    fun `without a source sync, source text stays at its initial value`() {
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)), initialSourceText = "unchanged")

        viewModel.execute(AddEntityCommand(api))

        assertEquals("unchanged", viewModel.sourceText)
    }

    @Test
    fun `execute regenerates source text through the source sync`() {
        val sync = FakeDiagramSourceSync(Diagram(entities = listOf(web))) { "generated:${it.entities.size}" }
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)), sourceSync = sync)

        viewModel.execute(AddEntityCommand(api))

        assertEquals("generated:2", viewModel.sourceText)
    }

    @Test
    fun `undo and redo regenerate source text through the source sync`() {
        val sync = FakeDiagramSourceSync(Diagram(entities = listOf(web))) { "generated:${it.entities.size}" }
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)), sourceSync = sync)
        viewModel.execute(AddEntityCommand(api))

        viewModel.undo()
        assertEquals("generated:1", viewModel.sourceText)

        viewModel.redo()
        assertEquals("generated:2", viewModel.sourceText)
    }

    @Test
    fun `applyExternalSourceEdit replaces the diagram and keeps the typed text verbatim on success`() {
        val sync = FakeDiagramSourceSync(Diagram(entities = listOf(web))) { "generated:${it.entities.size}" }
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)), sourceSync = sync)
        val edited = Diagram(entities = listOf(web, api))
        sync.externalEditResult = SourceEditOutcome.Applied(edited)

        viewModel.applyExternalSourceEdit("hand-typed source")

        assertEquals(edited, viewModel.diagram)
        assertEquals("hand-typed source", viewModel.sourceText)
        assertEquals(emptyList<String>(), viewModel.sourceParseErrors)
        assertFalse(viewModel.canUndo)
        assertFalse(viewModel.canRedo)
    }

    @Test
    fun `applyExternalSourceEdit surfaces errors without touching diagram or source text on failure`() {
        val sync = FakeDiagramSourceSync(Diagram(entities = listOf(web))) { "generated:${it.entities.size}" }
        val viewModel =
            DiagramViewModel(Diagram(entities = listOf(web)), initialSourceText = "original", sourceSync = sync)
        sync.externalEditResult = SourceEditOutcome.Rejected(listOf("Unable to parse relationship."))

        viewModel.applyExternalSourceEdit("broken source")

        assertEquals(Diagram(entities = listOf(web)), viewModel.diagram)
        assertEquals("original", viewModel.sourceText)
        assertEquals(listOf("Unable to parse relationship."), viewModel.sourceParseErrors)
    }

    @Test
    fun `applyExternalSourceEdit is a no-op without a source sync`() {
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)), initialSourceText = "original")

        viewModel.applyExternalSourceEdit("typed while nothing is wired up")

        assertEquals(Diagram(entities = listOf(web)), viewModel.diagram)
        assertEquals("original", viewModel.sourceText)
        assertEquals(emptyList<String>(), viewModel.sourceParseErrors)
    }
}
