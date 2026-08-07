package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Generation of standalone entity declarations
 * (docs/implementation-plan.md Milestone 4 task 1).
 */
class EntityGenerationTest {
    private fun generate(vararg entities: Entity): String =
        PlantUmlC4Generator.generate(Diagram(entities = entities.toList()))

    @Test
    fun `Person with only alias and name has no optional arguments`() {
        val source = generate(Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON))

        assertTrue(source.contains("""Person(customer, "Customer")"""))
    }

    @Test
    fun `System renders with the System macro`() {
        val source =
            generate(Entity(id = EntityId("banking"), name = "Internet Banking System", type = EntityType.SYSTEM))

        assertTrue(source.contains("""System(banking, "Internet Banking System")"""))
    }

    @Test
    fun `Container renders with the Container macro`() {
        val source = generate(Entity(id = EntityId("web_app"), name = "Web Application", type = EntityType.CONTAINER))

        assertTrue(source.contains("""Container(web_app, "Web Application")"""))
    }

    @Test
    fun `DATABASE renders with the ContainerDb macro`() {
        val source = generate(Entity(id = EntityId("db"), name = "Database", type = EntityType.DATABASE))

        assertTrue(source.contains("""ContainerDb(db, "Database")"""))
    }

    @Test
    fun `Component renders with the Component macro`() {
        val source = generate(Entity(id = EntityId("ctrl"), name = "Controller", type = EntityType.COMPONENT))

        assertTrue(source.contains("""Component(ctrl, "Controller")"""))
    }

    @Test
    fun `multiple top-level entities are each rendered on their own line`() {
        val source =
            generate(
                Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON),
                Entity(id = EntityId("banking"), name = "Internet Banking System", type = EntityType.SYSTEM),
            )

        assertTrue(source.contains("""Person(customer, "Customer")"""))
        assertTrue(source.contains("""System(banking, "Internet Banking System")"""))
    }

    @Test
    fun `an empty diagram still produces a valid startuml-enduml wrapper`() {
        val source = PlantUmlC4Generator.generate(Diagram())

        assertTrue(source.contains("@startuml"))
        assertTrue(source.contains("@enduml"))
    }
}
