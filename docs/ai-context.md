# Diagram Composer — AI Project Brief

**Purpose:** This document provides the minimum context an AI coding assistant needs to contribute effectively to the Diagram Composer project. It is intended to be included at the beginning of most AI-assisted development sessions.

For complete project details, refer to the full specifications in the `docs` folder.

---

# 1. Project Summary

Diagram Composer is an **IntelliJ IDEA plugin** that helps developers create and maintain **text-based diagrams** through a visual editing experience.

The plugin accelerates authoring while keeping the diagram source as the single source of truth.

Diagram Composer:

* edits diagram models visually
* generates and updates text-based diagram source
* allows direct editing of the source at any time
* relies on existing rendering plugins (it does **not** render diagrams itself)

The first supported language is **PlantUML C4**, with an architecture designed to support additional languages such as Mermaid in the future.

---

# 2. Vision

Diagram Composer aims to become:

> **The best developer-focused visual authoring environment for text-based diagram languages.**

The project values:

* simplicity
* correctness
* readability
* maintainability
* testability
* extensibility
* excellent developer experience

---

# 3. Architecture Summary

The system consists of four major layers:

```text
IntelliJ Platform

        │

Compose UI

        │

Diagram Core Model

        │

Diagram Adapter API

        │

Language Adapter
```

Important rule:

> The editor is language-independent.

PlantUML, Mermaid and future languages must be implemented through adapters.

---

# 4. Technology Choices

| Area         | Technology                      |
|--------------|---------------------------------|
| Language     | Kotlin                          |
| IDE Platform | IntelliJ Platform SDK           |
| Build        | Gradle Kotlin DSL               |
| UI           | JetBrains Compose Multiplatform |
| Testing      | JUnit 5                         |
| Architecture | Modular                         |
| License      | Open Source                     |

Do **not** introduce Java unless explicitly requested.

Do **not** use JCEF unless specifically requested.

---

# 5. Core Design Principles

## Text is the Source of Truth

Diagram Composer never creates a proprietary diagram format.

Users must always be able to edit the source directly.

---

## Visual Editing Manipulates Concepts

Users edit:

* elements
* relationships
* boundaries
* properties

Users do **not** manually draw pixels or control final rendering.

---

## Rendering is External

Diagram Composer does **not** render diagrams.

Rendering is delegated to existing plugins (for example, PlantUML).

---

## Language Independence

The editor must not contain language-specific logic.

Language-specific behaviour belongs inside adapters.

---

## Keep It Simple

Prefer the simplest solution that satisfies the current requirements.

Avoid speculative abstractions.

Avoid premature optimisation.

---

# 6. Module Overview

Typical project structure:

```text
core/
model/
adapter-api/
adapter-plantuml/
ui-compose/
intellij-plugin/
tests/
docs/
```

Responsibilities:

| Module           | Responsibility                      |
|------------------|-------------------------------------|
| core             | Commands, editing logic, validation |
| model            | Language-independent diagram model  |
| adapter-api      | Adapter interfaces                  |
| adapter-plantuml | PlantUML implementation             |
| ui-compose       | Compose UI                          |
| intellij-plugin  | IntelliJ integration                |
| docs             | Specifications                      |

Dependencies should flow inward. The core and model modules must remain free of IntelliJ- and language-specific code.

---

# 7. Development Principles

The project follows:

* specification-driven development
* Kotlin-first implementation
* incremental delivery
* AI-assisted development
* automated testing
* small reviewable changes

The specifications in `docs/` are the source of truth.

---

# 8. Testing Requirements

Every behaviour change must include appropriate automated tests.

Examples:

| Change      | Required Tests      |
|-------------|---------------------|
| New model   | Unit tests          |
| New command | Execute/undo tests  |
| New parser  | Parser tests        |
| Bug fix     | Regression test     |
| UI workflow | UI/integration test |

A feature without tests is incomplete.

---

# 9. Documentation Requirements

Update documentation whenever behaviour changes.

Possible updates include:

* architecture.md
* product.md
* ui.md
* adapters.md
* engineering.md
* development.md

Documentation should remain aligned with implementation.

---

# 10. Coding Expectations

Prefer:

* idiomatic Kotlin
* immutable data classes
* sealed interfaces/classes
* extension functions
* clear naming
* explicit behaviour

Avoid:

* unnecessary inheritance
* global mutable state
* duplicated logic
* Java-style code
* large classes
* hidden behaviour

---

# 11. Definition of Done

A task is complete only when:

* Code compiles.
* Tests have been added or updated.
* All tests pass.
* Documentation has been updated if required.
* No architectural boundaries have been violated.
* No unnecessary complexity has been introduced.

---

# 12. Current Status

Current implementation stage:

> Complete:
> - [x] Milestone 0 (project scaffolding) 
> - [x] Milestone 1 (core domain model)
> - [x] Milestone 2 (adapter API)
> - [x] Milestone 3 (PlantUML C4 adapter — parsing)
> - [x] Milestone 4 (PlantUML C4 adapter — generation) 
> - [x] Milestone 5 (core editing logic)
> - [x] Milestone 6 (core ↔ adapter integration)
> - [x] Milestone 7 (UI: read-only viewer)
>
> Next:
> - [ ] Milestone 8 (UI: basic editing)

CI (`.github/workflows/ci.yml`) now runs `ktlintCheck` and `detekt` ahead of
`build test`, per Milestone 0 task 7. See `docs/development.md` §18 for how
these are configured.

See `docs/implementation-plan.md` for the full milestone breakdown and current position.

The implementation should proceed incrementally through defined milestones.

---

# 13. AI Working Rules

When contributing:

1. Read this document first.
2. Follow the project specifications.
3. Work only on the requested task.
4. Avoid redesigning unrelated components.
5. Prefer incremental improvements.
6. State assumptions before implementing if requirements are unclear.
7. Include tests with every code change.
8. Explain any significant architectural impact.

If multiple reasonable solutions exist, choose the simplest one that satisfies the specifications.

---

# 14. Decision Priority

When making implementation decisions, optimise in this order:

1. Correctness
2. Simplicity
3. Readability
4. Testability
5. Maintainability
6. Extensibility
7. Performance
8. Cleverness

Never sacrifice a higher priority to improve a lower one.

---

# 15. Recommended AI Workflow

For each task:

1. Understand the task and relevant specifications.
2. Identify the affected module(s).
3. Explain the proposed approach if it is non-trivial.
4. Implement only the requested functionality.
5. Add or update tests.
6. Verify the implementation against the acceptance criteria.
7. Summarise:

    * what changed,
    * tests added,
    * documentation updated,
    * assumptions made.

Keep changes small, focused, and easy to review.

---

# 16. Related Documents

For detailed information, consult:

1. `docs/vision.md`
2. `docs/architecture.md`
3. `docs/product.md`
4. `docs/ui.md`
5. `docs/adapters.md`
6. `docs/engineering.md`
7. `docs/development.md`

These documents collectively define the intended behaviour and architecture of Diagram Composer.
