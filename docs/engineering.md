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

**Kotlin**

Reasons:

* First-class IntelliJ Platform support.
* Modern language features.
* Better null safety than Java.
* Excellent interoperability with existing IntelliJ APIs.
* Better fit for Compose.

---

## 2.2 Build System

Use:

* Gradle Kotlin DSL.
* IntelliJ Platform Gradle Plugin.

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

# 3. Module Architecture

The authoritative module layout is defined in `docs/architecture.md` Section 3. This document does not redefine it; refer there.

```
diagram-composer/

└── modules/

    ├── core/

    ├── ui/

    ├── adapter-api/

    ├── adapter-plantuml-c4/

    ├── adapter-mermaid/

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

Example:

```kotlin
data class Diagram(
    val entities: List<Entity>,
    val relationships: List<Relationship>,
    val boundaries: List<Boundary>
)
```

---

## Entity

```kotlin
data class Entity(
    val id: String,
    val name: String,
    val type: EntityType,
    val description: String? = null,
    val technology: String? = null,
    val tags: List<String> = emptyList(),
    val properties: Map<String, String> = emptyMap()
)
```

---

## Relationship

```kotlin
data class Relationship(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val description: String? = null,
    val technology: String? = null,
    val type: RelationshipType = RelationshipType.DEFAULT,
    val properties: Map<String, String> = emptyMap()
)
```

---

## Boundary

```kotlin
data class Boundary(
    val id: String,
    val name: String,
    val type: BoundaryType,
    val children: List<String> = emptyList() // ids of contained entities or nested boundaries
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
CreateElementCommand
RenameElementCommand
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

* Kotlin formatting.
* Automated tests.
* CI builds.
* Static analysis.
* Documentation updates with major changes.

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
