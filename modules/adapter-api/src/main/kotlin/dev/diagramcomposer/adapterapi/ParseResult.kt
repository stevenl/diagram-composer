package dev.diagramcomposer.adapterapi

import dev.diagramcomposer.core.model.Diagram

/**
 * Outcome of [DiagramAdapter.parse].
 *
 * Modelled as a sealed result type — rather than a nullable [Diagram] or a
 * thrown exception — so callers must handle the failure case explicitly and
 * can see every collected [ParseError] at once, matching docs/adapters.md
 * §9's requirement that the parser "must never silently discard
 * information."
 */
sealed interface ParseResult {
    data class Success(
        val diagram: Diagram,
    ) : ParseResult

    data class Failure(
        val errors: List<ParseError>,
    ) : ParseResult {
        init {
            require(errors.isNotEmpty()) { "Failure must contain at least one ParseError" }
        }
    }
}
