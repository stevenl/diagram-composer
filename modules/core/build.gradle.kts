// core: language-independent domain model + editing commands.
// Must stay free of adapter-api, adapter-plantuml-c4, and IntelliJ dependencies
// (docs/architecture.md §3, ai-context.md §5).

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
