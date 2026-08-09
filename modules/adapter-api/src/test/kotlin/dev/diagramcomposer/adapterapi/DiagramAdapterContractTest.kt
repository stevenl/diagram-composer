package dev.diagramcomposer.adapterapi

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Exercises the [DiagramAdapter] contract itself — not any particular
 * language's syntax — via [FakeAdapter]. Real adapters (starting with
 * `adapter-plantuml-c4` in Milestone 3) are expected to satisfy the same
 * behaviours demonstrated here.
 */
class DiagramAdapterContractTest {
    private val adapter: DiagramAdapter = FakeAdapter()

    @Test
    fun `metadata is exposed`() {
        assertEquals("fake", adapter.metadata.languageId)
        assertEquals(listOf(".fake"), adapter.metadata.fileExtensions)
    }

    @Test
    fun `parsing empty source succeeds with an empty diagram`() {
        val result = adapter.parse("")

        assertTrue(result is ParseResult.Success)
        assertEquals(Diagram(), (result as ParseResult.Success).diagram)
    }

    @Test
    fun `parsing valid source succeeds with the expected diagram`() {
        val result = adapter.parse("ENTITY api Payment API\nENTITY db Payment DB")

        assertTrue(result is ParseResult.Success)
        val diagram = (result as ParseResult.Success).diagram
        assertEquals(
            listOf(
                Entity(id = EntityId("api"), name = "Payment API", type = EntityType.SYSTEM),
                Entity(id = EntityId("db"), name = "Payment DB", type = EntityType.SYSTEM),
            ),
            diagram.entities,
        )
    }

    @Test
    fun `parsing malformed source fails with a non-empty list of errors`() {
        val result = adapter.parse("not a valid line")

        assertTrue(result is ParseResult.Failure)
        val errors = (result as ParseResult.Failure).errors
        assertTrue(errors.isNotEmpty())
        assertEquals(1, errors.first().line)
    }

    @Test
    fun `parse failure is propagated without partial success`() {
        val result = adapter.parse(FakeAdapter.FAILING_SOURCE)

        assertTrue(result is ParseResult.Failure)
    }

    @Test
    fun `generate produces source that reparses to an equivalent diagram`() {
        val diagram =
            Diagram(
                entities =
                    listOf(
                        Entity(id = EntityId("api"), name = "Payment API", type = EntityType.SYSTEM),
                    ),
            )

        val source = adapter.generate(diagram)
        val reparsed = adapter.parse(source)

        assertTrue(reparsed is ParseResult.Success)
        assertEquals(diagram, (reparsed as ParseResult.Success).diagram)
    }

    @Test
    fun `parse then generate round-trips to equivalent source`() {
        val source = "ENTITY api Payment API\nENTITY db Payment DB"

        val diagram = (adapter.parse(source) as ParseResult.Success).diagram
        val regenerated = adapter.generate(diagram)

        assertEquals(source, regenerated)
    }
}
