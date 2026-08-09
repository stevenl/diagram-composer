package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Boundary
import dev.diagramcomposer.core.model.Diagram

/**
 * Replaces the boundary with id [newBoundary.id][Boundary.id] with
 * [newBoundary] in place — see [EditEntityCommand]'s doc comment for why a
 * full-value replace is used instead of narrower per-field commands, and
 * why the id itself can't change through this command.
 *
 * Unlike [EditEntityCommand]/[EditRelationshipCommand], this intentionally
 * allows [newBoundary] to have a different [Boundary.children] than the
 * boundary it replaces — restructuring which entities/boundaries a
 * boundary directly contains this way is deliberately not blocked, since
 * [Diagram]'s own invariants (every entity/boundary appears exactly once
 * across the whole tree) already reject any resulting inconsistency; there
 * is no extra rule to enforce here beyond what [Diagram] already checks at
 * construction.
 *
 * [execute] captures the replaced boundary's prior value for [undo] (see
 * [Command]'s doc comment).
 */
class EditBoundaryCommand(
    private val newBoundary: Boundary,
) : Command {
    private var previousBoundary: Boundary? = null

    override fun execute(diagram: Diagram): Diagram {
        val index = diagram.boundaries.indexOfFirst { it.id == newBoundary.id }
        require(index >= 0) { "Boundary '${newBoundary.id}' not found" }

        previousBoundary = diagram.boundaries[index]
        return diagram.copy(boundaries = diagram.boundaries.toMutableList().apply { this[index] = newBoundary })
    }

    override fun undo(diagram: Diagram): Diagram {
        val previous = checkNotNull(previousBoundary) { "undo() called before execute()" }
        val index = diagram.boundaries.indexOfFirst { it.id == newBoundary.id }
        check(index >= 0) { "Boundary '${newBoundary.id}' not found" }

        return diagram.copy(boundaries = diagram.boundaries.toMutableList().apply { this[index] = previous })
    }
}
