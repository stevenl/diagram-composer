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

# 3. Module Architecture

Recommended project layout:

```
diagram-composer/

├── core/
│
├── model/
│
├── adapter-api/
│
├── adapter-plantuml/
│
├── ui-compose/
│
├── intellij-plugin/
│
└── tests/
```

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

The model represents a semantic diagram.

Example:

```kotlin
data class Diagram(
    val elements: List<Element>,
    val relationships: List<Relationship>,
    val boundaries: List<Boundary>
)
```

---

## Element

```kotlin
data class Element(
    val id: String,
    val name: String,
    val type: ElementType,
    val properties: Map<String,String>
)
```

---

## Relationship

```kotlin
data class Relationship(
    val sourceId: String,
    val targetId: String,
    val description: String?
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
    val elementId: String,
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

The parser should support:

* Incremental parsing.
* Error recovery.
* Source preservation.

Avoid:

```
Source

   ↓

Complete regeneration
```

for every change.

Prefer:

```
Source

    ↓

AST + Model

    ↓

Small targeted update
```

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
