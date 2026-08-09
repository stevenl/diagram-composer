// adapter-api: the contract every language adapter (PlantUML, later Mermaid)
// must implement, plus DiagramSession, the core<->adapter coordinator
// (docs/implementation-plan.md Milestone 6). Depends only on core's domain
// model and command types (docs/adapters.md).

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":modules:core"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
