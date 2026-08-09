package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipId
import dev.diagramcomposer.core.model.RelationshipType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Generation of relationships (docs/implementation-plan.md Milestone 4 task 2). */
class RelationshipGenerationTest {
    private val customer = Entity(id = EntityId("customer"), name = "Customer", type = EntityType.PERSON)
    private val banking = Entity(id = EntityId("banking"), name = "Internet Banking System", type = EntityType.SYSTEM)

    private fun generate(relationship: Relationship): String =
        PlantUmlC4Generator.generate(
            Diagram(entities = listOf(customer, banking), relationships = listOf(relationship)),
        )

    @Test
    fun `a DEFAULT relationship without a direction renders with Rel`() {
        val source =
            generate(
                Relationship(id = RelationshipId("r1"), sourceId = customer.id, targetId = banking.id),
            )

        assertTrue(source.contains("Rel(customer, banking,"))
    }

    @Test
    fun `a BIDIRECTIONAL relationship renders with BiRel`() {
        val source =
            generate(
                Relationship(
                    id = RelationshipId("r1"),
                    sourceId = customer.id,
                    targetId = banking.id,
                    type = RelationshipType.BIDIRECTIONAL,
                ),
            )

        assertTrue(source.contains("BiRel(customer, banking,"))
    }

    @Test
    fun `direction properties render the matching directional macro`() {
        val expectedMacroByDirection =
            mapOf(
                "up" to "Rel_U",
                "down" to "Rel_D",
                "left" to "Rel_L",
                "right" to "Rel_R",
            )

        for ((direction, macro) in expectedMacroByDirection) {
            val source =
                generate(
                    Relationship(
                        id = RelationshipId("r1"),
                        sourceId = customer.id,
                        targetId = banking.id,
                        properties = mapOf("direction" to direction),
                    ),
                )

            assertTrue(source.contains("$macro(customer, banking,")) { "expected $macro for direction=$direction" }
        }
    }

    @Test
    fun `description and technology render as named arguments`() {
        val source =
            generate(
                Relationship(
                    id = RelationshipId("r1"),
                    sourceId = customer.id,
                    targetId = banking.id,
                    description = "Uses",
                    technology = "HTTPS",
                ),
            )

        assertTrue(source.contains("""${'$'}descr="Uses""""))
        assertTrue(source.contains("""${'$'}techn="HTTPS""""))
    }

    @Test
    fun `the relationship id is preserved via a $id named argument`() {
        val source =
            generate(
                Relationship(id = RelationshipId("checkout-flow"), sourceId = customer.id, targetId = banking.id),
            )

        assertTrue(source.contains("""${'$'}id="checkout-flow""""))
    }
}
