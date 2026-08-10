package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
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
    }

    @Test
    fun `refresh replaces the displayed diagram and its derived views`() {
        val viewModel = DiagramViewModel(Diagram(entities = listOf(web)))

        viewModel.refresh(Diagram(entities = listOf(web, api)))

        assertEquals(Diagram(entities = listOf(web, api)), viewModel.diagram)
        assertEquals(
            listOf(ElementTreeNode.EntityNode(web), ElementTreeNode.EntityNode(api)),
            viewModel.elementTree,
        )
    }
}
