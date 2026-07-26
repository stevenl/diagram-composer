# Diagram Composer AI Development Guide

This document defines how AI coding agents should contribute to the Diagram Composer project.

These instructions apply to all AI coding assistants, regardless of platform.

Examples include:

- Claude Code
- GitHub Copilot
- Cursor
- Codex
- Future AI development tools

This document is the authoritative source for AI development behaviour.

---

# Project Overview

Diagram Composer is an IntelliJ IDEA plugin that enables developers to create and edit text-based diagrams through a visual editor.

The project is:

- Kotlin-first
- IntelliJ Platform native
- Open source
- Specification-driven
- Test-driven
- AI-assisted

Diagram Composer is **not** a rendering engine.

Rendering is delegated to existing plugins (for example PlantUML).

The visual editor exists to make writing diagram source faster while preserving text as the source of truth.

---

# Project Philosophy

The project values:

1. Correctness
2. Simplicity
3. Readability
4. Testability
5. Maintainability
6. Extensibility
7. Performance

Never sacrifice simplicity merely to support hypothetical future features.

Avoid over-engineering.

---

# Read Before Coding

Before implementing any feature, read the following documents in order.

1. docs/vision.md
2. docs/architecture.md
3. docs/product.md
4. docs/adapters.md
5. docs/engineering.md
6. docs/development.md

The specifications are the authoritative description of intended behaviour.

If implementation differs from the specifications, the specifications take precedence unless instructed otherwise.

---

# Core Principles

## Text is the Source of Truth

Diagram source files are authoritative.

The plugin must never introduce a proprietary document format.

Users must always be able to:

- edit source manually
- commit to Git
- review normal diffs
- use existing rendering plugins

---

## Keep the Core Language Independent

The core model must never contain PlantUML-specific or Mermaid-specific logic.

Language-specific behaviour belongs inside adapters.

---

## Rendering is Not Our Responsibility

Never implement a rendering engine.

Diagram Composer edits semantic models.

Rendering remains the responsibility of external plugins.

---

## Prefer Composition Over Duplication

Reuse existing project components whenever practical.

Avoid duplicated logic.

---

## Keep Modules Independent

Maintain separation between:

- core
- model
- adapters
- UI
- IntelliJ integration

Do not bypass architectural boundaries.

---

# Kotlin Guidelines

Use idiomatic Kotlin.

Prefer:

- immutable data classes
- sealed interfaces/classes
- value objects where appropriate
- extension functions
- expression syntax
- null safety

Avoid:

- unnecessary inheritance
- mutable shared state
- static utility classes
- Java-style code

Do not introduce Java unless explicitly required.

---

# UI Guidelines

Use:

- IntelliJ Platform APIs
- Compose Multiplatform

Do not introduce JCEF unless specifically requested.

The UI edits the model.

The model never depends on the UI.

---

# Adapter Guidelines

Adapters own:

- parsing
- generation
- validation
- formatting
- language capabilities

The editor should never contain language-specific code.

---

# Testing Policy

Every behaviour change must include tests.

Examples:

New model:

→ unit tests

New parser:

→ parser tests

New generator:

→ generator tests

Bug fix:

→ regression test

UI feature:

→ UI/integration tests

A feature without tests is incomplete.

---

# Documentation Policy

Update documentation whenever behaviour changes.

Possible updates include:

- vision.md
- architecture.md
- product.md
- ui.md
- adapters.md
- engineering.md
- development.md

Documentation should evolve alongside the implementation.

---

# Before Writing Code

Understand the existing implementation.

Do not rewrite large areas unnecessarily.

Consider:

- architecture consistency
- existing patterns
- testability
- backwards compatibility

If multiple reasonable approaches exist, prefer the simplest.

---

# During Implementation

Keep commits logically focused.

Avoid unrelated refactoring.

Do not rename or reorganise files unless required.

Avoid introducing dependencies without justification.

Prefer incremental improvements.

---

# Error Handling

Fail gracefully.

Preserve user data.

Never silently discard information.

Unknown syntax should be preserved whenever practical.

---

# Performance

Optimise for:

- Small diagrams (<50 elements)
- Medium diagrams (50–200 elements)
- Large diagrams (200+ elements)

Do not optimise prematurely.

Measure before introducing complexity.

---

# Decision Hierarchy

When making design decisions, optimise in this order:

1. Correctness
2. Simplicity
3. Readability
4. Testability
5. Maintainability
6. Extensibility
7. Performance
8. Cleverness

Do not violate a higher priority to improve a lower one.

---

# When Unsure

Do not invent requirements.

Instead:

- consult the specifications
- infer from existing architecture
- keep behaviour consistent

If uncertainty remains, identify assumptions clearly.

---

# Code Generation Guidelines

Generated code should:

- compile cleanly
- follow existing architecture
- be small and understandable
- avoid unnecessary abstractions
- minimise hidden behaviour

Prefer explicit code over clever code.

---

# Definition of Done

A task is complete only if:

- [x] Code compiles
- [x] Existing tests pass
- [x] New tests are included
- [x] Documentation is updated if required
- [x] Architecture remains consistent
- [x] No unnecessary complexity has been introduced
- [x] Public APIs remain coherent

---

# Pull Request Summary

When completing work, provide a concise summary including:

- What changed
- Why it changed
- Tests added
- Documentation updated
- Any assumptions made

---

# Things to Avoid

Do not:

- introduce proprietary file formats
- bypass the adapter architecture
- duplicate business logic
- place UI logic inside the core model
- introduce language-specific code into the core
- regenerate entire files when targeted updates are possible
- add dependencies without justification
- over-engineer solutions

---

# Long-Term Vision

Diagram Composer should become the best developer-focused editor for text-based diagram languages.

Every contribution should move the project toward:

- simplicity
- reliability
- excellent developer experience
- high-quality code
- maintainable architecture
- easy extensibility