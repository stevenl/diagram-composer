package dev.diagramcomposer.core.command

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.Entity

/**
 * Replaces the entity with id [newEntity.id][Entity.id] with [newEntity] in
 * place — covers renaming, changing type, and editing any other property
 * (docs/architecture.md §7's "Rename entity", "Change type", "Modify
 * properties" examples), since [Entity] is a plain data class and the
 * caller can already build the desired new value with `.copy(...)`. A
 * single full-value replace is simpler than a menu of narrower
 * `RenameEntityCommand` / `ChangeEntityTypeCommand` / ... commands that
 * would all do the same thing (ai-context.md §5 "Keep It Simple").
 *
 * The entity being replaced is looked up by [newEntity]'s id, so an entity
 * cannot be given a *different* id through this command — that would be
 * indistinguishable from removing one entity and adding an unrelated one,
 * which [RemoveEntityCommand] / [AddEntityCommand] already model
 * explicitly.
 *
 * [execute] captures the replaced entity's prior value for [undo] (see
 * [Command]'s doc comment).
 */
class EditEntityCommand(
    private val newEntity: Entity,
) : Command {
    private var previousEntity: Entity? = null

    override fun execute(diagram: Diagram): Diagram {
        val index = diagram.entities.indexOfFirst { it.id == newEntity.id }
        require(index >= 0) { "Entity '${newEntity.id}' not found" }

        previousEntity = diagram.entities[index]
        return diagram.copy(entities = diagram.entities.toMutableList().apply { this[index] = newEntity })
    }

    override fun undo(diagram: Diagram): Diagram {
        val previous = checkNotNull(previousEntity) { "undo() called before execute()" }
        val index = diagram.entities.indexOfFirst { it.id == newEntity.id }
        check(index >= 0) { "Entity '${newEntity.id}' not found" }

        return diagram.copy(entities = diagram.entities.toMutableList().apply { this[index] = previous })
    }
}
