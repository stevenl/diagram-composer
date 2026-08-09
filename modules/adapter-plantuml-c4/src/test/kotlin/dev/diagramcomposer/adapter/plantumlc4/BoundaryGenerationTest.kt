package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.BoundaryType
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Generation of boundaries, including nesting (docs/implementation-plan.md Milestone 4 task 3). */
class BoundaryGenerationTest {
    @Test
    fun `System_Boundary opens with a trailing brace and closes on its own line`() {
        val webApp = Entity(id = EntityId("web_app"), name = "Web Application", type = EntityType.CONTAINER)
        val diagram =
            Diagram(
                entities = listOf(webApp),
                boundaries =
                    listOf(
                        Boundary(
                            id = BoundaryId("c1"),
                            name = "Internet Banking System",
                            type = BoundaryType.SYSTEM,
                            children = listOf(BoundaryChildId.OfEntity(webApp.id)),
                        ),
                    ),
            )

        val source = PlantUmlC4Generator.generate(diagram)

        assertTrue(source.contains("""System_Boundary(c1, "Internet Banking System") {"""))
        assertTrue(source.lineSequence().any { it.trim() == "}" })
        assertTrue(source.contains("""Container(web_app, "Web Application")"""))
    }

    @Test
    fun `Container_Boundary and Enterprise_Boundary render with their matching macros`() {
        val diagram =
            Diagram(
                boundaries =
                    listOf(
                        Boundary(id = BoundaryId("e1"), name = "Big Bank plc", type = BoundaryType.ENTERPRISE),
                        Boundary(id = BoundaryId("c1"), name = "API", type = BoundaryType.CONTAINER),
                    ),
            )

        val source = PlantUmlC4Generator.generate(diagram)

        assertTrue(source.contains("""Enterprise_Boundary(e1, "Big Bank plc") {"""))
        assertTrue(source.contains("""Container_Boundary(c1, "API") {"""))
    }

    @Test
    fun `a nested boundary is rendered inside its parent's block, not at the top level`() {
        val controller = Entity(id = EntityId("controller"), name = "Controller", type = EntityType.COMPONENT)
        val inner =
            Boundary(
                id = BoundaryId("inner"),
                name = "Inner",
                type = BoundaryType.CONTAINER,
                children = listOf(BoundaryChildId.OfEntity(controller.id)),
            )
        val outer =
            Boundary(
                id = BoundaryId("outer"),
                name = "Outer",
                type = BoundaryType.SYSTEM,
                children = listOf(BoundaryChildId.OfBoundary(inner.id)),
            )
        val diagram = Diagram(entities = listOf(controller), boundaries = listOf(outer, inner))

        val source = PlantUmlC4Generator.generate(diagram)
        val lines = source.lines().map { it.trim() }

        val outerLineIndex = lines.indexOfFirst { it.startsWith("System_Boundary(outer") }
        val innerLineIndex = lines.indexOfFirst { it.startsWith("Container_Boundary(inner") }
        assertTrue(outerLineIndex in 0 until innerLineIndex)
    }

    @Test
    fun `entities not attached to any boundary are rendered at the top level`() {
        val customer = Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON)
        val diagram =
            Diagram(
                entities = listOf(customer),
                boundaries = listOf(Boundary(id = BoundaryId("c1"), name = "System", type = BoundaryType.SYSTEM)),
            )

        val source = PlantUmlC4Generator.generate(diagram)
        val lines = source.lines().map { it.trim() }

        val customerLine = lines.indexOfFirst { it.startsWith("Person(customer") }
        val boundaryOpenLine = lines.indexOfFirst { it.startsWith("System_Boundary(c1") }
        assertTrue(customerLine < boundaryOpenLine)
    }
}
