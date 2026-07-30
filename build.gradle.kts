// Root build script for Diagram Composer.
// Declares plugin versions once (via the version catalog) and applies
// bare minimum shared configuration. Each module's own build.gradle.kts
// owns its dependencies and behaviour — see docs/engineering.md §2.

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.intellij.platform) apply false
}

allprojects {
    group = "dev.diagramcomposer"
    version = "0.1.0-SNAPSHOT"
}
