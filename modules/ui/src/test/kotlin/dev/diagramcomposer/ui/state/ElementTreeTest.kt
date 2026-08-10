package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.BoundaryType
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ElementTreeTest {
    private val customer = Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON)
    private val web = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val api = Entity(id = EntityId("api"), name = "API", type = EntityType.CONTAINER)

    @Test
    fun `an empty diagram has an empty tree`() {
        assertEquals(emptyList<ElementTreeNode>(), buildElementTree(Diagram()))
    }

    @Test
    fun `top-level entities with no boundary appear as entity nodes in declaration order`() {
        val diagram = Diagram(entities = listOf(customer, web))

        assertEquals(
            listOf(ElementTreeNode.EntityNode(customer), ElementTreeNode.EntityNode(web)),
            buildElementTree(diagram),
        )
    }

    @Test
    fun `entities inside a boundary are nested under a boundary node`() {
        val boundary =
            Boundary(
                id = BoundaryId("shop"),
                name = "Online Shop",
                type = BoundaryType.SYSTEM,
                children = listOf(BoundaryChildId.OfEntity(web.id), BoundaryChildId.OfEntity(api.id)),
            )
        val diagram =
            Diagram(
                entities = listOf(customer, web, api),
                boundaries = listOf(boundary),
                rootChildren =
                    listOf(BoundaryChildId.OfEntity(customer.id), BoundaryChildId.OfBoundary(boundary.id)),
            )

        val tree = buildElementTree(diagram)

        assertEquals(
            listOf(
                ElementTreeNode.EntityNode(customer),
                ElementTreeNode.BoundaryNode(
                    boundary,
                    listOf(ElementTreeNode.EntityNode(web), ElementTreeNode.EntityNode(api)),
                ),
            ),
            tree,
        )
    }

    @Test
    fun `nested boundaries resolve recursively`() {
        val inner =
            Boundary(
                id = BoundaryId("inner"),
                name = "Inner",
                type = BoundaryType.CONTAINER,
                children = listOf(BoundaryChildId.OfEntity(api.id)),
            )
        val outer =
            Boundary(
                id = BoundaryId("outer"),
                name = "Outer",
                type = BoundaryType.SYSTEM,
                children = listOf(BoundaryChildId.OfBoundary(inner.id)),
            )
        val diagram =
            Diagram(
                entities = listOf(api),
                boundaries = listOf(outer, inner),
                rootChildren = listOf(BoundaryChildId.OfBoundary(outer.id)),
            )

        val tree = buildElementTree(diagram)

        assertEquals(
            listOf(
                ElementTreeNode.BoundaryNode(
                    outer,
                    listOf(ElementTreeNode.BoundaryNode(inner, listOf(ElementTreeNode.EntityNode(api)))),
                ),
            ),
            tree,
        )
    }
}
