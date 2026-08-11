package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IdSuggestionTest {
    @Test
    fun `suggests a camelCase id for a multi-word name`() {
        assertEquals(EntityId("paymentApi"), suggestEntityId("Payment API", Diagram()))
    }

    @Test
    fun `suggests a lowercase id for a single-word name`() {
        assertEquals(EntityId("database"), suggestEntityId("Database", Diagram()))
    }

    @Test
    fun `appends a numeric suffix when the suggested id is already taken`() {
        val diagram = Diagram(entities = listOf(Entity(EntityId("web"), "Web", EntityType.CONTAINER)))

        assertEquals(EntityId("web2"), suggestEntityId("Web", diagram))
    }

    @Test
    fun `keeps incrementing the suffix until a free id is found`() {
        val diagram =
            Diagram(
                entities =
                    listOf(
                        Entity(EntityId("web"), "Web", EntityType.CONTAINER),
                        Entity(EntityId("web2"), "Web", EntityType.CONTAINER),
                    ),
            )

        assertEquals(EntityId("web3"), suggestEntityId("Web", diagram))
    }

    @Test
    fun `falls back to a stem id when the name has no alphanumeric characters`() {
        assertEquals(EntityId("element"), suggestEntityId("---", Diagram()))
    }

    @Test
    fun `prefixes an underscore when the name starts with a digit`() {
        assertEquals(EntityId("_3dPrinter"), suggestEntityId("3D Printer", Diagram()))
    }

    @Test
    fun `suggests rel1 for the first relationship in an empty diagram`() {
        assertEquals(RelationshipId("rel1"), suggestRelationshipId(Diagram()))
    }

    @Test
    fun `skips already-used relationship ids`() {
        val web = Entity(EntityId("web"), "Web", EntityType.CONTAINER)
        val api = Entity(EntityId("api"), "API", EntityType.CONTAINER)
        val diagram =
            Diagram(
                entities = listOf(web, api),
                relationships = listOf(Relationship(RelationshipId("rel1"), web.id, api.id)),
            )

        assertEquals(RelationshipId("rel2"), suggestRelationshipId(diagram))
    }
}
