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

// group and version come from root gradle.properties, which Gradle applies
// to the root project automatically. Propagate explicitly to subprojects
// (Gradle does not do this implicitly for `version`) rather than hardcoding
// them here, since release.yml (docs/development.md §19) rewrites
// gradle.properties on every release.
allprojects {
    group = rootProject.group
    version = rootProject.version
}
