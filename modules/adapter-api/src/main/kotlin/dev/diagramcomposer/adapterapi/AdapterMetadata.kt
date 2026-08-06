package dev.diagramcomposer.adapterapi

/**
 * Identifying information a [DiagramAdapter] advertises about the language
 * it supports (docs/adapters.md §6 "Adapter Registration").
 *
 * This is intentionally minimal — the richer capability advertisement
 * sketched in docs/adapters.md §7 ("Supports Boundaries", "Supports
 * Components", etc.) is deferred until an adapter actually exists to
 * motivate what capabilities need to be queried (docs/implementation-plan.md
 * Milestone 2 task 4; ai-context.md §5 "Keep It Simple" — avoid speculative
 * abstractions). `languageId` and `fileExtensions` are kept because even the
 * first adapter (Milestone 3) needs them for IntelliJ file-type association.
 */
data class AdapterMetadata(
    val languageId: String,
    val displayName: String,
    val fileExtensions: List<String>,
) {
    init {
        require(languageId.isNotBlank()) { "languageId must not be blank" }
        require(displayName.isNotBlank()) { "displayName must not be blank" }
        require(fileExtensions.isNotEmpty()) { "fileExtensions must not be empty" }
        require(fileExtensions.all { it.isNotBlank() }) {
            "fileExtensions must not contain blank entries"
        }
    }
}
