package dev.diagramcomposer.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DiagramTest {

    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)

    private fun validRelationship() = Relationship(
        id = RelationshipId("rel-1"),
        sourceId = webApp.id,
        targetId = paymentApi.id,
    )

    private fun validBoundary() = Boundary(
        id = BoundaryId("payments-system"),
        name = "Payments System",
        type = BoundaryType.SYSTEM,
        children = listOf(BoundaryChildId.OfEntity(paymentApi.id)),
    )

    @Test
    fun `an empty diagram is valid`() {
        val diagram = Diagram()

        assertEquals(emptyList<Entity>(), diagram.entities)
        assertEquals(emptyList<Relationship>(), diagram.relationships)
        assertEquals(emptyList<Boundary>(), diagram.boundaries)
    }

    @Test
    fun `a diagram with consistent entities, relationships, and boundaries is valid`() {
        val diagram = Diagram(
            entities = listOf(webApp, paymentApi),
            relationships = listOf(validRelationship()),
            boundaries = listOf(validBoundary()),
        )

        assertEquals(listOf(webApp, paymentApi), diagram.entities)
        assertEquals(listOf(validRelationship()), diagram.relationships)
        assertEquals(listOf(validBoundary()), diagram.boundaries)
    }

    @Test
    fun `duplicate entity ids are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Diagram(entities = listOf(webApp, webApp.copy(name = "Web App (duplicate)")))
        }
    }

    @Test
    fun `duplicate relationship ids are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Diagram(
                entities = listOf(webApp, paymentApi),
                relationships = listOf(validRelationship(), validRelationship()),
            )
        }
    }

    @Test
    fun `duplicate boundary ids are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Diagram(
                entities = listOf(paymentApi),
                boundaries = listOf(validBoundary(), validBoundary()),
            )
        }
    }

    @Test
    fun `a relationship referencing a missing source entity is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Diagram(
                entities = listOf(paymentApi),
                relationships = listOf(validRelationship()),
            )
        }
    }

    @Test
    fun `a relationship referencing a missing target entity is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Diagram(
                entities = listOf(webApp),
                relationships = listOf(validRelationship()),
            )
        }
    }

    @Test
    fun `a boundary referencing a missing entity child is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Diagram(entities = emptyList(), boundaries = listOf(validBoundary()))
        }
    }

    @Test
    fun `a boundary referencing a missing nested boundary child is rejected`() {
        val outer = Boundary(
            id = BoundaryId("enterprise"),
            name = "Enterprise",
            type = BoundaryType.ENTERPRISE,
            children = listOf(BoundaryChildId.OfBoundary(BoundaryId("nonexistent"))),
        )

        assertThrows(IllegalArgumentException::class.java) {
            Diagram(boundaries = listOf(outer))
        }
    }

    @Test
    fun `a boundary referencing an existing nested boundary is valid`() {
        val inner = Boundary(id = BoundaryId("inner"), name = "Inner", type = BoundaryType.CONTAINER)
        val outer = Boundary(
            id = BoundaryId("outer"),
            name = "Outer",
            type = BoundaryType.SYSTEM,
            children = listOf(BoundaryChildId.OfBoundary(inner.id)),
        )

        val diagram = Diagram(boundaries = listOf(inner, outer))

        assertEquals(listOf(inner, outer), diagram.boundaries)
    }

    @Test
    fun `copy re-validates invariants`() {
        val diagram = Diagram(entities = listOf(webApp, paymentApi), relationships = listOf(validRelationship()))

        assertThrows(IllegalArgumentException::class.java) {
            diagram.copy(entities = listOf(webApp)) // drops paymentApi, orphaning the relationship
        }
    }

    @Test
    fun `rootChildren defaults to entities then boundaries not claimed by a boundary`() {
        val diagram =
            Diagram(
                entities = listOf(webApp, paymentApi),
                boundaries = listOf(validBoundary()), // claims paymentApi
            )

        assertEquals(
            listOf(BoundaryChildId.OfEntity(webApp.id), BoundaryChildId.OfBoundary(validBoundary().id)),
            diagram.rootChildren,
        )
    }

    @Test
    fun `an explicit rootChildren interleaving entities and boundaries is honoured`() {
        val boundary = validBoundary() // claims paymentApi as a child
        val interleaved =
            listOf(
                BoundaryChildId.OfEntity(webApp.id),
                BoundaryChildId.OfBoundary(boundary.id),
            )

        val diagram =
            Diagram(
                entities = listOf(webApp, paymentApi),
                boundaries = listOf(boundary),
                rootChildren = interleaved,
            )

        assertEquals(interleaved, diagram.rootChildren)
    }

    @Test
    fun `rootChildren referencing a missing entity is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Diagram(entities = emptyList(), rootChildren = listOf(BoundaryChildId.OfEntity(webApp.id)))
        }
    }

    @Test
    fun `an entity missing from both rootChildren and every boundary's children is rejected`() {
        // webApp exists in `entities` but isn't referenced by rootChildren or any boundary,
        // so it would silently disappear from generated output.
        assertThrows(IllegalArgumentException::class.java) {
            Diagram(entities = listOf(webApp), rootChildren = emptyList())
        }
    }

    @Test
    fun `an entity appearing in rootChildren and also as a boundary child is rejected`() {
        // paymentApi is already a child of validBoundary(); also listing it in rootChildren
        // would render it twice.
        assertThrows(IllegalArgumentException::class.java) {
            Diagram(
                entities = listOf(paymentApi),
                boundaries = listOf(validBoundary()),
                rootChildren = listOf(BoundaryChildId.OfEntity(paymentApi.id)),
            )
        }
    }
}
