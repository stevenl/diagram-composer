package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Parsing of entity/relationship properties
 * (docs/adapters.md §15 "Supported Properties"), including PlantUML C4's
 * `$name=value` named-argument style for optional parameters.
 */
class PropertyParsingTest {
    private val adapter = PlantUmlC4Adapter()

    private fun parseSuccessfully(source: String) =
        (adapter.parse(source) as? ParseResult.Success)?.diagram
            ?: error("expected ParseResult.Success, got ${adapter.parse(source)}")

    @Test
    fun `named $descr and $techn override or supply positional values`() {
        val diagram =
            parseSuccessfully(
                """System(banking, "Internet Banking System", ${'$'}descr="Named description", ${'$'}techn="Kotlin")""",
            )

        val entity = diagram.entities.single()
        assertEquals("Named description", entity.description)
        assertEquals("Kotlin", entity.technology)
    }

    @Test
    fun `named $tags is split into a list of tags`() {
        val diagram =
            parseSuccessfully(
                """System(banking, "Internet Banking System", ${'$'}tags="internal,critical")""",
            )

        assertEquals(listOf("internal", "critical"), diagram.entities.single().tags)
    }

    @Test
    fun `entity without $tags has an empty tags list`() {
        val diagram = parseSuccessfully("""System(banking, "Internet Banking System")""")

        assertEquals(emptyList<String>(), diagram.entities.single().tags)
    }

    @Test
    fun `unrecognized named arguments are preserved in the properties map`() {
        val diagram =
            parseSuccessfully(
                """System(banking, "Internet Banking System", ${'$'}sprite="server")""",
            )

        assertEquals("server", diagram.entities.single().properties["sprite"])
    }

    @Test
    fun `relationship named $descr and $techn override positional values`() {
        val diagram =
            parseSuccessfully(
                """
                Person(customer, "Customer")
                System(banking, "Internet Banking System")
                Rel(customer, banking, "positional label", ${'$'}descr="Named description", ${'$'}techn="HTTPS")
                """.trimIndent(),
            )

        val relationship = diagram.relationships.single()
        assertEquals("Named description", relationship.description)
        assertEquals("HTTPS", relationship.technology)
    }

    @Test
    fun `a label containing an escaped quote is parsed correctly`() {
        val diagram = parseSuccessfully("""System(banking, "The \"Banking\" System")""")

        assertEquals("""The "Banking" System""", diagram.entities.single().name)
    }

    @Test
    fun `a quoted value containing a comma is not split into multiple arguments`() {
        val diagram =
            parseSuccessfully(
                """Container(web_app, "Web Application", "Java, Spring MVC")""",
            )

        assertEquals("Java, Spring MVC", diagram.entities.single().technology)
    }
}
