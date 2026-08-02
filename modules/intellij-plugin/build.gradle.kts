// intellij-plugin: IntelliJ Platform integration shell. Depends on everything
// else (ui, core) but contains no editor/domain logic itself
// (docs/architecture.md §3). Milestone 0 scope: empty plugin.xml that builds.

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("org.jetbrains.intellij.platform")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":modules:core"))
    implementation(project(":modules:ui"))

    intellijPlatform {
        create("IC", "2024.3")
        // instrumentationTools() was removed from the IntelliJ Platform
        // Gradle Plugin in later 2.x releases — it's no longer required.
    }
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
