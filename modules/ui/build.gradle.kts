// ui: Compose Multiplatform UI, embedded into the IntelliJ tool window by
// intellij-plugin. Language-independent — talks to `core` only, never to
// adapter-api or adapter-plantuml-c4 directly (docs/architecture.md §3).

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

    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material3)
}
