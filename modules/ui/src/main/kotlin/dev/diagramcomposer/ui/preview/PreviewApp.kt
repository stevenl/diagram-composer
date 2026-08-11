package dev.diagramcomposer.ui.preview

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.diagramcomposer.adapter.plantumlc4.PlantUmlC4Adapter
import dev.diagramcomposer.adapterapi.DiagramSession
import dev.diagramcomposer.adapterapi.OpenResult
import dev.diagramcomposer.ui.DiagramComposerApp
import dev.diagramcomposer.ui.state.DiagramViewModel

/**
 * Manual-testing entry point for Milestone 7 task 4
 * (docs/implementation-plan.md): opens a sample PlantUML C4 source with
 * [PlantUmlC4Adapter], wraps the resulting `Diagram` in a read-only
 * [DiagramViewModel], and renders [DiagramComposerApp] in a plain Compose
 * desktop window — the "runnable Compose desktop preview" the task
 * describes.
 *
 * This is the only file in `ui` that imports `adapter-api` or
 * `adapter-plantuml-c4`. Everything else in this module (the view models in
 * `dev.diagramcomposer.ui.state`, the composables in
 * `dev.diagramcomposer.ui`/`dev.diagramcomposer.ui.components`) imports
 * only `core`, preserving "the UI communicates only with the core model"
 * (docs/architecture.md §3.2) for the parts of this module `intellij-plugin`
 * will eventually reuse. See `modules/ui/build.gradle.kts` for why the
 * dependency exists at the module level at all, scoped to this preview.
 *
 * Run via `./gradlew :modules:ui:run`.
 */

private val sampleSource =
    """
    @startuml
    !include <C4/C4_Container>

    Person(customer, "Customer", "A customer of the shop")
    System_Boundary(shop, "Online Shop") {
      Container(web, "Web Application", "Kotlin/Compose", "Lets customers browse and buy products")
      Container(api, "API", "Kotlin", "Handles business logic")
      ContainerDb(db, "Database", "PostgreSQL", "Stores orders and products")
    }

    Rel(customer, web, "Uses", "HTTPS")
    Rel(web, api, "Calls", "JSON/HTTPS")
    Rel(api, db, "Reads from and writes to", "JDBC")
    @enduml
    """.trimIndent()

fun main() {
    val session =
        when (val result = DiagramSession.open(sampleSource, PlantUmlC4Adapter())) {
            is OpenResult.Success -> result.session
            is OpenResult.Failure -> error("Sample PlantUML source failed to parse: ${result.errors}")
        }

    val viewModel = DiagramViewModel(session.diagram)

    application {
        Window(onCloseRequest = ::exitApplication, title = "Diagram Composer — Preview") {
            DiagramComposerApp(viewModel)
        }
    }
}
