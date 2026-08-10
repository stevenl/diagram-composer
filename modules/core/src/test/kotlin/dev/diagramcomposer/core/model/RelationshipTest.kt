package dev.diagramcomposer.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RelationshipTest {
    private fun sampleRelationship() =
        Relationship(
            id = RelationshipId("rel-1"),
            sourceId = EntityId("web"),
            targetId = EntityId("payment-api"),
            description = "Uses REST API",
        )

    @Test
    fun `relationships with the same field values are equal`() {
        assertEquals(sampleRelationship(), sampleRelationship())
    }

    @Test
    fun `copy changes only the specified field`() {
        val retargeted = sampleRelationship().copy(targetId = EntityId("orders-api"))

        assertEquals(EntityId("orders-api"), retargeted.targetId)
        assertEquals(sampleRelationship().sourceId, retargeted.sourceId)
        assertEquals(sampleRelationship().description, retargeted.description)
    }

    @Test
    fun `type defaults to DEFAULT`() {
        assertEquals(RelationshipType.DEFAULT, sampleRelationship().type)
    }

    @Test
    fun `type can be set to BIDIRECTIONAL`() {
        val biRel = sampleRelationship().copy(type = RelationshipType.BIDIRECTIONAL)

        assertEquals(RelationshipType.BIDIRECTIONAL, biRel.type)
    }
}
