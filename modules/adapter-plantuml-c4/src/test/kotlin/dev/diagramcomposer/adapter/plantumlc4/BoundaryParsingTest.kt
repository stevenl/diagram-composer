package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseResult
import dev.diagramcomposer.core.model.BoundaryChildId
import dev.diagramcomposer.core.model.BoundaryId
import dev.diagramcomposer.core.model.BoundaryType
import dev.diagramcomposer.core.model.EntityId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Parsing of boundaries and their nesting
 * (docs/adapters.md §15 "Supported Boundaries").
 */
class BoundaryParsingTest {
    private val adapter = PlantUmlC4Adapter()

    private fun parseSuccessfully(source: String) =
        (adapter.parse(source) as? ParseResult.Success)?.diagram
            ?: error("expected ParseResult.Success, got ${adapter.parse(source)}")

    @Test
    fun `System_Boundary contains the entities declared inside it`() {
        val source =
            """
            System_Boundary(c1, "Internet Banking System") {
                Container(web_app, "Web Application")
                Container(spa, "Single-Page App")
            }
            """.trimIndent()

        val diagram = parseSuccessfully(source)

        assertEquals(2, diagram.entities.size)
        val boundary = diagram.boundaries.single()
        assertEquals(BoundaryId("c1"), boundary.id)
        assertEquals("Internet Banking System", boundary.name)
        assertEquals(BoundaryType.SYSTEM, boundary.type)
        assertEquals(
            listOf(BoundaryChildId.OfEntity(EntityId("web_app")), BoundaryChildId.OfEntity(EntityId("spa"))),
            boundary.children,
        )
    }

    @Test
    fun `Container_Boundary and Enterprise_Boundary map to their respective types`() {
        val diagram =
            parseSuccessfully(
                """
                Enterprise_Boundary(e1, "Big Bank plc") {
                    Container_Boundary(c1, "API") {
                        Component(controller, "Controller")
                    }
                }
                """.trimIndent(),
            )

        val byId = diagram.boundaries.associateBy { it.id }
        assertEquals(BoundaryType.ENTERPRISE, byId.getValue(BoundaryId("e1")).type)
        assertEquals(BoundaryType.CONTAINER, byId.getValue(BoundaryId("c1")).type)
    }

    @Test
    fun `nested boundaries record the inner boundary as a child of the outer one`() {
        val diagram =
            parseSuccessfully(
                """
                System_Boundary(outer, "Outer") {
                    Container_Boundary(inner, "Inner") {
                        Component(controller, "Controller")
                    }
                }
                """.trimIndent(),
            )

        val outer = diagram.boundaries.single { it.id == BoundaryId("outer") }
        val inner = diagram.boundaries.single { it.id == BoundaryId("inner") }

        assertEquals(listOf(BoundaryChildId.OfBoundary(BoundaryId("inner"))), outer.children)
        assertEquals(listOf(BoundaryChildId.OfEntity(EntityId("controller"))), inner.children)
    }

    @Test
    fun `entities outside any boundary are not attached to a boundary`() {
        val diagram =
            parseSuccessfully(
                """
                Person(customer, "Customer")
                System_Boundary(c1, "Internet Banking System") {
                    Container(web_app, "Web Application")
                }
                """.trimIndent(),
            )

        val boundary = diagram.boundaries.single()
        assertTrue(boundary.children.none { it == BoundaryChildId.OfEntity(EntityId("customer")) })
    }

    @Test
    fun `an unclosed boundary fails to parse`() {
        val result =
            adapter.parse(
                """
                System_Boundary(c1, "Internet Banking System") {
                    Container(web_app, "Web Application")
                """.trimIndent(),
            )

        assertTrue(result is ParseResult.Failure)
        assertTrue((result as ParseResult.Failure).errors.any { it.message.contains("c1") })
    }

    @Test
    fun `a stray closing brace fails to parse`() {
        val result = adapter.parse("}")

        assertTrue(result is ParseResult.Failure)
        assertEquals(1, (result as ParseResult.Failure).errors.single().line)
    }

    @Test
    fun `a boundary declaration without a trailing brace fails to parse`() {
        val result = adapter.parse("""System_Boundary(c1, "Internet Banking System")""")

        assertTrue(result is ParseResult.Failure)
    }
}
