package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseResult
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Parsing of standalone entity declarations
 * (docs/adapters.md §15 "Supported Entities"), before any relationships or
 * boundaries are involved.
 */
class EntityParsingTest {
    private val adapter = PlantUmlC4Adapter()

    private fun parseSuccessfully(source: String) =
        (adapter.parse(source) as? ParseResult.Success)?.diagram
            ?: error("expected ParseResult.Success, got ${adapter.parse(source)}")

    @Test
    fun `Person maps to a PERSON entity with alias, name and description`() {
        val diagram = parseSuccessfully("""Person(customer, "Customer", "A retail bank customer.")""")

        assertEquals(1, diagram.entities.size)
        val entity = diagram.entities.single()
        assertEquals(EntityId("customer"), entity.id)
        assertEquals("Customer", entity.name)
        assertEquals(EntityType.PERSON, entity.type)
        assertEquals("A retail bank customer.", entity.description)
        assertEquals(false, entity.external)
    }

    @Test
    fun `Person_Ext maps to PERSON with external set`() {
        val diagram = parseSuccessfully("""Person_Ext(auditor, "External Auditor")""")

        val entity = diagram.entities.single()
        assertEquals(EntityType.PERSON, entity.type)
        assertEquals(true, entity.external)
    }

    @Test
    fun `System and System_Ext map to SYSTEM, only System_Ext sets external`() {
        val diagram =
            parseSuccessfully(
                """
                System(banking, "Internet Banking System", "Lets customers view accounts.")
                System_Ext(email, "E-Mail System")
                """.trimIndent(),
            )

        assertEquals(
            listOf(EntityType.SYSTEM, EntityType.SYSTEM),
            diagram.entities.map { it.type },
        )
        assertEquals("Lets customers view accounts.", diagram.entities[0].description)
        assertEquals(false, diagram.entities[0].external)
        assertEquals(true, diagram.entities[1].external)
    }

    @Test
    fun `Container maps to CONTAINER with technology and description`() {
        val diagram =
            parseSuccessfully(
                """Container(web_app, "Web Application", "Java, Spring MVC", "Delivers the SPA to the browser.")""",
            )

        val entity = diagram.entities.single()
        assertEquals(EntityType.CONTAINER, entity.type)
        assertEquals("Java, Spring MVC", entity.technology)
        assertEquals("Delivers the SPA to the browser.", entity.description)
    }

    @Test
    fun `ContainerDb maps to DATABASE`() {
        val diagram =
            parseSuccessfully(
                """ContainerDb(database, "Database", "PostgreSQL", "Stores accounts and transactions.")""",
            )

        assertEquals(EntityType.DATABASE, diagram.entities.single().type)
    }

    @Test
    fun `Component maps to COMPONENT`() {
        val diagram = parseSuccessfully("""Component(controller, "Accounts Controller", "Spring MVC Controller")""")

        val entity = diagram.entities.single()
        assertEquals(EntityType.COMPONENT, entity.type)
        assertEquals("Spring MVC Controller", entity.technology)
    }

    @Test
    fun `entity with only alias and label leaves description and technology null`() {
        val diagram = parseSuccessfully("""System(banking, "Internet Banking System")""")

        val entity = diagram.entities.single()
        assertEquals(null, entity.description)
        assertEquals(null, entity.technology)
    }

    @Test
    fun `multiple entities on separate lines are all parsed`() {
        val diagram =
            parseSuccessfully(
                """
                Person(customer, "Customer")
                System(banking, "Internet Banking System")
                """.trimIndent(),
            )

        assertEquals(2, diagram.entities.size)
    }

    @Test
    fun `entity missing a required label fails to parse`() {
        val result = adapter.parse("System(banking)")

        assertTrue(result is ParseResult.Failure)
        val errors = (result as ParseResult.Failure).errors
        assertEquals(1, errors.size)
        assertEquals(1, errors.single().line)
    }

    @Test
    fun `blank lines and comments between entities are ignored`() {
        val diagram =
            parseSuccessfully(
                """
                ' this is a comment
                Person(customer, "Customer")

                System(banking, "Internet Banking System")
                """.trimIndent(),
            )

        assertEquals(2, diagram.entities.size)
    }
}
