package dev.diagramcomposer.adapter.plantumlc4

/**
 * Parses the argument list of a single PlantUML C4 macro call, e.g. the
 * `alias, "Label", "Some, tech", $tags="a,b"` inside
 * `Container(alias, "Label", "Some, tech", $tags="a,b")`.
 *
 * Splitting is done manually (rather than a single `split(",")`) because
 * commas may appear inside double-quoted argument values and must not be
 * treated as argument separators there. Quotes themselves are stripped from
 * the returned tokens, and `\"` inside a quoted value is unescaped to `"`.
 *
 * The result separates:
 * - [positional]: arguments in declaration order, e.g. `alias, "Label"`.
 * - [named]: `$name=value` arguments (PlantUML C4's convention for optional
 *   parameters such as `$tags`, `$descr`, `$techn`), keyed by `name`.
 */
internal data class PlantUmlArgs(
    val positional: List<String>,
    val named: Map<String, String>,
) {
    companion object {
        private val NAMED_ARG = Regex("^\\$([A-Za-z_][A-Za-z0-9_]*)\\s*=(.*)$", RegexOption.DOT_MATCHES_ALL)

        fun parse(argsSource: String): PlantUmlArgs {
            val tokens = splitTopLevel(argsSource)
            val positional = mutableListOf<String>()
            val named = mutableMapOf<String, String>()

            for (token in tokens) {
                val match = NAMED_ARG.matchEntire(token)
                if (match != null) {
                    named[match.groupValues[1]] = match.groupValues[2]
                } else {
                    positional += token
                }
            }

            return PlantUmlArgs(positional, named)
        }

        /**
         * Splits [argsSource] on top-level commas — commas that are not
         * inside a double-quoted value — and strips the surrounding quotes
         * (if any) and quote-escaping from each resulting token.
         */
        private fun splitTopLevel(argsSource: String): List<String> {
            if (argsSource.isBlank()) return emptyList()

            val tokens = mutableListOf<String>()
            val current = StringBuilder()
            var inQuotes = false
            var i = 0
            while (i < argsSource.length) {
                val c = argsSource[i]
                when {
                    c == '\\' && inQuotes && i + 1 < argsSource.length -> {
                        current.append(argsSource[i + 1])
                        i++
                    }

                    c == '"' -> {
                        inQuotes = !inQuotes
                    }

                    c == ',' && !inQuotes -> {
                        tokens += current.toString().trim()
                        current.clear()
                    }

                    else -> {
                        current.append(c)
                    }
                }
                i++
            }
            tokens += current.toString().trim()
            return tokens
        }
    }
}

/** Splits a comma-separated tag value (e.g. from `$tags="internal,billing"`) into individual tags. */
internal fun String.splitTags(): List<String> = split(",").map { it.trim() }.filter { it.isNotEmpty() }
