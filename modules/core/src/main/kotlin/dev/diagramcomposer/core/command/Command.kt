package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Diagram

/**
 * A single undoable edit to a [Diagram] (docs/architecture.md §7 "Undo and
 * Redo", docs/implementation-plan.md Milestone 5 task 1).
 *
 * ## Chosen pattern
 *
 * [Diagram] is an immutable, self-validating data class (see its doc
 * comment) — there's no mutable model instance a command could hold a
 * reference to and mutate in place. So `Command` is a pure state
 * transition rather than the no-argument `execute()`/`undo()` sketched in
 * docs/engineering.md §7 (which predates Milestone 1's decision to make
 * `Diagram` immutable): [execute] takes the diagram it applies to and
 * returns the *new* diagram; [undo] takes the diagram *after* [execute]
 * and returns the diagram *before* it. Neither method mutates its
 * argument — [Diagram] can't be mutated — they only ever return a new
 * instance.
 *
 * Concrete commands are free to capture whatever they need in order to
 * invert themselves:
 *
 * - Purely additive commands (e.g. [AddEntityCommand]) can always invert
 *   from their own constructor arguments alone — nothing needs capturing.
 * - Destructive commands (e.g. [RemoveEntityCommand]) generally can't:
 *   "restore what was removed, in the position it was removed from"
 *   requires state that doesn't exist until [execute] actually runs. Those
 *   commands capture a memento of the removed state during [execute] and
 *   use it in [undo].
 *
 * Because of the second case, **[undo] requires a prior [execute] call on
 * the same command instance** — calling [undo] first throws
 * [IllegalStateException]. A command instance is not reusable across
 * unrelated diagrams once it has captured memento state; construct a new
 * instance per edit, the same way [CommandHistory] does.
 *
 * [CommandHistory] is what actually sequences [execute]/[undo]/redo
 * (`execute` again) calls; commands themselves know nothing about undo/redo
 * history or IntelliJ's undo stack (that's `intellij-plugin`'s job,
 * Milestone 10 — docs/engineering.md §7's "command stack integrates with
 * IntelliJ undo management").
 */
interface Command {
    /** Applies this edit to [diagram], returning the resulting diagram. */
    fun execute(diagram: Diagram): Diagram

    /**
     * Reverses this edit. [diagram] must be the diagram [execute] returned
     * (or an equivalent state reached via redo); the result is the diagram
     * as it was immediately before [execute] ran.
     */
    fun undo(diagram: Diagram): Diagram
}
