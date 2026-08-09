package dev.diagramcomposer.adapter.plantumlc4

import dev.diagramcomposer.adapterapi.ParseResult
import dev.diagramcomposer.core.model.Diagram
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * `source -> Diagram -> source -> Diagram` round-trip stability
 * (docs/adapters.md §10, §20 "Round-trip" — the *second* model must be
 * semantically equivalent to the first; the regenerated source is not
 * expected to be byte-identical to the original, per
 * docs/implementation-plan.md Milestone 4 task 5).
 *
 * Each case parses hand-written source, regenerates source from the
 * resulting [Diagram], reparses that, and asserts the two [Diagram]s are
 * equal — [Diagram] and its constituent types are structural data classes,
 * so `==` here is exactly "semantically equivalent".
 */
class SourceRoundTripTest {
    private val adapter = PlantUmlC4Adapter()

    private fun assertRoundTrips(source: String) {
        val firstDiagram = parseSuccessfully(source)
        val regenerated = adapter.generate(firstDiagram)
        val secondDiagram = parseSuccessfully(regenerated)

        assertEquals(firstDiagram, secondDiagram)
    }

    private fun parseSuccessfully(source: String): Diagram {
        val result = adapter.parse(source)
        assertTrue(result is ParseResult.Success) { "expected success, got $result" }
        return (result as ParseResult.Success).diagram
    }

    @Test
    fun `a single entity round-trips`() {
        assertRoundTrips("""Person(customer, "Customer")""")
    }

    @Test
    fun `entities with descriptions and technology round-trip`() {
        assertRoundTrips(
            """
            Person(customer, "Customer", "A retail bank customer.")
            Container(web_app, "Web Application", "Java, Spring MVC", "Delivers the SPA.")
            """.trimIndent(),
        )
    }

    @Test
    fun `entities and relationships round-trip`() {
        assertRoundTrips(
            """
            Person(customer, "Customer")
            System(banking, "Internet Banking System")
            Rel(customer, banking, "Uses", "HTTPS")
            BiRel(banking, customer, "Notifies")
            """.trimIndent(),
        )
    }

    @Test
    fun `directional relationships round-trip`() {
        assertRoundTrips(
            """
            Person(customer, "Customer")
            System(banking, "Internet Banking System")
            Rel_U(customer, banking, "Uses")
            Rel_D(customer, banking, "Uses")
            Rel_L(customer, banking, "Uses")
            Rel_R(customer, banking, "Uses")
            """.trimIndent(),
        )
    }

    @Test
    fun `nested boundaries round-trip`() {
        assertRoundTrips(
            """
            System_Boundary(outer, "Outer") {
                Container_Boundary(inner, "Inner") {
                    Component(controller, "Controller")
                }
                Container(web_app, "Web Application")
            }
            """.trimIndent(),
        )
    }

    @Test
    fun `tags and unrecognized named arguments round-trip`() {
        assertRoundTrips(
            """
            System(banking, "Internet Banking System", ${'$'}descr="Lets customers view accounts.", ${'$'}tags="internal,critical", ${'$'}sprite="server")
            """.trimIndent(),
        )
    }

    @Test
    fun `a top-level entity declared after a boundary round-trips`() {
        // Regression test: the generator used to emit all top-level entities
        // before all top-level boundaries, regardless of source order. That
        // reorders `banking_web` ahead of the boundary on reparse, so
        // Diagram.entities (a plain, order-sensitive List) would no longer
        // equal the original — even though nothing about the diagram's
        // *content* changed. See Diagram.rootChildren.
        assertRoundTrips(
            """
            Person(customer, "Customer")
            System_Boundary(banking, "Internet Banking System") {
                Container(web_app, "Web Application")
            }
            System_Ext(banking_web, "Some External System")
            """.trimIndent(),
        )
    }

    @Test
    fun `a representative full diagram round-trips`() {
        assertRoundTrips(
            """
            @startuml
            !include <C4/C4_Container>

            Person(customer, "Personal Banking Customer", "A customer of the bank.")

            System_Boundary(banking, "Internet Banking System") {
                Container(spa, "Single-Page App", "JavaScript, Angular", "Provides internet banking functionality.")
                Container(web_app, "Web Application", "Java, Spring MVC", "Delivers content and the SPA.")
                ContainerDb(database, "Database", "PostgreSQL", "Stores accounts and audit data.")

                Container_Boundary(api, "API") {
                    Component(accounts_ctrl, "Accounts Controller", "Spring MVC Controller")
                }
            }

            System_Ext(email_system, "E-Mail System", "The internal Microsoft Exchange system.")

            Rel(customer, spa, "Uses", "HTTPS")
            Rel(spa, web_app, "Makes API calls to", "JSON/HTTPS")
            Rel_D(web_app, database, "Reads from and writes to", "JDBC")
            BiRel(web_app, email_system, "Sends e-mails using")
            @enduml
            """.trimIndent(),
        )
    }
}
