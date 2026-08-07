package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Generation of entity/relationship properties matching PlantUML C4 syntax
 * (docs/implementation-plan.md Milestone 4 task 4).
 */
class PropertyGenerationTest {
    private fun generate(entity: Entity): String = PlantUmlC4Generator.generate(Diagram(entities = listOf(entity)))

    @Test
    fun `technology renders as a $techn named argument`() {
        val source =
            generate(
                Entity(
                    id = EntityId("web_app"),
                    name = "Web Application",
                    type = EntityType.CONTAINER,
                    technology = "Java, Spring MVC"
                ),
            )

        assertTrue(source.contains("""${'$'}techn="Java, Spring MVC""""))
    }

    @Test
    fun `description renders as a $descr named argument`() {
        val source =
            generate(
                Entity(
                    id = EntityId("banking"),
                    name = "Internet Banking System",
                    type = EntityType.SYSTEM,
                    description = "Lets customers view accounts.",
                ),
            )

        assertTrue(source.contains("""${'$'}descr="Lets customers view accounts.""""))
    }

    @Test
    fun `tags render as a single comma-joined $tags named argument`() {
        val source =
            generate(
                Entity(
                    id = EntityId("banking"),
                    name = "Internet Banking System",
                    type = EntityType.SYSTEM,
                    tags = listOf("internal", "critical"),
                ),
            )

        assertTrue(source.contains("""${'$'}tags="internal,critical""""))
    }

    @Test
    fun `leftover properties render as their own named arguments`() {
        val source =
            generate(
                Entity(
                    id = EntityId("banking"),
                    name = "Internet Banking System",
                    type = EntityType.SYSTEM,
                    properties = mapOf("sprite" to "server"),
                ),
            )

        assertTrue(source.contains("""${'$'}sprite="server""""))
    }

    @Test
    fun `a name containing a double quote is escaped`() {
        val source =
            generate(
                Entity(id = EntityId("banking"), name = """The "Banking" System""", type = EntityType.SYSTEM),
            )

        assertTrue(source.contains("""banking, "The \"Banking\" System")"""))
    }

    @Test
    fun `an entity with no optional fields has no named arguments at all`() {
        val source = generate(Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON))

        assertTrue(source.contains("""Person(customer, "Customer")"""))
        assertTrue("$" !in source.substringAfter("""Person(customer, "Customer")""").substringBefore("\n"))
    }
}
