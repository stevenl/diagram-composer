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
    id("org.jetbrains.intellij.platform.settings") version "2.1.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

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
