// adapter-api: the contract every language adapter (PlantUML, later Mermaid)
// must implement. Depends only on core's domain model types (docs/adapters.md).

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":modules:core"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
