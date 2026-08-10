package dev.diagramcomposer.adapterapi

import dev.diagramcomposer.core.model.Diagram

/**
 * Outcome of [DiagramSession.applyExternalEdit].
 *
 * Modelled the same way as [ParseResult] (a sealed result, not a nullable
 * [Diagram] or a thrown exception) so callers must handle a rejected edit
 * explicitly, per docs/implementation-plan.md Milestone 6 task 3 ("handle
 * parse-failure-on-reconcile gracefully").
 */
sealed interface ApplyExternalEditResult {
    /** The edited source parsed successfully and is now this session's diagram. */
    data class Applied(
        val diagram: Diagram,
    ) : ApplyExternalEditResult

    /**
     * The edited source failed to parse. [DiagramSession.diagram] and
     * [DiagramSession.sourceText] are unchanged — the last good model and
     * its corresponding source are preserved, exactly as they were before
     * this call (docs/adapters.md §9 "must never silently discard
     * information" — rejecting an unparsable edit outright, rather than
     * guessing at a partial merge, is what upholds that here).
     */
    data class Rejected(
        val errors: List<ParseError>,
    ) : ApplyExternalEditResult {
        init {
            require(errors.isNotEmpty()) { "Rejected must contain at least one ParseError" }
        }
    }
}
