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

The module layout (`core/`, `ui/`, `adapter-api/`, `adapter-plantuml-c4/`, `adapter-mermaid/`, `intellij-plugin/`) is defined authoritatively in `docs/architecture.md` Section 3. All of these modules live under a top-level `modules/` directory, so they are visually distinguishable from `docs/` and other repository-level directories. Gradle module coordinates match the folder path exactly (e.g. `:modules:core`). Repository-level directories alongside `modules/`:

```
diagram-composer/

├── modules/

│   ├── core/

│   ├── ui/

│   ├── adapter-api/

│   ├── adapter-plantuml-c4/

│   ├── adapter-mermaid/

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
 |
 +-- feature/create-container
 |
 +-- fix/parser-error
 |
 +-- docs/update-adapter-spec
```

Branches should represent a single logical change.

---

# 5. Commit Guidelines

Commits should be:

* Small.
* Focused.
* Descriptive.

Preferred:

```
Add PlantUML Container parser support
```

Avoid:

```
Various fixes
```

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

RenameElementCommandTest

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
Element(
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

## Create Element

```
Open diagram

Click Container

Create element

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

CI should automatically run:

## Build

```
./gradlew build
```

---

## Tests

```
./gradlew test
```

---

## Static Analysis

Examples:

* Kotlin compiler checks.
* Detekt (optional).
* Dependency checks.

---

# 19. Release Process

A release should include:

* Passing CI.
* Updated changelog.
* Updated version.
* Documentation review.

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
