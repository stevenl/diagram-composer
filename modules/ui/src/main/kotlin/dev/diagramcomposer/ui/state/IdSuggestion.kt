package dev.diagramcomposer.ui.state

import dev.diagramcomposer.core.model.Diagram
import dev.diagramcomposer.core.model.EntityId
import dev.diagramcomposer.core.model.RelationshipId

private const val FALLBACK_STEM = "element"
private const val RELATIONSHIP_STEM = "rel"

/**
 * Suggests an [EntityId] for a new entity named [name], for the "add entity"
 * dialog's auto-suggested identifier field (docs/ui.md §5.3). The user may
 * still override it before confirming.
 *
 * The suggestion is a valid PlantUML alias — `[A-Za-z_][A-Za-z0-9_]*`, per
 * `PlantUmlC4Parser`'s `MACRO_CALL` regex — built by camel-casing [name]'s
 * words, then deduplicated against [diagram]'s existing entity ids by
 * appending a numeric suffix.
 */
fun suggestEntityId(
    name: String,
    diagram: Diagram,
): EntityId {
    val taken = diagram.entities.map { it.id.value }.toSet()
    return EntityId(uniqueValue(camelCaseIdentifier(name), taken))
}

/**
 * Suggests a [RelationshipId] for a new relationship, for the "add
 * relationship" dialog. Relationships have no natural human-readable
 * identifier the way an entity's name does — `PlantUmlC4Generator` only
 * round-trips this id via its `$id=` convention — so this is a simple
 * counter-based id ("rel1", "rel2", ...), deduplicated against [diagram]'s
 * existing relationship ids the same way [suggestEntityId] deduplicates
 * entity ids.
 */
fun suggestRelationshipId(diagram: Diagram): RelationshipId {
    val taken = diagram.relationships.map { it.id.value }.toSet()
    var suffix = 1
    while ("$RELATIONSHIP_STEM$suffix" in taken) suffix++
    return RelationshipId("$RELATIONSHIP_STEM$suffix")
}

private fun camelCaseIdentifier(name: String): String {
    val words = name.split(Regex("[^A-Za-z0-9]+")).filter { it.isNotBlank() }
    if (words.isEmpty()) return FALLBACK_STEM

    val camelCased =
        words
            .mapIndexed { index, word ->
                val lower = word.lowercase()
                if (index == 0) lower else lower.replaceFirstChar { it.uppercase() }
            }.joinToString("")

    return if (camelCased.first().isDigit()) "_$camelCased" else camelCased
}

/** Returns [base] unless it's in [taken], in which case `base` + the first free numeric suffix (starting at 2). */
private fun uniqueValue(
    base: String,
    taken: Set<String>,
): String {
    if (base !in taken) return base
    var suffix = 2
    while ("$base$suffix" in taken) suffix++
    return "$base$suffix"
}
