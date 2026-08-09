package dev.diagramcomposer.core.validation

import dev.diagramcomposer.core.model.Diagram

/**
 * Checks whether a candidate [Diagram] can be constructed, surfacing the
 * result as a [ValidationResult] instead of a thrown exception
 * (docs/implementation-plan.md Milestone 5 task 9).
 *
 * [Diagram] is self-validating (see its doc comment): it enforces its
 * invariants — unique ids, no dangling relationship/boundary references, no
 * entity/boundary missing from or duplicated across the containment tree —
 * by throwing [IllegalArgumentException] from its constructor, and there is
 * deliberately no separate hand-rolled copy of that invariant logic here.
 * Re-implementing the same checks in a second place would risk the two
 * definitions of "valid" drifting apart; instead this wraps construction
 * and translates the outcome.
 *
 * This is a *preview* — it doesn't commit anything. A typical caller
 * (before it exists, [dev.diagramcomposer.core.command.CommandHistory]
 * itself does not use this; a UI layer that wants to show inline errors or
 * grey out a control *before* attempting an edit would) checks validity via
 * [validate] first and only then calls
 * [dev.diagramcomposer.core.command.CommandHistory.execute]:
 *
 * ```kotlin
 * when (val result = DiagramValidator.validate { command.execute(diagram) }) {
 *     is ValidationResult.Valid -> commandHistory.execute(command)
 *     is ValidationResult.Invalid -> showError(result.reason)
 * }
 * ```
 *
 * Catches [IllegalArgumentException] specifically — the type [Diagram]'s
 * `require(...)` calls throw — rather than every [Throwable], so a genuine
 * programming error elsewhere in [candidate] still propagates instead of
 * being silently reported as an "invalid diagram."
 */
object DiagramValidator {
    fun validate(candidate: () -> Diagram): ValidationResult =
        try {
            candidate()
            ValidationResult.Valid
        } catch (e: IllegalArgumentException) {
            ValidationResult.Invalid(e.message ?: "Diagram is invalid")
        }
}
