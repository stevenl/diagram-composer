package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseResult
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
import dev.diagramcomposer.core.model.RelationshipType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * `Diagram -> source -> Diagram` round-trip stability
 * (docs/implementation-plan.md Milestone 4 task 6): unlike
 * [SourceRoundTripTest] (which starts from hand-written PlantUML),
 * these cases construct [Diagram] instances directly via the core model —
 * including [RelationshipId]s the generator/parser wouldn't have produced
 * on their own — and assert the reparsed diagram is *exactly* equal
 * (`==`) to the original, not just semantically equivalent.
 *
 * This is what motivates [PlantUmlC4Generator] round-tripping
 * [Relationship.id] via its own `$id=` argument (see that class's KDoc):
 * without it, an arbitrary hand-picked id like `"checkout-flow"` below
 * would come back renumbered as `"rel-1"` and equality would fail.
 */
class DiagramRoundTripTest {
    private val adapter = PlantUmlC4Adapter()

    private fun assertRoundTrips(diagram: Diagram) {
        val source = adapter.generate(diagram)
        val result = adapter.parse(source)

        assertTrue(result is ParseResult.Success) { "expected success parsing:\n$source\ngot: $result" }
        assertEquals(diagram, (result as ParseResult.Success).diagram)
    }

    @Test
    fun `a single entity round-trips`() {
        assertRoundTrips(
            Diagram(entities = listOf(Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON))),
        )
    }

    @Test
    fun `an entity with technology, description, tags, and extra properties round-trips`() {
        assertRoundTrips(
            Diagram(
                entities =
                    listOf(
                        Entity(
                            id = EntityId("web_app"),
                            name = "Web Application",
                            type = EntityType.CONTAINER,
                            description = "Delivers the SPA to the browser.",
                            technology = "Java, Spring MVC",
                            tags = listOf("frontend", "critical"),
                            properties = mapOf("sprite" to "server", "link" to "https://example.com"),
                        ),
                    ),
            ),
        )
    }

    @Test
    fun `a bidirectional relationship with an arbitrary id, description, and technology round-trips`() {
        val customer = Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON)
        val banking = Entity(id = EntityId("banking"), name = "Internet Banking System", type = EntityType.SYSTEM)

        assertRoundTrips(
            Diagram(
                entities = listOf(customer, banking),
                relationships =
                    listOf(
                        Relationship(
                            id = RelationshipId("checkout-flow"),
                            sourceId = customer.id,
                            targetId = banking.id,
                            description = "Uses",
                            technology = "HTTPS",
                            type = RelationshipType.BIDIRECTIONAL,
                            properties = mapOf("importance" to "high"),
                        ),
                    ),
            ),
        )
    }

    @Test
    fun `a directional relationship with an arbitrary id and extra properties round-trips`() {
        val customer = Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON)
        val banking = Entity(id = EntityId("banking"), name = "Internet Banking System", type = EntityType.SYSTEM)

        assertRoundTrips(
            Diagram(
                entities = listOf(customer, banking),
                relationships =
                    listOf(
                        Relationship(
                            id = RelationshipId("checkout-flow"),
                            sourceId = customer.id,
                            targetId = banking.id,
                            description = "Uses",
                            properties = mapOf("direction" to "up", "importance" to "high"),
                        ),
                    ),
            ),
        )
    }

    @Test
    fun `several relationships each keep their own distinct id`() {
        val customer = Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON)
        val banking = Entity(id = EntityId("banking"), name = "Internet Banking System", type = EntityType.SYSTEM)

        assertRoundTrips(
            Diagram(
                entities = listOf(customer, banking),
                relationships =
                    listOf(
                        Relationship(id = RelationshipId("uses"), sourceId = customer.id, targetId = banking.id),
                        Relationship(id = RelationshipId("notifies"), sourceId = banking.id, targetId = customer.id),
                    ),
            ),
        )
    }

    @Test
    fun `nested boundaries round-trip`() {
        val controller = Entity(id = EntityId("controller"), name = "Controller", type = EntityType.COMPONENT)
        val webApp = Entity(id = EntityId("web_app"), name = "Web Application", type = EntityType.CONTAINER)
        val inner =
            Boundary(
                id = BoundaryId("inner"),
                name = "API",
                type = BoundaryType.CONTAINER,
                children = listOf(BoundaryChildId.OfEntity(controller.id)),
            )
        val outer =
            Boundary(
                id = BoundaryId("outer"),
                name = "Internet Banking System",
                type = BoundaryType.SYSTEM,
                children = listOf(BoundaryChildId.OfBoundary(inner.id), BoundaryChildId.OfEntity(webApp.id)),
            )

        assertRoundTrips(
            // Listed inner-before-outer to match the parser's canonical order: a
            // nested boundary's closing '}' is always encountered (and so
            // appended to Diagram.boundaries) before its parent's — see
            // PlantUmlC4Parser.parse. Diagram.boundaries is a plain List, so
            // equality is order-sensitive; constructing the diagram in a
            // different order would make this round-trip fail for a reason
            // that has nothing to do with the adapter being wrong.
            Diagram(entities = listOf(controller, webApp), boundaries = listOf(inner, outer)),
        )
    }

    @Test
    fun `a top-level entity declared after a boundary in rootChildren round-trips`() {
        val customer = Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON)
        val webApp = Entity(id = EntityId("web_app"), name = "Web Application", type = EntityType.CONTAINER)
        val emailSystem =
            Entity(id = EntityId("email_system"), name = "E-Mail System", type = EntityType.SYSTEM, external = true)
        val boundary =
            Boundary(
                id = BoundaryId("banking"),
                name = "Internet Banking System",
                type = BoundaryType.SYSTEM,
                children = listOf(BoundaryChildId.OfEntity(webApp.id)),
            )

        assertRoundTrips(
            Diagram(
                entities = listOf(customer, webApp, emailSystem),
                boundaries = listOf(boundary),
                rootChildren =
                    listOf(
                        BoundaryChildId.OfEntity(customer.id),
                        BoundaryChildId.OfBoundary(boundary.id),
                        BoundaryChildId.OfEntity(emailSystem.id),
                    ),
            ),
        )
    }

    @Test
    fun `an empty diagram round-trips`() {
        assertRoundTrips(Diagram())
    }

    @Test
    fun `a diagram combining entities, boundaries, and relationships round-trips`() {
        val customer = Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON)
        val spa =
            Entity(id = EntityId("spa"), name = "Single-Page App", type = EntityType.CONTAINER, technology = "Angular")
        val database = Entity(id = EntityId("database"), name = "Database", type = EntityType.DATABASE)
        val boundary =
            Boundary(
                id = BoundaryId("banking"),
                name = "Internet Banking System",
                type = BoundaryType.SYSTEM,
                children = listOf(BoundaryChildId.OfEntity(spa.id), BoundaryChildId.OfEntity(database.id)),
            )

        assertRoundTrips(
            Diagram(
                entities = listOf(customer, spa, database),
                relationships =
                    listOf(
                        Relationship(
                            id = RelationshipId("uses"),
                            sourceId = customer.id,
                            targetId = spa.id,
                            description = "Uses"
                        ),
                        Relationship(
                            id = RelationshipId("persists"),
                            sourceId = spa.id,
                            targetId = database.id,
                            description = "Reads/writes",
                            technology = "JDBC",
                            properties = mapOf("direction" to "down"),
                        ),
                    ),
                boundaries = listOf(boundary),
            ),
        )
    }
}
