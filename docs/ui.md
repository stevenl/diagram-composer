# Diagram Composer UI Specification

## 1. Overview

## 1.1 Purpose

This document defines the user interface and interaction design for **Diagram Composer**, an IntelliJ IDEA plugin for visually authoring text-based diagrams.

The UI is designed around the following principle:

> Users compose diagrams visually while retaining complete control over the underlying text representation.

The UI should feel like a natural extension of IntelliJ IDEA rather than a separate diagramming application.

---

# 2. Design Principles

## 2.1 Developer First

Diagram Composer is designed for developers.

Priorities:

1. Fast creation.
2. Keyboard accessibility.
3. Source visibility.
4. Minimal dialogs.
5. IntelliJ-native behaviour.

---

## 2.2 Text and Visual Views Are Equal

The visual editor and source editor are two views of the same diagram model.

```text
               Diagram Model

              /             \

       Source View       Visual View
```

Neither view is secondary.

---

## 2.3 Semantic Editing Over Drawing

Diagram Composer edits **meaning**, not pixels.

Users manipulate:

* Systems.
* Containers.
* Components.
* Relationships.
* Boundaries.

Users do not manipulate:

* Exact coordinates.
* Lines manually.
* Visual styling directly.

The rendering engine remains responsible for final layout.

---

# 3. Editor Layout

## 3.1 Main Editor

The primary editing experience is a split editor.

Example:

```text
+----------------------------------------------------------+
| Diagram Composer Toolbar                                 |
+----------------------------------------------------------+
|                                                          |
| Source Editor                 Visual Editor              |
|                                                          |
| @startuml                     +----------------+         |
| Container(api,...)            |                |         |
|                               |     API        |         |
| Rel(web,api)                  |       |        |         |
|                               |       v        |         |
| @enduml                       |   Database     |         |
|                               |                |         |
+----------------------------------------------------------+
```

---

## 3.2 Source Editor

The source editor is the standard IntelliJ text editor.

Requirements:

* Full IntelliJ editing support.
* Syntax highlighting.
* Code completion (where supported).
* Find/replace.
* Refactoring support.
* Git diff support.

Diagram Composer does not replace normal text editing.

---

## 3.3 Visual Editor

The visual editor provides semantic editing.

Responsibilities:

* Display diagram elements.
* Allow selection.
* Create elements.
* Create relationships.
* Edit properties.

It is not a rendering preview.

---

# 4. Toolbar

## 4.1 Purpose

The toolbar provides quick access to common diagram operations.

The toolbar changes based on the active adapter.

---

# 4.2 PlantUML C4 Toolbar

Example:

```text
+-----------------------------------------------+
| Person | System | Container | Component | DB  |
|-----------------------------------------------|
| Relationship | Boundary                       |
+-----------------------------------------------+
```

---

# 4.3 Future Mermaid Toolbar

Example:

```text
+-------------------------------+
| Rectangle | Decision | Circle |
| Database  | Arrow             |
+-------------------------------+
```

The UI does not contain language-specific logic.

The adapter provides available actions.

---

# 5. Creating Diagram Elements

## 5.1 Creation Workflow

Example: Create Container

1. User selects "Container".
2. Cursor changes to creation mode.
3. User clicks diagram area.
4. Element creation dialog appears.
5. User enters details.
6. Element is created.
7. Source is updated.

---

## 5.2 Creation Dialog

Example:

```text
Create Container

Name:
[ Payment API              ]

Identifier:
[ payment-api             ]

Technology:
[ Spring Boot             ]

Description:
[ Handles payment requests ]

        Cancel       Create
```

---

## 5.3 Identifier Generation

The plugin should automatically suggest identifiers.

Example:

Input:

```text
Payment API
```

Suggested:

```text
payment-api
```

The user may override it.

---

# 6. Selecting Elements

## 6.1 Selection Behaviour

Selecting an element:

* Highlights the element.
* Shows properties.
* Enables context actions.

Example:

```text
Selected:

+----------------+
| Payment API    |
+----------------+

Properties:

Name:
Payment API

Technology:
Spring Boot
```

---

# 7. Property Panel

## 7.1 Purpose

The property panel provides structured editing of diagram elements.

---

## 7.2 Element Properties

Common properties:

```text
Identifier

Name

Description

Technology

Tags

Type
```

---

## 7.3 Relationship Properties

Example:

```text
Relationship

From:
Frontend

To:
Payment API

Description:
Uses REST API

Technology:
HTTPS
```

---

# 8. Context Menus

## 8.1 Element Context Menu

Right-click element:

```text
Edit Properties

Rename

Change Type

Create Relationship

Duplicate

Delete
```

---

## 8.2 Relationship Context Menu

Right-click relationship:

```text
Edit Relationship

Reverse Direction

Delete
```

---

# 9. Relationship Creation

## 9.1 Workflow

1. Select Relationship tool.
2. Click source element.
3. Click destination element.
4. Enter relationship details.
5. Relationship is created.

---

## 9.2 Quick Relationship Mode

Future enhancement:

Hold a keyboard modifier while selecting an element.

Example:

```text
Select API

Press R

Click Database
```

Creates:

```plantuml
Rel(api,db)
```

---

# 10. Navigation

## 10.1 Diagram Explorer

Large diagrams require navigation.

Provide a side panel:

```text
Diagram Explorer

Systems

 ├─ Customer Portal

Containers

 ├─ Web Application
 ├─ API
 └─ Database

Relationships

 ├─ Web → API
 └─ API → Database
```

---

## 10.2 Search

Support IntelliJ-style search.

Examples:

Search:

```text
payment
```

Find:

* Elements.
* Relationships.
* Properties.

---

# 11. Keyboard Support

## 11.1 General Principles

Common actions should have shortcuts.

All shortcuts should integrate with IntelliJ keymaps.

---

## 11.2 Suggested Defaults

| Action              | Shortcut     |
|---------------------|--------------|
| Create Element      | Ctrl+N       |
| Create Relationship | Ctrl+Shift+R |
| Delete Selected     | Delete       |
| Rename              | Shift+F6     |
| Show Properties     | Alt+Enter    |

---

# 12. Multi-Selection

Support selecting multiple elements.

Operations:

* Delete.
* Add tags.
* Change properties.
* Move into boundary.

---

# 13. Boundaries

## 13.1 Creating Boundaries

Workflow:

1. Select boundary.
2. Select elements.
3. Create grouping.

Example:

```text
+--------------------------------+
| Payment System                 |
|                                |
|  API                           |
|  Database                      |
|                                |
+--------------------------------+
```

---

# 14. Layout

## 14.1 Layout Philosophy

Diagram Composer does not provide free-form drawing.

The renderer controls final layout.

---

## 14.2 Layout Commands

Provide semantic layout actions:

```text
Arrange

├─ Top to Bottom
├─ Left to Right
├─ Compact
└─ Reset
```

These actions influence generated source where supported.

---

# 15. Source Synchronisation UI

## 15.1 Status Indicator

Display synchronisation state.

Example:

```text
✓ Diagram synchronized
```

or:

```text
⚠ Source contains unsupported syntax
```

---

## 15.2 Parse Errors

Show IntelliJ inspections.

Example:

```text
Unable to parse relationship.

Line 24:
Rel(api,)
```

The visual editor should remain usable where possible.

---

# 16. Empty State

When opening a new diagram:

Display:

```text
Create your first diagram

[Create System]

[Create Container]

[Open Template]
```

---

# 17. Templates

Future enhancement.

Templates:

* C4 System Context.
* C4 Container.
* API Architecture.
* Microservices.
* Event-driven Architecture.

Example:

```text
New Diagram

Choose Template:

○ Empty Diagram

○ Web Application

○ Microservices

○ Event Driven System
```

---

# 18. IntelliJ Integration

## 18.1 Actions

Provide IntelliJ actions:

* Open Diagram Composer.
* Create Element.
* Create Relationship.
* Format Diagram.
* Validate Diagram.

---

## 18.2 Tool Window

Optional tool window:

```text
Diagram Composer

Elements

Templates

Properties

Validation
```

---

# 19. Accessibility

Requirements:

* Keyboard navigation.
* Screen reader compatibility where supported.
* Avoid colour-only indicators.
* Use IntelliJ standard UI components.

---

# 20. Future UI Enhancements

Potential additions:

* AI-assisted diagram creation.
* Architecture suggestions.
* Code-to-diagram navigation.
* Multiple diagram tabs.
* Embedded documentation.
* Diagram comparison.
* Collaboration features.

---

# 21. Summary

The Diagram Composer UI provides a semantic editing environment for text-based diagrams.

The key UX decisions are:

* Split source and visual editing.
* Text remains authoritative.
* Visual editing manipulates diagram concepts, not pixels.
* IntelliJ-native interactions.
* Adapter-driven UI capabilities.
* Focus on developer workflows.

The MVP should feel like adding a powerful diagram authoring capability to IntelliJ IDEA rather than introducing a separate drawing application.
