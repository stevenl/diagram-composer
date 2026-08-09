package dev.diagramcomposer.core.validation

import dev.diagramcomposer.core.command.AddEntityCommand
import dev.diagramcomposer.core.command.RemoveEntityCommand
import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import dev.diagramcomposer.core.model.Relationship
import dev.diagramcomposer.core.model.RelationshipId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiagramValidatorTest {
    private val webApp = Entity(id = EntityId("web"), name = "Web App", type = EntityType.CONTAINER)
    private val paymentApi = Entity(id = EntityId("payment-api"), name = "Payment API", type = EntityType.CONTAINER)

    @Test
    fun `a candidate that constructs successfully is valid`() {
        val result = DiagramValidator.validate { Diagram(entities = listOf(webApp)) }

        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `a candidate that violates a Diagram invariant is invalid, with the invariant's message`() {
        val danglingRelationship =
            Relationship(id = RelationshipId("rel-1"), sourceId = webApp.id, targetId = paymentApi.id)

        val result =
            DiagramValidator.validate {
                Diagram(entities = listOf(webApp), relationships = listOf(danglingRelationship))
            }

        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).reason.contains(paymentApi.id.toString()))
    }

    @Test
    fun `previewing a command's effect does not commit it to the diagram passed in`() {
        val diagram = Diagram()

        val result = DiagramValidator.validate { AddEntityCommand(webApp).execute(diagram) }

        assertEquals(ValidationResult.Valid, result)
        assertEquals(Diagram(), diagram) // the original diagram value is untouched
    }

    @Test
    fun `previewing a command that would fail reports why`() {
        val diagram = Diagram()

        val result = DiagramValidator.validate { RemoveEntityCommand(webApp.id).execute(diagram) }

        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `an unrelated exception type is not caught`() {
        assertTrue(
            runCatching {
                DiagramValidator.validate { throw IllegalStateException("not a Diagram invariant") }
            }.exceptionOrNull() is IllegalStateException,
        )
    }
}
