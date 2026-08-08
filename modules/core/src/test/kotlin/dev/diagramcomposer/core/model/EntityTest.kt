package dev.diagramcomposer.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class EntityTest {

    private fun sampleEntity() = Entity(
        id = EntityId("payment-api"),
        name = "Payment API",
        type = EntityType.CONTAINER,
        technology = "Spring Boot",
    )

    @Test
    fun `entities with the same field values are equal`() {
        assertEquals(sampleEntity(), sampleEntity())
    }

    @Test
    fun `copy changes only the specified field`() {
        val renamed = sampleEntity().copy(name = "Payments API")

        assertEquals("Payments API", renamed.name)
        assertEquals(sampleEntity().id, renamed.id)
        assertEquals(sampleEntity().type, renamed.type)
        assertEquals(sampleEntity().technology, renamed.technology)
    }

    @Test
    fun `optional fields default sensibly`() {
        val minimal = Entity(id = EntityId("user"), name = "Customer", type = EntityType.PERSON)

        assertEquals(null, minimal.description)
        assertEquals(null, minimal.technology)
        assertEquals(emptyList<String>(), minimal.tags)
        assertEquals(emptyMap<String, String>(), minimal.properties)
        assertEquals(false, minimal.external)
    }

    @Test
    fun `external can be set independently of type`() {
        val entity = sampleEntity().copy(external = true)

        assertEquals(true, entity.external)
        assertEquals(sampleEntity().type, entity.type)
    }

    @Test
    fun `blank name is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Entity(id = EntityId("user"), name = " ", type = EntityType.PERSON)
        }
    }

    @Test
    fun `blank name is rejected on copy`() {
        assertThrows(IllegalArgumentException::class.java) {
            sampleEntity().copy(name = "")
        }
    }
}
