package dev.diagramcomposer.adapterapi

/**
 * Outcome of [DiagramSession.open] — parsing an initial source file is
 * itself fallible (e.g. opening a `.puml` file that doesn't parse), so this
 * mirrors [ParseResult] rather than throwing.
 */
sealed interface OpenResult {
    data class Success(
        val session: DiagramSession,
    ) : OpenResult

    data class Failure(
        val errors: List<ParseError>,
    ) : OpenResult {
        init {
            require(errors.isNotEmpty()) { "Failure must contain at least one ParseError" }
        }
    }
}
