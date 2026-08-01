import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

// Root settings for the Diagram Composer multi-module build.
// Milestone 0 scope: wiring only, no build logic beyond dependency resolution.

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Required so the intellij-platform plugin can contribute its own
    // repositories (IntelliJ SDK artifacts) via dependencyResolutionManagement.
    //
    // NOTE: this version is duplicated in gradle/libs.versions.toml
    // (intellij-platform-gradle-plugin) because this settings-level plugins
    // block runs before the version catalog is available. Keep both in sync
    // — a mismatch causes "plugin already on the classpath with an unknown
    // version" errors.
    id("org.jetbrains.intellij.platform.settings") version "2.18.1"

    // Enables auto-provisioning for Gradle's Daemon JVM criteria
    // (gradle/gradle-daemon-jvm.properties, generated via
    // `./gradlew updateDaemonJvm --jvm-version=21`). This is what pins the
    // JVM that runs the Gradle Daemon itself — distinct from and in addition
    // to the per-module `kotlin { jvmToolchain(21) }` blocks, which only pin
    // the *compilation target* JDK. Without this resolver, updateDaemonJvm
    // can still pin a version, but can't bake in download URLs for
    // contributors who don't already have a matching JDK installed.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()
        google()

        intellijPlatform {
            defaultRepositories()
        }
    }
}

rootProject.name = "diagram-composer"

include(
    ":modules:core",
    ":modules:adapter-api",
    ":modules:adapter-plantuml-c4",
    ":modules:ui",
    ":modules:intellij-plugin",
)
