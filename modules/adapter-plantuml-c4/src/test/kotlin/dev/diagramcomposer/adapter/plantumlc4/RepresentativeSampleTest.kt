package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseResult
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.RelationshipType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Parses a single, realistic C4-PlantUML container diagram exercising every
 * construct together — elements, relationships, boundaries (including
 * nesting), and properties — matching Milestone 3's definition of done in
 * `docs/implementation-plan.md`.
 */
class RepresentativeSampleTest {
    private val sample =
        """
        @startuml
        !include <C4/C4_Container>

        title Internet Banking System — Container Diagram

        Person(customer, "Personal Banking Customer", "A customer of the bank, with personal bank accounts.")

        System_Boundary(banking, "Internet Banking System") {
            Container(spa, "Single-Page App", "JavaScript, Angular", "Provides internet banking functionality via browser.", ${'$'}tags="frontend")
            Container(web_app, "Web Application", "Java, Spring MVC", "Delivers the static content and the SPA.")
            ContainerDb(database, "Database", "PostgreSQL", "Stores user registration, auth, and audit data.")

            Container_Boundary(api, "API") {
                Component(accounts_ctrl, "Accounts Controller", "Spring MVC Controller", "Serves account information.")
            }
        }

        System_Ext(email_system, "E-Mail System", "The internal Microsoft Exchange system.")

        Rel(customer, spa, "Uses", "HTTPS")
        Rel(spa, web_app, "Makes API calls to", "JSON/HTTPS")
        Rel_D(web_app, database, "Reads from and writes to", "JDBC")
        BiRel(web_app, email_system, "Sends e-mails using")

        @enduml
        """.trimIndent()

    @Test
    fun `a representative container diagram parses end-to-end`() {
        val result = PlantUmlC4Adapter().parse(sample)

        assertTrue(result is ParseResult.Success)
        val diagram = (result as ParseResult.Success).diagram

        assertEquals(6, diagram.entities.size)
        assertEquals(4, diagram.relationships.size)
        assertEquals(2, diagram.boundaries.size)

        val spa = diagram.entities.single { it.id == EntityId("spa") }
        assertEquals(EntityType.CONTAINER, spa.type)
        assertEquals("JavaScript, Angular", spa.technology)
        assertEquals(listOf("frontend"), spa.tags)

        val database = diagram.entities.single { it.id == EntityId("database") }
        assertEquals(EntityType.DATABASE, database.type)

        // 3 directly-contained entities (spa, web_app, database) plus the nested Container_Boundary("api").
        val outerBoundary = diagram.boundaries.single { it.id == BoundaryId("banking") }
        assertEquals(4, outerBoundary.children.size)

        val readsWrites = diagram.relationships.single { it.description == "Reads from and writes to" }
        assertEquals("down", readsWrites.properties["direction"])

        val emailRel = diagram.relationships.single { it.description == "Sends e-mails using" }
        assertEquals(RelationshipType.BIDIRECTIONAL, emailRel.type)
    }
}
