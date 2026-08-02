// adapter-plantuml-c4: the first concrete DiagramAdapter implementation.
// No parsing/generation logic yet — Milestone 3/4 scope. This is scaffolding only.

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":modules:core"))
    implementation(project(":modules:adapter-api"))

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
