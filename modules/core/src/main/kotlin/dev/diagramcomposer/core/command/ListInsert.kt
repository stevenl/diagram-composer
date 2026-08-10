package dev.diagramcomposer.core.command

/**
 * Inserts [element] into [list] at [index], or appends it if [index] is
 * `null` or beyond the end of [list]. Shared by every command that places
 * an item into an ordered children list ([dev.diagramcomposer.core.model.Diagram.rootChildren],
 * [dev.diagramcomposer.core.model.Boundary.children]) or a flat one
 * ([dev.diagramcomposer.core.model.Diagram.relationships]).
 *
 * A negative [index] is treated as `0` (insert at the start) rather than
 * throwing, so callers restoring a memento never have to special-case "the
 * item used to be first."
 */
internal fun <T> insertAt(
    list: List<T>,
    element: T,
    index: Int?,
): List<T> {
    val result = list.toMutableList()
    val at = index?.coerceIn(0, result.size) ?: result.size
    result.add(at, element)
    return result
}
