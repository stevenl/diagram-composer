package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Diagram

/**
 * Sequences [Command] execution, undo, and redo against a single [Diagram],
 * and owns the current diagram state that results
 * (docs/implementation-plan.md Milestone 5 task 8; docs/architecture.md §7
 * "Undo and Redo").
 *
 * A classic two-stack undo/redo: [execute] pushes onto the undo stack and
 * clears the redo stack (a new edit invalidates whatever was previously
 * available to redo — there's no single well-defined "future" once history
 * has branched); [undo] pops the undo stack and pushes onto the redo stack;
 * [redo] pops the redo stack and pushes back onto the undo stack.
 *
 * This class only tracks the [Command] sequence and the resulting
 * [Diagram] — it has no IntelliJ dependency. Wiring it to IntelliJ's own
 * undo manager (so the plugin's Ctrl+Z integrates with the platform rather
 * than competing with it) is `intellij-plugin`'s job, Milestone 10
 * (docs/engineering.md §7).
 */
class CommandHistory(
    initialDiagram: Diagram,
) {
    var diagram: Diagram = initialDiagram
        private set

    private val undoStack = ArrayDeque<Command>()
    private val redoStack = ArrayDeque<Command>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** Applies [command], making it the new top of the undo stack. */
    fun execute(command: Command): Diagram {
        diagram = command.execute(diagram)
        undoStack.addLast(command)
        redoStack.clear()
        return diagram
    }

    /** Reverses the most recently executed (or redone) command. */
    fun undo(): Diagram {
        check(canUndo) { "Nothing to undo" }
        val command = undoStack.removeLast()
        diagram = command.undo(diagram)
        redoStack.addLast(command)
        return diagram
    }

    /** Re-applies the most recently undone command. */
    fun redo(): Diagram {
        check(canRedo) { "Nothing to redo" }
        val command = redoStack.removeLast()
        diagram = command.execute(diagram)
        undoStack.addLast(command)
        return diagram
    }
}
