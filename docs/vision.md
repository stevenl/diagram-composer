# Diagram Composer Vision

**Version:** 1.0
**Status:** Draft

---

# 1. Purpose

Diagram Composer exists to make creating and maintaining **text-based diagrams** significantly faster and more enjoyable for developers.

It combines the strengths of text-based diagram languages with the convenience of visual editing, without sacrificing the advantages that made developers choose text-based diagrams in the first place.

Diagram Composer is not a replacement for existing diagram languages or rendering tools. Instead, it is a productivity tool that accelerates the authoring of those languages.

---

# 2. Vision Statement

> **To become the best developer-focused visual authoring environment for text-based diagram languages.**

Developers should be able to create complex diagrams with the same speed and confidence that modern IDEs provide when writing source code.

Diagram Composer should feel like an IDE feature, not a separate drawing application.

---

# 3. The Problem

Text-based diagram languages have become increasingly popular because they integrate naturally with software development workflows.

They work well with:

* Git
* Pull requests
* Documentation repositories
* Continuous integration
* Code reviews

However, authoring these diagrams is often slow and repetitive.

Developers frequently need to:

* Memorise language syntax.
* Look up documentation.
* Create nodes manually.
* Connect relationships manually.
* Correct syntax mistakes.
* Repeatedly switch between editing and previewing.

As diagrams become larger, this manual process becomes increasingly time-consuming.

---

# 4. Our Solution

Diagram Composer provides a semantic editor that allows developers to manipulate diagrams visually while continuously maintaining the underlying text representation.

The user edits concepts such as:

* Systems
* Components
* Services
* Databases
* Relationships
* Boundaries

The plugin generates and maintains the corresponding source code.

Developers remain free to edit the text directly whenever they choose.

Both editing styles coexist naturally.

---

# 5. Design Philosophy

Diagram Composer is guided by a small number of enduring principles.

## Text is the Source of Truth

The underlying diagram language remains authoritative.

Diagram Composer never creates or stores a proprietary diagram format.

Developers can continue using:

* Git
* Existing renderers
* Existing documentation pipelines
* Existing IDE plugins

without modification.

---

## Visual Editing Accelerates Authoring

The visual editor exists to eliminate repetitive typing, not to replace text.

Users should be able to choose whichever editing style is most efficient for the task at hand.

Some tasks are faster visually.

Others are faster in plain text.

Diagram Composer should support both equally well.

---

## Developer Experience Comes First

The plugin is designed for developers rather than professional illustrators.

Success is measured by how quickly a developer can communicate an idea—not by producing artistic diagrams.

The interface should prioritise:

* speed
* clarity
* predictability
* keyboard efficiency
* integration with existing development workflows

---

## Open Standards

Diagram Composer embraces existing diagram languages rather than inventing a new one.

The project should contribute to the wider ecosystem instead of fragmenting it.

---

## Extensible by Design

Support for additional diagram languages should require new adapters rather than changes to the editor itself.

The editor should remain language-independent.

---

# 6. Long-Term Vision

Diagram Composer should evolve into a universal editor for text-based diagrams.

The editor should eventually support multiple diagram languages through a common editing experience.

Potential languages include:

* PlantUML
* Mermaid
* Structurizr DSL
* D2
* Graphviz DOT

Future language support should be additive rather than requiring architectural changes.

---

# 7. Guiding Principles

## Simplicity

Prefer simple solutions.

Avoid unnecessary complexity.

---

## Correctness

Generated diagram source should always be valid and reliable.

---

## Transparency

Users should always understand how visual edits affect the underlying source.

Nothing should happen "behind the scenes" without being visible in the generated text.

---

## Predictability

The same actions should always produce the same result.

Unexpected behaviour erodes confidence.

---

## Maintainability

The project should remain approachable for both users and contributors.

Architecture should be clean, well documented and easy to understand.

---

# 8. Target Audience

Diagram Composer is primarily intended for software developers.

Typical users include:

* application developers
* software architects
* technical leads
* DevOps engineers
* engineering managers
* technical consultants
* technical writers

The initial optimisation target is developers creating architecture and API documentation inside IntelliJ IDEA.

---

# 9. Success Criteria

Diagram Composer is successful when developers can:

* create diagrams significantly faster than writing raw syntax
* continue editing diagram source directly without restrictions
* commit generated source confidently to Git
* collaborate through normal code review workflows
* switch between visual editing and text editing without friction

The plugin should reduce effort without reducing control.

---

# 10. What Diagram Composer Is Not

Diagram Composer is not intended to become:

* a replacement for PlantUML
* a replacement for Mermaid
* a rendering engine
* a vector drawing application
* a Visio competitor
* a draw.io clone

Those products solve different problems.

Diagram Composer focuses exclusively on improving the authoring experience for text-based diagrams.

---

# 11. Open Source Vision

Diagram Composer is intended to become a community-driven project.

The project should be:

* welcoming to contributors
* well documented
* specification-driven
* easy to extend
* architecturally stable

The adapter framework should encourage the community to add support for additional diagram languages without requiring changes to the core editor.

---

# 12. Future Opportunities

While the MVP focuses on authoring, Diagram Composer should establish a foundation for future capabilities such as:

* AI-assisted diagram generation.
* Diagram validation and linting.
* Architecture analysis.
* Automatic diagram generation from source code.
* Cross-language conversion between diagram formats.
* Intelligent refactoring of diagrams.
* Shared diagram templates.
* Richer editing experiences for additional diagram DSLs.

These capabilities should build upon the same core architecture rather than requiring a redesign.

---

# 13. Core Values

Every design decision should reinforce the following values:

1. Developers first.
2. Text remains authoritative.
3. Simplicity over cleverness.
4. Native IDE experience.
5. Open standards over proprietary formats.
6. Extensibility through adapters.
7. High-quality engineering.
8. Comprehensive automated testing.
9. Excellent documentation.
10. Long-term maintainability.

---

# 14. Vision Summary

Diagram Composer aims to become the preferred way for developers to author text-based diagrams.

Rather than replacing existing diagram languages, it complements them by providing a fast, intuitive and IDE-native editing experience that preserves the openness, portability and maintainability of text-based diagram source.

The long-term goal is a single extensible editor capable of supporting many diagram languages through a common user experience, allowing developers to focus on communicating ideas instead of memorising syntax.
