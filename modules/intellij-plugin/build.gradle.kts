// intellij-plugin: IntelliJ Platform integration. Registers
// DiagramComposerEditorProvider (docs/implementation-plan.md Milestone 10),
// a split source/visual FileEditor for PlantUML C4 files, built on `ui`'s
// existing composables/view models (docs/architecture.md §3).
//
// adapter-api/adapter-plantuml-c4 are real dependencies here (unlike `ui`,
// where they're scoped to the single PreviewApp.kt file): this module opens
// real files into a real DiagramSession (DiagramComposerEditorProvider),
// which needs both. Compose Desktop is added directly (not inherited from
// `ui`, which doesn't expose it as an api dependency) so DiagramVisualFileEditor
// can host DiagramComposerApp in a Swing ComposePanel.

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose)
    id("org.jetbrains.intellij.platform")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":modules:core"))
    implementation(project(":modules:ui"))
    implementation(project(":modules:adapter-api"))
    implementation(project(":modules:adapter-plantuml-c4"))

    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material3)

    intellijPlatform {
        create("IC", "2024.3")
        // instrumentationTools() was removed from the IntelliJ Platform
        // Gradle Plugin in later 2.x releases — it's no longer required.
    }

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild.set("243")
            // until-build intentionally left unset per Milestone 0 task 5 —
            // do NOT let the plugin default this to "243.*".
            untilBuild.set(provider { null })
        }
    }
}
