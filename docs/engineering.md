# Engineering Specification

## 1. Overview

This document describes the engineering implementation requirements for Diagram Composer.

The goal is to define:

* Technology choices.
* Implementation patterns.
* Internal architecture.
* Testing strategy.
* Performance requirements.
* Development practices.

---

# 2. Technology Stack

## 2.1 Primary Language

The plugin will be implemented using:

**Kotlin 2.1.x**

Reasons:

* First-class IntelliJ Platform support.
* Modern language features.
* Better null safety than Java.
* Excellent interoperability with existing IntelliJ APIs.
* Better fit for Compose.

Kotlin's version is not an independent choice — the IntelliJ Platform Plugin SDK maps supported Kotlin versions to target IDE builds, and the mapping shifts (sometimes within a single IDE major version) as new IDE releases ship. Section 2.6 below fixes the target IDE range this version is valid for; if that range ever changes, re-check the current mapping at the IntelliJ Platform Plugin SDK's Kotlin support page rather than assuming this version still applies.

---

## 2.2 Build System

Use:

* Gradle Kotlin DSL.
* IntelliJ Platform Gradle Plugin 2.x (the actively maintained successor to the obsolete 1.x Gradle IntelliJ Plugin).

Example:

```
build.gradle.kts
settings.gradle.kts
```

---

## 2.3 UI Framework

Recommended:

**JetBrains Compose Multiplatform**

Used for:

* Diagram canvas.
* Visual editor.
* Custom controls.
* Property panels.

---

## 2.4 IntelliJ UI Integration

Use IntelliJ native APIs for:

* Actions.
* Tool windows.
* Editors.
* Notifications.
* Settings.
* Inspections.

Compose should be embedded where custom UI is required.

---

## 2.5 IntelliJ Platform Integration Points

The plugin integrates through the platform's standard extension points rather than reading and writing files by hand. This section exists so Sections 6 (Synchronisation) and 7 (Undo/Redo) can actually be implemented, not as new product requirements.

### Language and file type

Each supported extension (`.puml`, `.plantuml`) is registered as a `com.intellij.lang.Language` with a corresponding `LanguageFileType`. This is what makes the platform treat diagram files as first-class source files (syntax highlighting hooks, PSI, VFS change tracking) instead of generic text.

### PSI

Source text is parsed into a `PsiFile` through the language's parser definition. Adapters (Section 12) should read from and write through PSI — not raw strings read off disk — so the plugin gets IntelliJ's existing incremental reparsing, change events, and editor integration for free instead of re-implementing them.

### FileEditor

The split source/visual layout described in `docs/ui.md` Section 3.1 is implemented as a `FileEditorProvider` supplying a custom `FileEditor` (conceptually similar to the platform's `TextEditorWithPreview`), not a standalone window or dialog.

### Threading

All PSI and VFS reads must happen inside a `ReadAction`; all writes inside a `WriteAction` (typically via `WriteCommandAction`) on the EDT. Compose recomposition happens outside those actions. The Diagram Model is the boundary that marshals data between the two sides — adapters and Compose code must never touch PSI directly from a background thread.

### Undo integration

`WriteCommandAction` boundaries are what IntelliJ's undo stack actually records. The `Command`/`execute()`/`undo()` abstraction in Section 7 should be a thin wrapper around a single `WriteCommandAction`, not a parallel undo mechanism competing with the platform's.

---

## 2.6 Target IDE Version Range

* **Minimum supported IDE:** 2024.3 (build 243).
* **Maximum supported IDE:** none pinned (`until-build` left open) — avoids needing a release just to bump a ceiling.
* **JDK:** 21 (required for IDE builds from 2024.2 onward).

This "balanced" range was chosen over an older floor (e.g. 2024.2, wider install base but older APIs) and a newer one (e.g. 2026.1+, newest APIs but the Kotlin-version-to-build mapping is still actively churning as of mid-2026 — see 2.1 above). Revisit this range periodically — it is expected to move forward over the project's life, not stay fixed at 243.

---

# 3. Module Architecture

The authoritative module layout is defined in `docs/architecture.md` Section 3. This document does not redefine it; refer there.

```
diagram-composer/
└── modules/
    ├── core/
    ├── ui/
    ├── adapter-api/
    ├── adapter-plantuml-c4/
    ├── adapter-mermaid-flowchart/
    └── intellij-plugin/
```

All code modules live under `modules/`, distinguishing them from `docs/` and other repository-level directories. Gradle module coordinates match the folder path exactly (e.g. `:modules:core`).

The domain model (Section 5 below) lives inside `core/` — there is no separate `model/` module. Tests live alongside the code they test (`src/test`, `src/commonTest`, `src/jvmTest`, per each module's own source sets), not in a standalone top-level `tests/` directory.

---

# 4. Core Module

The core module contains language-independent logic.

Responsibilities:

* Diagram model.
* Commands.
* Editing operations.
* Validation.
* Events.

It must not depend on:

* IntelliJ APIs.
* Compose.
* PlantUML.

---

# 5. Diagram Model

The canonical domain model is defined in `docs/architecture.md` Section 4 (Entity, Relationship, Boundary). This section is not a competing definition — it is that same model expressed as Kotlin data classes for implementation reference. If this ever drifts from architecture.md, architecture.md wins.

Ids are wrapped in per-type `value class`es (`EntityId`, `RelationshipId`, `BoundaryId`) rather than passed around as raw `String`. This is a Milestone 1 implementation choice, not a change to the conceptual model in architecture.md §4 — an `EntityId` still just carries a string underneath. It exists so the compiler rejects passing, say, a `RelationshipId` where an `EntityId` is expected, which a raw `String` parameter cannot catch. See `docs/architecture.md` §4.5 for the full class diagram.

Example:

```kotlin
data class Diagram(
    val entities: List<Entity>,
    val relationships: List<Relationship>,
    val boundaries: List<Boundary>,
    val rootChildren: List<BoundaryChildId>, // top-level declaration order; see architecture.md §4.1
)
```

---

## Entity

```kotlin
@JvmInline
value class EntityId(val value: String)

data class Entity(
    val id: EntityId,
    val name: String,
    val type: EntityType,
    val description: String? = null,
    val technology: String? = null,
    val tags: List<String> = emptyList(),
    val properties: Map<String, String> = emptyMap(),
    val external: Boolean = false, // e.g. PlantUML Person_Ext/System_Ext; architecture.md §4.2
)
```

---

## Relationship

```kotlin
@JvmInline
value class RelationshipId(val value: String)

data class Relationship(
    val id: RelationshipId,
    val sourceId: EntityId,
    val targetId: EntityId,
    val description: String? = null,
    val technology: String? = null,
    val type: RelationshipType = RelationshipType.DEFAULT,
    val properties: Map<String, String> = emptyMap()
)
```

---

## Boundary

A boundary's children may be either entities or nested boundaries (architecture.md §4.4). `BoundaryChildId` is a sealed interface over `EntityId`/`BoundaryId` expressing that union, rather than an untyped `List<String>` — this is what lets the core model reject a boundary that references a missing entity or boundary at construction time instead of only at parse/generate time.

```kotlin
sealed interface BoundaryChildId {
    data class OfEntity(val id: EntityId) : BoundaryChildId
    data class OfBoundary(val id: BoundaryId) : BoundaryChildId
}

@JvmInline
value class BoundaryId(val value: String)

data class Boundary(
    val id: BoundaryId,
    val name: String,
    val type: BoundaryType,
    val children: List<BoundaryChildId> = emptyList() // contained entities or nested boundaries
)
```

---

# 6. State Management

The UI should not directly mutate the model.

Use a unidirectional data flow approach:

```
User Action

    ↓

Command

    ↓

Model Update

    ↓

UI State Refresh
```

Example:

```kotlin
CreateEntityCommand
RenameEntityCommand
DeleteRelationshipCommand
```

---

# 7. Undo / Redo

Use command-based editing.

Each command provides:

```kotlin
interface Command {

    fun execute()

    fun undo()
}
```

The command stack integrates with IntelliJ undo management.

---

# 8. Compose UI Architecture

The UI should follow:

```
DiagramScreen
├── Toolbar
├── Canvas
├── PropertiesPanel
└── Explorer
```

---

# 9. Diagram Canvas

The canvas is responsible for:

* Rendering elements.
* Handling selection.
* Handling mouse events.
* Showing relationships.
* Showing boundaries.

It is not responsible for:

* Parsing.
* Generating source.
* Language-specific behaviour.

---

# 10. Rendering Model

The visual canvas uses a separate presentation model.

Example:

```
Diagram Model

      ↓

Layout Engine

      ↓

Visual Model

      ↓

Compose Rendering
```

---

Example:

```kotlin
data class VisualElement(
    val entityId: String,
    val position: Offset,
    val size: Size
)
```

---

# 11. Layout Engine

The MVP should not implement a full layout engine.

Responsibilities:

* Store temporary positions.
* Provide selection coordinates.
* Support simple arrangement.

Future:

* GraphViz integration.
* ELK layout.
* Automatic architecture layouts.

---

# 12. Adapter Implementation

Each adapter implements:

```kotlin
interface DiagramAdapter {

    fun parse(source: String): Diagram

    fun generate(diagram: Diagram): String

    fun validate(source:String): List<Issue>
}
```

---

# 13. Parsing Strategy

The parser must support:

* Error recovery.
* Source preservation.

Incremental parsing is a Phase 2 goal (`docs/adapters.md` Section 13), not an MVP requirement.

For the MVP, this is sufficient:

```
Source

    ↓

Full parse → Model
```

and generation may fully regenerate the source file on every change (`docs/adapters.md` Section 10).

Once that round-trip is reliable and covered by tests, targeted/incremental updates can be introduced as an optimisation:

```
Source

    ↓

AST + Model

    ↓

Small targeted update
```

Do not build the incremental path before the simple, full-regeneration path is correct — building it speculatively ahead of evidence that regeneration is too slow would violate the project's simplicity-first principle.

---

# 14. Source Synchronisation

Maintain three representations:

```
Text

   ↓

Parsed Model

   ↓

Visual Model
```

Changes flow through the model.

---

# 15. Testing Strategy

## Unit Tests

Required for:

* Model operations.
* Commands.
* Parsers.
* Generators.
* Validation.

---

## Adapter Tests

Each adapter requires:

### Parse tests

```
Source → Model
```

### Generation tests

```
Model → Source
```

### Round-trip tests

```
Source
  ↓
Model
  ↓
Source
  ↓
Model
```

---

## UI Tests

Test:

* Create element.
* Edit properties.
* Create relationship.
* Undo.
* Redo.
* Delete.

---

# 16. Performance Requirements

Target:

### Small diagrams

<50 elements

Immediate response.

---

### Medium diagrams

50–200 elements

No noticeable UI lag.

---

### Large diagrams

200+ elements

Graceful degradation.

---

# 17. Logging and Diagnostics

Provide:

* Adapter errors.
* Parse failures.
* Synchronisation issues.
* Performance metrics.

Use IntelliJ logging APIs.

---

# 18. Error Handling

The plugin should fail gracefully.

Examples:

Invalid source:

```
Source cannot be parsed.

Visual editing disabled.

Text editing remains available.
```

Unsupported syntax:

```
Some elements cannot be displayed.

Unknown content preserved.
```

---

# 19. Development Practices

Required:

* Kotlin formatting (ktlint).
* Automated tests.
* CI builds.
* Static analysis (Detekt).
* Documentation updates with major changes.

See `docs/development.md` §18 for how these are wired into CI.

---

# 20. Future Engineering Enhancements

Potential future work:

* Persistent diagram metadata.
* Incremental AST editing.
* Advanced layout engines.
* AI-assisted diagram generation.
* Architecture analysis.
* Multi-file diagrams.

---

# 21. Summary

Diagram Composer should be implemented as a Kotlin-first IntelliJ plugin using Compose Multiplatform for the visual editing experience.

The architecture should prioritise:

* Clean separation between model, UI, and adapters.
* Text-first workflows.
* Extensibility.
* Native IntelliJ integration.
* Maintainability for open-source contributors.

The recommended technology direction is:

**Kotlin + IntelliJ Platform APIs + Compose Multiplatform + pluggable diagram adapters.**
