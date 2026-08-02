# Diagram Composer Product Specification

## 1. Overview

## 1.1 Purpose

This document defines the product requirements for the IntelliJ IDEA Diagram Composer plugin.

The plugin helps developers create and maintain software diagrams faster through a visual modelling interface.

The initial target is **PlantUML C4 diagrams**, with future support planned for other architecture and flow diagram languages such as Mermaid.

---

# 2. Product Vision

Developers increasingly document software architecture using text-based diagram languages because they integrate well with:

* Git workflows.
* Code reviews.
* Documentation systems.
* CI/CD pipelines.

However, creating complex diagrams manually using text syntax is slow and error-prone.

The plugin provides a faster authoring workflow:

```text
Traditional workflow:

Think about architecture
        ↓
Write syntax manually
        ↓
Fix syntax errors
        ↓
Preview diagram


Plugin workflow:

Think about architecture
        ↓
Create entities visually
        ↓
Connect relationships visually
        ↓
Refine generated text
        ↓
Preview diagram
```

The goal is not to replace text-based diagrams, but to make them easier to create and maintain.

---

# 3. Target Users

## Primary User

Software developers creating architecture documentation.

Typical activities:

* Designing new systems.
* Documenting existing systems.
* Creating API flow diagrams.
* Explaining technical designs.
* Reviewing architecture changes.

---

## Secondary Users

Potential future users:

* Solution architects.
* Technical leads.
* Engineering managers.
* Technical writers.
* Consultants.

---

# 4. Primary Use Cases

## 4.1 Software Architecture Documentation

Example:

A developer wants to document:

```text
User

  ↓

Web Application

  ↓

API Service

  ↓

Database
```

The plugin allows them to create:

* Systems.
* Containers.
* Components.
* Databases.
* Relationships.

---

## 4.2 API Flow Documentation

Example:

```text
Mobile App

    ↓

API Gateway

    ↓

Payment Service

    ↓

Payment Provider
```

The developer can quickly create relationships and label them.

---

## 4.3 Architecture Review

During design discussions, developers can:

* Add components.
* Modify relationships.
* Explore alternatives.
* Review generated diagrams.

---

# 5. Product Principles

## 5.1 Developer First

The plugin should feel natural to developers.

Priorities:

1. Speed.
2. Keyboard support.
3. Source control compatibility.
4. Minimal ceremony.

---

## 5.2 Text Compatibility

The output must remain valid source code.

Users should always be able to:

* Open the file.
* Edit text manually.
* Commit changes.
* Review diffs.

---

## 5.3 Progressive Enhancement

A user should get value without using every feature.

Examples:

Beginner:

* Click buttons.
* Create diagrams visually.

Advanced:

* Edit generated PlantUML directly.
* Use shortcuts.
* Customize output.

---

# 6. MVP Scope

The MVP focuses on creating useful C4 architecture diagrams quickly.

## Included

### Diagram Types

Supported:

* PlantUML C4 diagrams.

Specifically:

* System Context diagrams.
* Container diagrams.
* Component diagrams.

---

## Entity Types

The MVP supports:

### Person

Example:

```plantuml
Person(user, "Customer")
```

---

### Software System

Example:

```plantuml
System(app, "Shopping Application")
```

---

### Container

Example:

```plantuml
Container(api, "API Service")
```

---

### Component

Example:

```plantuml
Component(auth, "Authentication Component")
```

---

### Database

Example:

```plantuml
ContainerDb(db, "Database")
```

---

## Boundaries

Supported:

* System boundaries.
* Container boundaries.

---

## Relationships

Supported:

* Create relationship.
* Delete relationship.
* Edit relationship description.

Example:

```plantuml
Rel(web, api, "Uses REST API")
```

---

# 7. Core Features

# 7.1 Visual Entity Creation

## Requirement

Users can create diagram entities without manually writing syntax.

Workflow:

1. Select entity type.
2. Place entity.
3. Enter properties.
4. Entity appears in diagram model.
5. Source text updates.

---

## Acceptance Criteria

The user can create:

* Person.
* System.
* Container.
* Component.
* Database.

Each entity generates valid PlantUML.

---

# 7.2 Relationship Creation

## Requirement

Users can connect entities visually.

Workflow:

1. Select relationship tool.
2. Select source entity.
3. Select destination entity.
4. Enter description.
5. Relationship is created.

---

## Acceptance Criteria

Generated PlantUML contains:

```plantuml
Rel(source, destination, "description")
```

---

# 7.3 Property Editing

Users can modify entity properties.

Supported:

* Identifier.
* Name.
* Description.
* Technology.
* Tags.

Example:

Before:

```text
API
```

After:

```text
Payment API

Technology:
Spring Boot
```

---

# 7.4 Text Editing

The user can always edit the source directly.

Requirements:

* Source remains visible.
* Manual edits are parsed.
* Visual view updates.

---

# 7.5 Live Synchronisation

Changes must flow both directions.

## Visual → Text

Example:

Create Container:

```text
Visual action

      ↓

Container(api,"API")

      ↓

PlantUML file updated
```

---

## Text → Visual

Example:

User edits:

```plantuml
Container(db,"Database")
```

The visual model updates automatically.

---

# 7.6 Undo and Redo

All editing operations support IntelliJ undo/redo.

Examples:

* Create entity.
* Delete entity.
* Rename entity.
* Add relationship.
* Modify properties.

---

# 8. User Interface Requirements

## 8.1 Editor Layout

The MVP uses a split editor.

Example:

```text
+------------------------------------------------+
| Toolbar                                        |
+----------------------+-------------------------+
| PlantUML Source      | Visual Editor           |
|                      |                         |
| Container(api...)    |        API              |
|                      |         |               |
| Rel(...)             |      Database           |
+----------------------+-------------------------+
```

---

# 8.2 Toolbar

The toolbar provides quick creation actions.

Initial buttons:

Entities:

* Person.
* System.
* Container.
* Component.
* Database.

Relationships:

* Relationship.

---

# 8.3 Property Panel

Selecting an item displays editable properties.

Example:

```
Container

Name:
Payment API

Technology:
Spring Boot

Description:
Handles payments
```

---

# 8.4 Context Actions

Right-click actions:

Entity:

* Rename.
* Edit properties.
* Delete.
* Create relationship.

Relationship:

* Edit label.
* Delete.

---

# 9. Non-Functional Requirements

## 9.1 Performance

The plugin should remain responsive for:

* Small diagrams (<50 elements).
* Medium diagrams (50–200 elements).

---

## 9.2 Reliability

The plugin must not corrupt source files.

Requirements:

* Preserve unknown syntax where possible.
* Avoid unnecessary rewrites.
* Support recovery after parser failures.

---

## 9.3 IntelliJ Compatibility

Support:

* Current IntelliJ IDEA versions.
* Community Edition where possible.
* Ultimate Edition.

---

## 9.4 Open Source

The project should be:

* Publicly hosted.
* Contributor friendly.
* Extensible.

---

# 10. Out of Scope for MVP

The following are intentionally excluded.

## Full diagram drawing

Not supported:

* Pixel-perfect positioning.
* Freehand drawing.
* Resize handles.

Reason:

Diagrams are semantic models, not illustrations.

---

## Automatic architecture discovery

Not included:

* Scanning source code.
* Generating diagrams from classes.
* Dependency analysis.

Potential future feature.

---

## Multiple diagram languages

Not included initially.

Future:

* Mermaid.
* Structurizr DSL.
* D2.
* Graphviz.

---

## AI generation

Not included in MVP.

Potential future capability:

* Generate diagrams from descriptions.
* Suggest missing components.
* Explain architecture.

---

# 11. Future Roadmap

## Phase 1 — MVP

PlantUML C4 editing:

* Entities.
* Relationships.
* Properties.
* Synchronisation.
* IntelliJ integration.

---

## Phase 2 — Usability Improvements

Add:

* Keyboard shortcuts.
* Templates.
* Diagram navigation.
* Better formatting.
* Improved error messages.

---

## Phase 3 — Additional Languages

Add:

* Mermaid.
* Structurizr DSL.
* Other text-based diagram formats.

---

## Phase 4 — Intelligence

Potential features:

* Generate diagrams from code.
* Architecture validation.
* Dependency analysis.
* AI-assisted design.

---

# 12. Success Criteria

The MVP is successful if a developer can:

1. Create a useful C4 architecture diagram in minutes.
2. Avoid manually remembering PlantUML syntax.
3. Continue editing the source normally.
4. Commit diagrams to Git.
5. Collaborate with teammates using normal code review workflows.

---

# 13. Summary

The product is a developer-focused visual editor for text-based diagrams.

The MVP deliberately focuses on:

* PlantUML C4.
* Fast diagram modelling.
* Text-first workflows.
* IntelliJ integration.
* Extensibility.

The product should feel like an IDE feature rather than a separate diagramming application.
