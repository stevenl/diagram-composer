package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseResult
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.RelationshipType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Parsing of relationships (docs/adapters.md §15 "Supported Relationships"). */
class RelationshipParsingTest {
    private val adapter = PlantUmlC4Adapter()

    private fun parseSuccessfully(source: String) =
        (adapter.parse(source) as? ParseResult.Success)?.diagram
            ?: error("expected ParseResult.Success, got ${adapter.parse(source)}")

    private val twoEntities =
        """
        Person(customer, "Customer")
        System(banking, "Internet Banking System")
        """.trimIndent()

    @Test
    fun `Rel maps to a DEFAULT relationship with description and technology`() {
        val diagram =
            parseSuccessfully(
                "$twoEntities\n" + """Rel(customer, banking, "Uses", "HTTPS")""",
            )

        val relationship = diagram.relationships.single()
        assertEquals(EntityId("customer"), relationship.sourceId)
        assertEquals(EntityId("banking"), relationship.targetId)
        assertEquals("Uses", relationship.description)
        assertEquals("HTTPS", relationship.technology)
        assertEquals(RelationshipType.DEFAULT, relationship.type)
    }

    @Test
    fun `BiRel maps to a BIDIRECTIONAL relationship`() {
        val diagram =
            parseSuccessfully(
                "$twoEntities\n" + """BiRel(customer, banking, "Exchanges data with")""",
            )

        assertEquals(RelationshipType.BIDIRECTIONAL, diagram.relationships.single().type)
    }

    @Test
    fun `directional Rel variants record direction in properties`() {
        val source =
            """
            $twoEntities
            Rel_U(customer, banking, "Uses")
            Rel_D(customer, banking, "Uses")
            Rel_L(customer, banking, "Uses")
            Rel_R(customer, banking, "Uses")
            """.trimIndent()

        val diagram = parseSuccessfully(source)

        assertEquals(
            listOf("up", "down", "left", "right"),
            diagram.relationships.map { it.properties["direction"] },
        )
    }

    @Test
    fun `plain Rel has no direction property`() {
        val diagram = parseSuccessfully("$twoEntities\n" + """Rel(customer, banking, "Uses")""")

        assertTrue("direction" !in diagram.relationships.single().properties)
    }

    @Test
    fun `each relationship gets a unique generated id`() {
        val source =
            """
            $twoEntities
            Rel(customer, banking, "Uses")
            Rel(banking, customer, "Notifies")
            """.trimIndent()

        val diagram = parseSuccessfully(source)

        assertEquals(
            2,
            diagram.relationships
                .map { it.id }
                .toSet()
                .size,
        )
    }

    @Test
    fun `relationship referencing an unknown entity fails to parse`() {
        val result =
            adapter.parse(
                """
                Person(customer, "Customer")
                Rel(customer, missing_system, "Uses")
                """.trimIndent(),
            )

        assertTrue(result is ParseResult.Failure)
        assertTrue((result as ParseResult.Failure).errors.isNotEmpty())
    }

    @Test
    fun `relationship missing a target entity argument fails to parse`() {
        val result = adapter.parse("Rel(customer)")

        assertTrue(result is ParseResult.Failure)
        assertEquals(1, (result as ParseResult.Failure).errors.single().line)
    }
}
