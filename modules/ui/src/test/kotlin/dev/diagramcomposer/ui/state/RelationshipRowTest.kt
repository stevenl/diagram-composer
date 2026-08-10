package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipId
import dev.diagramcomposer.core.model.RelationshipType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RelationshipRowTest {
    private val web = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val api = Entity(id = EntityId("api"), name = "API", type = EntityType.CONTAINER)

    @Test
    fun `an empty diagram has no relationship rows`() {
        assertEquals(emptyList<RelationshipRow>(), buildRelationshipRows(Diagram()))
    }

    @Test
    fun `entity ids are resolved to names, in declaration order`() {
        val relationship =
            Relationship(
                id = RelationshipId("rel-1"),
                sourceId = web.id,
                targetId = api.id,
                description = "Calls",
                technology = "JSON/HTTPS",
                type = RelationshipType.BIDIRECTIONAL,
            )
        val diagram = Diagram(entities = listOf(web, api), relationships = listOf(relationship))

        assertEquals(
            listOf(
                RelationshipRow(
                    id = relationship.id,
                    sourceName = "Web App",
                    targetName = "API",
                    description = "Calls",
                    technology = "JSON/HTTPS",
                    type = RelationshipType.BIDIRECTIONAL,
                ),
            ),
            buildRelationshipRows(diagram),
        )
    }
}
