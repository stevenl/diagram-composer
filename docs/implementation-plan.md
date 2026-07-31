# Diagram Composer — Implementation Plan

**Companion to:** `docs/ai-context.md`
**Purpose:** Break the project into milestones and small, single-session tasks, ordered so each task needs minimal context to complete.

---

## How to use this plan (context-window strategy)

Since we're on limited context:

- Start each session with `docs/ai-context.md` + this plan. Only pull in the specific spec doc (`architecture.md`, `adapters.md`, etc.) relevant to the milestone you're in.
- Work **one task at a time**. Each task below is scoped to fit in a single session (implementation + tests + doc update).
- Follow dependency order — module dependencies flow inward (`adapter-api` ← `adapter-plantuml-c4`, `core` has no dependency on adapters/IntelliJ, `ui` depends on `core`, `intellij-plugin` depends on everything). There is no separate `model` module — the domain model lives inside `core` (see `docs/engineering.md` §3).
- At the end of every task: state what changed, tests added, docs updated, and any assumptions — per the "Recommended AI Workflow" in `ai-context.md`.
- If a task turns out bigger than expected, stop and split it rather than pushing through — flag it in this plan for re-splitting.

---

## Milestone 0 — Project Scaffolding

Goal: an empty but correctly structured, buildable multi-module project.

All code modules live under `modules/` so they're visually distinct from `docs/`, `gradle/`, and root config files. Gradle coordinates match the folder path exactly (e.g. `:modules:core`) — no shortening indirection, to keep structure inferrable at a glance even with partial context. Module names match the authoritative layout in `docs/architecture.md` §3: there is no separate `model` module (the domain model lives inside `core`), and `adapter-mermaid` is part of the target layout but its folder isn't created until Milestone 11 — creating it earlier would be a speculative module with nothing in it, which `ai-context.md` §5 tells us to avoid.

```text
modules/
    core/
    adapter-api/
    adapter-plantuml-c4/
    ui/
    intellij-plugin/
docs/
gradle/
build.gradle.kts
settings.gradle.kts
```

1. Create the `modules/` directory with subfolders: `core/`, `adapter-api/`, `adapter-plantuml-c4/`, `ui/`, `intellij-plugin/`. Create top-level `tests/` and `docs/` alongside `modules/`. (Do not create `adapter-mermaid-flowchart/` yet — see Milestone 11.)
2. Configure root `settings.gradle.kts` to `include(":modules:core")` etc. for each module, and root `build.gradle.kts` with Kotlin DSL (Kotlin 2.1.x, JDK 21 toolchain), versions catalog, and module wiring (no logic yet — just modules that compile). See `docs/engineering.md` §2 for the pinned versions and why they're paired this way.
3. Add JUnit 5 test setup to each non-UI module with one placeholder passing test per module.
4. Add JetBrains Compose Multiplatform dependency setup to `ui` (empty composable that compiles).
5. Add IntelliJ Platform Gradle Plugin 2.x setup to `intellij-plugin`: `since-build` 243 (2024.3), `until-build` left unset, empty `plugin.xml` that builds.
6. Push the docs already authored in this repo's `docs/` folder (`architecture.md`, `product.md`, `ui.md`, `adapters.md`, `engineering.md`, `development.md`, `vision.md`, `specification-index.md`) along with `AGENTS.md`, `CLAUDE.md`, `.cursor/`, and `.github/copilot-instructions.md` — these are already fully written (per `ai-context.md` §12, specs are complete), not stubs; this task is just getting them into the GitHub repo, which currently only has `LICENSE` and `README.md`.
7. Add `.github/workflows/ci.yml`: triggers on `push` (any branch) and `pull_request`; runs `./gradlew build test` with Gradle dependency caching. Also add a commit-lint step (e.g. `wagoid/commitlint-github-action`) that fails the PR if any commit message doesn't follow Conventional Commits — required because task 9's version derivation depends on every commit being classifiable. This is the only gate for branches/PRs — no release side effects.
8. Adopt Conventional Commits as the required commit message format (`feat:`, `fix:`, `feat!:`/`BREAKING CHANGE:` footer, `chore:`/`docs:`/`test:` for no-bump commits). Document this in `development.md` as a contribution requirement, since it's what the version in task 9 is computed from.
9. Add `.github/workflows/release.yml`: triggers on `push` to `main` only; runs a release-automation tool (e.g. `googleapis/release-please` or `semantic-release`) that reads Conventional Commit messages since the last release tag, computes the next version (patch/minor/major) automatically, writes it into `gradle.properties`, generates a changelog from the commit messages, tags the commit, builds the distributable plugin artifact (`intellij-plugin` packaging task), and publishes a GitHub Release with the artifact and changelog attached. Does **not** publish to the JetBrains Marketplace — flag that as a separate future decision if/when we want public distribution.
10. Document the required branch protection rule in `development.md` (or `engineering.md`): `main` requires the `ci.yml` build (including the commit-lint check) to pass before merge. This is a GitHub repo setting, not something a workflow file enforces — note it as a manual one-time setup step, not a coding task.

**Definition of done:** `./gradlew build` succeeds across all modules; empty plugin can be built as a `.zip`/`.jar` artifact; a push to a feature branch and a PR both trigger `ci.yml` (build, test, and commit-lint) and it passes; merging to `main` triggers `release.yml`, which derives the correct version from commit history, and produces a tagged GitHub Release with the built plugin artifact and changelog attached.

---

## Milestone 1 — Core Domain Model (`core`)

Goal: a language-independent, immutable diagram model with no IntelliJ/language-specific code. This lives inside the `core` module (in its own package, e.g. `core.model`) — there is no separate `model` module, per `docs/architecture.md` §3 and `docs/engineering.md` §3.

1. Define `ElementId`, `RelationshipId`, and other value-object identifiers as inline/value classes.
2. Define `Element` as an immutable data class (name, type, properties) — no PlantUML-specific fields.
3. Define `Relationship` as an immutable data class (source, target, label, type).
4. Define `Boundary` as an immutable data class (name, contained element IDs).
5. Define `Property` (key/value, typed if needed) and attach to `Element`/`Relationship`.
6. Define `Diagram` aggregate root holding elements, relationships, boundaries, with basic invariant checks (e.g., no dangling relationship references).
7. Unit tests: construction, equality, `copy()` semantics for each type.
8. Unit tests: `Diagram` invariant violations (e.g., relationship referencing missing element) are rejected or reported.

**Definition of done:** the domain model package inside `core` has zero dependencies on `adapter-api`, `adapter-plantuml-c4`, or IntelliJ; full test coverage of invariants.

---

## Milestone 2 — Adapter API (`adapter-api`)

Goal: the contract that all language adapters (PlantUML now, Mermaid later) must implement.

1. Define `DiagramAdapter` interface: `parse(source: String): ParseResult` and `generate(diagram: Diagram): String`.
2. Define `ParseResult` sealed type (`Success(Diagram)` / `Failure(errors: List<ParseError>)`).
3. Define `ParseError` type (message, optional location/line info).
4. Define adapter capability/metadata interface (e.g., language id, file extensions) — kept minimal.
5. Write a `FakeAdapter` test double in `adapter-api`'s test sources.
6. Unit tests asserting the interface contract using `FakeAdapter` (round-trip behavior expectations, error propagation).

**Definition of done:** `adapter-api` depends only on `core`'s domain model types; no PlantUML-specific types leak into it.

---

## Milestone 3 — PlantUML C4 Adapter: Parsing (`adapter-plantuml-c4`)

Goal: parse PlantUML C4 source into the core `Diagram` model, incrementally by construct.

1. Set up `adapter-plantuml-c4` module skeleton implementing `DiagramAdapter` (generate can throw `NotImplemented` for now).
2. Implement parsing of element declarations only (e.g., `Person`, `System`) → `Element` list. Tests with sample snippets.
3. Implement parsing of relationships (e.g., `Rel(...)`) → `Relationship` list. Tests.
4. Implement parsing of boundaries (e.g., `System_Boundary`, `Container_Boundary`) → `Boundary` list, including nesting. Tests.
5. Implement parsing of element/relationship properties (labels, technology, description). Tests.
6. Implement error handling for malformed/unrecognized syntax → `ParseError` with useful messages. Tests with intentionally broken input.

**Definition of done:** parser handles a representative real-world C4 PlantUML sample end-to-end (elements + relationships + boundaries + properties), with parser tests for each construct plus at least one malformed-input test per construct.

---

## Milestone 4 — PlantUML C4 Adapter: Generation

Goal: generate valid PlantUML C4 source from the core `Diagram` model.

1. Implement generation of element declarations from `Diagram.elements`. Tests.
2. Implement generation of relationships. Tests.
3. Implement generation of boundaries (including nesting). Tests.
4. Implement generation of properties/labels matching PlantUML C4 syntax. Tests.
5. Round-trip tests: `generate(parse(source)) ≈ source` (semantically, not necessarily byte-identical) for several sample diagrams.
6. Round-trip tests the other direction: `parse(generate(diagram)) == diagram` for several constructed `Diagram` instances.

**Definition of done:** adapter fully implements `DiagramAdapter`; round-trip tests pass in both directions for representative diagrams.

---

## Milestone 5 — Core Editing Logic (`core`)

Goal: command-based editing of the `Diagram` model with undo/redo, independent of any language or UI.

1. Define `Command` interface with `execute(diagram: Diagram): Diagram` and `undo(diagram: Diagram): Diagram` (or an equivalent state-transition pattern — decide and document the chosen pattern first).
2. Implement `AddElementCommand` + execute/undo tests.
3. Implement `RemoveElementCommand` (with cascading relationship handling) + execute/undo tests.
4. Implement `AddRelationshipCommand` + execute/undo tests.
5. Implement `RemoveRelationshipCommand` + execute/undo tests.
6. Implement `EditPropertyCommand` (elements and relationships) + execute/undo tests.
7. Implement `AddBoundaryCommand` / `EditBoundaryCommand` + execute/undo tests.
8. Implement a `CommandHistory` (undo/redo stack) service + tests (execute, undo, redo, redo-invalidated-by-new-command).
9. Implement model validation service (uses `Diagram` invariants from Milestone 1, surfaces violations before commit) + tests.

**Definition of done:** every command has execute/undo tests; this command logic (like the domain model in Milestone 1) has no dependency on `adapter-api` or `adapter-plantuml-c4` directly — see Milestone 6 for wiring.

---

## Milestone 6 — Core ↔ Adapter Integration

Goal: connect edits to source regeneration and keep source/model in sync.

1. Define a `DiagramSession` (or similarly named) coordinator in `core` that holds a `Diagram`, a `DiagramAdapter`, and a `CommandHistory`, and regenerates source text after each command. Integration tests.
2. Implement "apply external source edit" flow: re-parse edited source, reconcile with current model (define and document the reconciliation strategy — e.g., full replace vs. diff-based merge — as an assumption to confirm). Integration tests.
3. Handle parse-failure-on-reconcile gracefully (keep last good model, surface errors) + tests.
4. End-to-end integration tests: sequence of visual edits → verify generated source at each step; manual source edit → verify model updates.

**Definition of done:** a full edit→regenerate→reparse loop is tested end-to-end without any UI involved.

---

## Milestone 7 — UI: Read-Only Viewer (`ui`)

Goal: minimal Compose UI that displays a `Diagram` from `core`, no editing yet.

1. Define UI-side view models/state holders that observe a `DiagramSession` (read-only).
2. Build a simple tree/list view of elements, grouped by boundary.
3. Build a simple list view of relationships.
4. Wire a sample `DiagramSession` (backed by the PlantUML adapter) into a runnable Compose desktop preview for manual testing.

**Definition of done:** running the Compose preview against a sample PlantUML file displays its elements/relationships/boundaries correctly; no editing capability yet.

---

## Milestone 8 — UI: Basic Editing

Goal: users can perform core edits visually, dispatching `core` commands.

1. Add "add element" UI flow (form/dialog) dispatching `AddElementCommand`.
2. Add "edit element properties" UI flow dispatching `EditPropertyCommand`.
3. Add "add relationship" UI flow dispatching `AddRelationshipCommand`.
4. Add "remove element/relationship" UI flow dispatching remove commands.
5. Wire undo/redo UI controls to `CommandHistory`.
6. UI/integration tests for each flow (state changes correctly after each user action).

**Definition of done:** a user can build a small diagram from scratch through the UI alone, with working undo/redo.

---

## Milestone 9 — UI: Source View & Two-Way Sync

Goal: expose the generated source and support manual edits flowing back into the model.

1. Add a source text panel that live-updates from `DiagramSession` after visual edits.
2. Add manual-edit support in the source panel, wired to the "apply external source edit" flow from Milestone 6.
3. Add visible parse-error feedback in the UI when manual edits fail to parse.
4. UI/integration tests: visual edit reflected in source; manual edit reflected in visual model; broken manual edit shows an error without corrupting state.

**Definition of done:** both editing directions (visual → source, source → visual) work and are covered by tests.

---

## Milestone 10 — IntelliJ Plugin Integration

Goal: run Diagram Composer as an IntelliJ tool window against real PlantUML C4 files.

1. Register a tool window in `plugin.xml` and embed the Compose UI inside it.
2. Detect and associate `.puml`/PlantUML C4 files with the tool window (file type/editor association logic).
3. Load a real file's content into a `DiagramSession` on open; save edits back to disk.
4. Confirm interoperability with an existing PlantUML rendering plugin (no in-house rendering) — document how they coexist (e.g., editing side-by-side with the rendering preview).
5. Manual/integration test pass: open, edit, save, and re-open a real `.puml` file; verify no data loss.

**Definition of done:** plugin can be built and run in a sandbox IDE instance; a real PlantUML C4 file can be opened, edited visually, and saved without corrupting content outside the plugin's understanding (e.g., comments, unsupported syntax) — define and document this preservation guarantee explicitly.

---

## Milestone 11 — Polish & Mermaid-Readiness

Goal: harden the first release and confirm the architecture is genuinely language-independent.

1. Edge-case pass on parser (comments, unusual whitespace, partially-supported syntax) + regression tests for any bugs found.
2. Edge-case pass on core commands (empty diagrams, duplicate names, self-relationships) + regression tests.
3. Update `architecture.md`, `adapters.md`, `product.md`, `ui.md` to reflect final implemented behavior.
4. Review `adapter-api` against a hypothetical Mermaid flowchart adapter (no implementation) to confirm no PlantUML-specific assumptions leaked in; document findings/adjustments needed. Mermaid diagram types other than flowchart (sequence, class, gantt, etc.) are out of scope — they don't map onto the same `Element`/`Relationship`/`Boundary` model and would need their own adapter module if pursued later; no such module is planned yet.
5. Create the `adapter-mermaid-flowchart` module skeleton (empty `DiagramAdapter` implementation, builds and has a placeholder test) — this is the point where creating it stops being speculative, since the review in task 4 is what it's needed for. Actual Mermaid flowchart parsing/generation is out of scope for this milestone.
6. Update `development.md` / `engineering.md` with setup, build, and contribution instructions reflecting the real project.

**Definition of done:** all "Definition of Done" criteria from `ai-context.md` §11 hold for the whole project; docs match implementation.

---

## Milestone 12 — JetBrains Marketplace Publishing (deferred)

Goal: publish the plugin somewhere IntelliJ users can install it from directly, beyond the GitHub Release set up in Milestone 0.

This milestone is intentionally placed last and is **not required** for the plan's earlier definition of "done" — Milestone 0 already produces a tagged GitHub Release with the built artifact on every merge to `main`, which is sufficient until we decide to distribute publicly. Only take this on once the plugin is stable enough to support external users.

1. Create a JetBrains Marketplace publisher account and plugin listing (manual, one-time; not a coding task).
2. Add plugin metadata required for listing (description, changelog format, vendor info) to `plugin.xml` / Gradle IntelliJ plugin config.
3. Set up a signing certificate for the plugin and store it in GitHub Actions secrets.
4. Extend `.github/workflows/release.yml` (or add a separate `publish.yml` triggered by the same tag) to run the Gradle IntelliJ plugin's publish task against the Marketplace, gated behind a manual approval step (e.g. a GitHub Environment with required reviewers) so publishing isn't fully automatic on every merge.
5. Dry-run the publish flow against the Marketplace's beta/hidden channel before enabling public visibility.

**Definition of done:** a tagged release can be manually promoted to the JetBrains Marketplace through CI, without every `main` merge auto-publishing publicly.

---

## Summary of Milestone Order & Dependencies

```
M0 Scaffolding (+ CI/CD)
│
└── M1 core domain model
    │
    ├── M2 adapter-api
    │   │
    │   └── M3 adapter-plantuml-c4 (parse)
    │       │
    │       └── M4 adapter-plantuml-c4 (generate)
    │
    └── M5 core (commands)
        │
        └── M6 core ↔ adapter integration   [needs M4 + M5]
            │
            └── M7 UI read-only viewer
                │
                └── M8 UI editing
                    │
                    └── M9 UI source sync
                        │
                        └── M10 IntelliJ plugin integration
                            │
                            └── M11 Polish & Mermaid-readiness
                                │
                                └── M12 Marketplace publishing (deferred, optional)
```

Each milestone's tasks are ordered so you can stop after any single task with a compiling, tested, committable state.
