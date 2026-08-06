package dev.diagramcomposer.adapterapi

/**
 * A single problem encountered while parsing source text
 * (docs/adapters.md §9 "Parsing Requirements" — the parser must report
 * validation issues and "must never silently discard information").
 *
 * [line] and [column] are optional: some errors (e.g. "source is empty")
 * don't refer to a specific location, while most syntax errors do. Both are
 * 1-indexed, matching how editors display line/column numbers to users.
 */
data class ParseError(
    val message: String,
    val line: Int? = null,
    val column: Int? = null,
) {
    init {
        require(message.isNotBlank()) { "ParseError message must not be blank" }
        line?.let { require(it >= 1) { "line must be >= 1 if present, was $it" } }
        column?.let { require(it >= 1) { "column must be >= 1 if present, was $it" } }
    }
}
