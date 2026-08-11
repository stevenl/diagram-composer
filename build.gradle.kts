// Root build script for Diagram Composer.
// Declares plugin versions once (via the version catalog) and applies
// bare minimum shared configuration. Each module's own build.gradle.kts
// owns its dependencies and behaviour — see docs/engineering.md §2.

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.compose) apply false
}

// group and version come from root gradle.properties, which Gradle applies
// to the root project automatically. Propagate explicitly to subprojects
// (Gradle does not do this implicitly for `version`) rather than hardcoding
// them here, since release.yml (docs/development.md §19) rewrites
// gradle.properties on every release.
allprojects {
    group = rootProject.group
    version = rootProject.version

    pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    // ktlintCheck is wired into `check` (and therefore `build`) by the
    // plugin automatically. Style rules themselves live in the root
    // .editorconfig (ktlint_official, per gradle.properties'
    // kotlin.code.style=official) rather than here, so there's a single
    // place to look for what's enforced. CI runs this via `ktlintCheck`
    // (docs/development.md §18) — see .github/workflows/ci.yml.
    configure<KtlintExtension> {
        verbose.set(true)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
        }
    }

    // Custom rule set file: only overrides FunctionNaming, to allow
    // PascalCase @Composable functions (Compose API guideline,
    // https://developer.android.com/develop/ui/compose/api-guidelines#naming-unit)
    // — consistent with the library's own composables (Text, Column, ...).
    // buildUponDefaultConfig = true means every other default rule still
    // applies as-is (docs/development.md §18).
    configure<DetektExtension> {
        buildUponDefaultConfig = true
        autoCorrect = false
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }
}

// The detekt plugin's own jvmTarget default doesn't read the per-module
// `kotlin { jvmToolchain(21) }` blocks, so it has to be set explicitly here
// or every module's `detekt` task warns about a JVM target mismatch.
subprojects {
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "21"
    }
}
