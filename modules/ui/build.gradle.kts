// ui: Compose Multiplatform UI, embedded into the IntelliJ tool window by
// intellij-plugin. The reusable view models/composables
// (dev.diagramcomposer.ui, dev.diagramcomposer.ui.state,
// dev.diagramcomposer.ui.components) talk to `core` only, never to
// adapter-api or adapter-plantuml-c4 directly (docs/architecture.md §3.2).
//
// The adapter-api/adapter-plantuml-c4 dependencies below exist solely for
// dev.diagramcomposer.ui.preview.PreviewApp — the Milestone 7 task 4 manual-
// testing entry point, which needs a real DiagramSession/DiagramAdapter to
// have something to display. See PreviewApp.kt's doc comment for the full
// rationale for this scoped, documented deviation.

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":modules:core"))

    // Scoped to dev.diagramcomposer.ui.preview.PreviewApp only — see header comment.
    implementation(project(":modules:adapter-api"))
    implementation(project(":modules:adapter-plantuml-c4"))

    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material3)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

// Lets `./gradlew :modules:ui:run` launch the Milestone 7 manual-testing
// preview directly (docs/implementation-plan.md Milestone 7 task 4).
compose.desktop {
    application {
        mainClass = "dev.diagramcomposer.ui.preview.PreviewAppKt"
    }
}
