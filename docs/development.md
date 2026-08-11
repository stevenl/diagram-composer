# Development Specification

## 1. Overview

This document defines the development practices, contribution workflow, project organisation, and engineering standards for **Diagram Composer**.

The project is designed as an open-source IntelliJ IDEA plugin and is expected to be developed significantly with the assistance of AI coding tools.

The development process must ensure that AI-assisted development produces maintainable, tested, and reviewable code.

---

# 2. Development Principles

## 2.1 Tests Accompany Code Changes

Every code change must include appropriate tests.

The project follows the principle:

> A feature is not complete until its automated tests are included.

Examples:

| Change                 | Required Tests       |
|------------------------|----------------------|
| New model class        | Unit tests           |
| New command            | Execute/undo tests   |
| New parser behaviour   | Parser tests         |
| New adapter capability | Adapter tests        |
| New UI workflow        | UI/integration tests |
| Bug fix                | Regression test      |

A pull request without tests should be considered incomplete unless there is a documented reason.

---

# 2.2 AI-Assisted Development

Diagram Composer is expected to be developed using AI coding assistants.

AI tools may be used for:

* Generating implementation code.
* Creating tests.
* Refactoring.
* Documentation.
* Exploring design alternatives.

However:

> AI-generated code must meet the same quality standards as manually written code.

The developer remains responsible for:

* Understanding changes.
* Reviewing generated code.
* Ensuring tests are meaningful.
* Maintaining architecture consistency.

---

# 2.3 Small, Reviewable Changes

Changes should be broken into small units.

Preferred:

```
Add Container creation command

+ implementation
+ unit tests
+ documentation update
```

Avoid:

```
Rewrite diagram editor architecture

+ 5000 lines changed
+ unclear test coverage
```

---

# 3. Repository Structure

The module layout (`core/`, `ui/`, `adapter-api/`, `adapter-plantuml-c4/`, `adapter-mermaid-flowchart/`, `intellij-plugin/`) is defined authoritatively in `docs/architecture.md` Section 3. All of these modules live under a top-level `modules/` directory, so they are visually distinguishable from `docs/` and other repository-level directories. Gradle module coordinates match the folder path exactly (e.g. `:modules:core`). Repository-level directories alongside `modules/`:

```
diagram-composer/
├── modules/
│   ├── core/
│   ├── ui/
│   ├── adapter-api/
│   ├── adapter-plantuml-c4/
│   ├── adapter-mermaid-flowchart/
│   └── intellij-plugin/
├── docs/
└── examples/
```

There is no separate top-level `tests/` or `model/` directory — see `docs/engineering.md` Section 3.

---

# 4. Branching Strategy

Recommended approach:

```
main
├── feature/create-container
├── fix/parser-error
└── docs/update-adapter-spec
```

Branches should represent a single logical change.

---

# 5. Commit Guidelines

Commits should be:

* Small.
* Focused.
* Descriptive.

## 5.1 Conventional Commits (required)

Commit messages **must** follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

This is not just a style preference: the release process (§19) parses these
messages to decide whether a release is a major/minor/patch bump and to
generate the changelog. A malformed commit message doesn't just look messy —
it produces a wrong or missing version bump.

Common types:

| Type       | Meaning                                      | Triggers a release?  |
|------------|----------------------------------------------|----------------------|
| `feat`     | New feature                                  | Minor bump           |
| `fix`      | Bug fix                                      | Patch bump           |
| `docs`     | Documentation only                           | No                   |
| `test`     | Adding/correcting tests                      | No                   |
| `refactor` | Code change that neither fixes nor adds      | No                   |
| `chore`    | Build process, tooling, dependency bumps     | No                   |

A breaking change is indicated either with a `!` after the type/scope
(`feat!: ...`) or a `BREAKING CHANGE:` footer, and always triggers a major
bump.

Preferred:

```
feat(adapter-plantuml-c4): add Container parser support

fix(core): prevent duplicate ids when duplicating an element
```

Avoid:

```
Various fixes
Add PlantUML Container parser support
```

Commit messages are linted automatically in CI (`.github/workflows/ci.yml`)
via `commitlint`, configured in `.commitlintrc.json`. A pull request with a
non-conforming commit message will fail that check.

---

# 6. Pull Request Requirements

Every pull request should include:

## Description

Explain:

* What changed.
* Why it changed.
* How it was tested.

---

## Tests

State:

* Tests added.
* Tests updated.
* Tests executed.

---

## Documentation

Update documentation when changing:

* Architecture.
* Public APIs.
* User workflows.
* Adapter behaviour.

---

# 7. AI Development Workflow

The recommended workflow for AI-assisted development:

```
1. Define requirement

      ↓

2. Create/update specification

      ↓

3. Ask AI to propose implementation

      ↓

4. Review design

      ↓

5. Generate code

      ↓

6. Generate tests

      ↓

7. Run tests

      ↓

8. Review changes

      ↓

9. Commit
```

---

# 8. AI Coding Guidelines

AI-generated code should:

* Follow existing architecture.
* Avoid unnecessary abstractions.
* Include tests.
* Include documentation where appropriate.
* Prefer simple solutions.
* Avoid introducing dependencies without justification.

---

# 9. Specification-Driven Development

Development should begin from specifications.

The preferred workflow:

```
Specification

    ↓

Acceptance Criteria

    ↓

Implementation

    ↓

Tests

    ↓

Review
```

The specifications are the source of truth for intended behaviour.

---

# 10. Testing Strategy

## 10.1 Test Pyramid

The project follows:

```
            UI Tests
             
                ▲

        Integration Tests

                ▲

           Unit Tests
```

Most tests should be unit tests.

---

# 11. Unit Testing

Required for:

* Core model.
* Commands.
* Validation.
* Parsers.
* Generators.
* Utilities.

Example:

```
CreateContainerCommandTest

RenameEntityCommandTest

PlantUmlParserTest
```

---

# 12. Adapter Testing

Every adapter requires comprehensive tests.

## Parser Tests

Example:

Input:

```plantuml
Container(api,"API")
```

Expected:

```
Entity(
    id="api",
    type=Container
)
```

---

## Generator Tests

Example:

Model:

```
Container(api)
```

Expected:

```plantuml
Container(api,"API")
```

---

## Round Trip Tests

Every adapter must test:

```
Source

  ↓

Parser

  ↓

Model

  ↓

Generator

  ↓

Source
```

The result should be semantically equivalent.

---

# 13. Regression Testing

Every bug fix requires a regression test.

Example:

Bug:

```
Parser fails when relationship has spaces.
```

Fix:

```
Add parser support.

Add regression test.

Prevent recurrence.
```

---

# 14. UI Testing

UI tests should cover important user workflows.

Examples:

## Create Entity

```
Open diagram

Click Container

Create entity

Verify source updated
```

---

## Create Relationship

```
Select relationship tool

Select source

Select destination

Verify generated syntax
```

---

## Undo

```
Create element

Undo

Verify removed

Redo

Verify restored
```

---

# 15. Code Quality Standards

## Kotlin Style

Use:

* Kotlin official style.
* Idiomatic Kotlin.
* Null safety.
* Immutable data structures where practical.

---

## Avoid

Avoid:

* Large classes.
* Hidden state.
* Global mutable variables.
* UI logic in domain classes.
* Language-specific code in the core module.

---

# 16. Dependency Management

New dependencies require justification.

Before adding a dependency, consider:

* Maintenance status.
* License compatibility.
* Security.
* Long-term value.

Prefer:

* Kotlin standard library.
* IntelliJ APIs.
* Existing project dependencies.

---

# 17. Documentation Requirements

Documentation must be updated when changing:

## Architecture

Update:

```
docs/architecture.md
```

---

## User Features

Update:

```
docs/product.md
docs/ui.md
```

---

## Adapter Behaviour

Update:

```
docs/adapters.md
```

---

## Development Process

Update:

```
docs/development.md
```

---

# 18. Continuous Integration

Implemented in `.github/workflows/ci.yml`, triggered on every push and every
pull request. It has no release side effects (see §19 for the separate
release workflow).

## Build & test

```
./gradlew build test
```

---

## Commit message lint

Every commit is checked against Conventional Commits (§5.1) using
`commitlint`, configured in `.commitlintrc.json`.

---

## Kotlin Formatting

```
./gradlew ktlintCheck
```

Enforced via the `org.jlleitschuh.gradle.ktlint` plugin, applied to every
module from the root `build.gradle.kts`. The enforced style is
`ktlint_official` (matching `kotlin.code.style=official` in
`gradle.properties`, so the ktlint and IntelliJ formatters agree), pinned
explicitly in the root `.editorconfig` rather than left as an implicit
plugin default. `.editorconfig` also sets
`ktlint_function_naming_ignore_when_annotated_with = Composable`, so
`standard:function-naming` allows PascalCase `@Composable` functions —
the same exception as detekt's `FunctionNaming` override below, kept in
the same file since it's ktlint's supported mechanism for this rather than
a project-specific rule.

Run `./gradlew ktlintFormat` locally to auto-fix violations before
committing. If this fails with a configuration-cache serialization error
mentioning `DefaultProject` on a Kotlin-script task (`gradle.properties`
sets `org.gradle.configuration-cache=true`), it's a known open ktlint-gradle
issue scoped to `ktlintFormat`'s `.kts`-linting task
(JLLeitschuh/ktlint-gradle#936) — CI's `ktlintCheck` isn't known to be
affected. Work around it locally with
`./gradlew ktlintFormat --no-configuration-cache`.

---

## Static Analysis

```
./gradlew detekt
```

Enforced via the `io.gitlab.arturbosch.detekt` plugin, applied to every
module from the root `build.gradle.kts`. Runs with `buildUponDefaultConfig
= true`, plus one project-specific override in `config/detekt/detekt.yml`:
`FunctionNaming` ignores `@Composable`-annotated functions, so composables
follow the Compose API guideline of PascalCase (consistent with the
library's own `Text`, `Column`, `AlertDialog`, etc.) instead of detekt's
default lowerCamelCase. Every other rule is still the unmodified default.

Dependency vulnerability checks are not yet enabled; still a candidate for
future consideration.

---

## 18.1 Branch Protection (one-time manual setup)

CI enforcement only has teeth if `main` is protected. This is a repository
setting, not something a workflow file can configure, so it must be set up
once, manually, in GitHub: **Settings → Branches → Branch protection rules**
for `main`.

Required:

* "Require status checks to pass before merging", with **both** the
  `Build & test` and `Commit message lint` jobs from `ci.yml` selected as
  required checks. Kotlin formatting and static analysis run as steps
  inside the `Build & test` job rather than as separate jobs, so a
  `ktlintCheck` or `detekt` failure already fails that required check —
  no extra job needs selecting.
* "Require branches to be up to date before merging" (recommended, so a
  stale branch can't merge around a check that would now fail).

Without this, `ci.yml` still runs and reports failures, but nothing stops a
failing PR from being merged anyway.

---

# 19. Release Process

Releases are automated by `.github/workflows/release.yml`, using
[release-please](https://github.com/googleapis/release-please), driven
entirely by the Conventional Commits (§5.1) merged to `main`.

## 19.1 How it works

1. Every push to `main` runs `release-please-action`, which scans commits
   since the last release tag.
2. If there are releasable commits (`feat`, `fix`, or anything with a
   breaking-change marker), it opens/updates a standing **release PR**
   containing:
    * the computed next version, written into the root `gradle.properties`
      (`version=...`, consumed automatically by Gradle — see
      `build.gradle.kts`),
    * a generated `CHANGELOG.md` entry.
3. That PR is reviewed like any other change (it's a normal PR against
   `main`, subject to the same branch protection in §18.1).
4. Merging it is itself a push to `main`, which triggers the workflow again.
   This time release-please recognizes its own release PR was merged, and:
    * creates a git tag for the new version,
    * publishes a GitHub Release with the generated changelog.
5. A second job in the same workflow, gated on a release having just been
   created, then runs `./gradlew :modules:intellij-plugin:buildPlugin` and
   uploads the resulting plugin zip to that GitHub Release as an artifact.

## 19.2 What this does *not* do

Publishing to the JetBrains Marketplace is a separate, deliberately
unaddressed step — see `docs/implementation-plan.md` Milestone 12. Until
then, the plugin zip attached to each GitHub Release is the only
distribution channel.

## 19.3 Configuration

* `release-please-config.json` — release strategy and the `gradle.properties`
  version-file mapping.
* `.release-please-manifest.json` — release-please's record of the last
  released version per package (this repo has one package: `.`).

---

# 20. Example Development Task

Example issue:

```
Add support for Container creation in PlantUML C4 adapter
```

Expected implementation:

```
Update adapter capability model

+

Add Container element support

+

Add parser tests

+

Add generator tests

+

Add UI action

+

Add UI test

+

Update documentation
```

---

# 21. Open Source Contribution Model

Contributors should be able to work independently.

The project should provide:

* Clear architecture.
* Good examples.
* Test expectations.
* Development setup instructions.

---

# 22. AI Contribution Guidelines

AI-assisted contributions are welcome.

However:

A contribution generated by AI is still expected to:

* Follow project architecture.
* Include tests.
* Have human review.
* Avoid unnecessary complexity.

---

# 23. Future Development Improvements

Potential future additions:

* AI coding agent instructions file.
* Automated architecture checks.
* Generated documentation from specifications.
* Contributor onboarding tasks.
* Example adapter implementation.

---

# 24. Summary

Diagram Composer development is based on:

* Specification-driven development.
* Kotlin-first implementation.
* Test-driven changes.
* AI-assisted development.
* Small reviewable commits.
* Open-source collaboration.

The core development rule is:

> Every behaviour change must include the implementation, the tests proving it works, and the documentation explaining why it exists.
