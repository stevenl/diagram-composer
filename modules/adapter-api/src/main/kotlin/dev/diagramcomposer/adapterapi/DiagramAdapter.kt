package dev.diagramcomposer.adapterapi

import dev.diagramcomposer.core.model.Diagram

/**
 * The contract every language adapter (PlantUML C4 first, per Milestone 3;
 * Mermaid flowchart later) implements (docs/adapters.md §8,
 * docs/architecture.md §5.2).
 *
 * This is a deliberately minimal slice of the fuller conceptual interface
 * sketched in docs/adapters.md §8 / docs/architecture.md §5.2, which also
 * lists `validate`, `supports`, `capabilities`, `entityTypes`,
 * `relationshipTypes`, and `propertyDefinitions`. Those are deferred until
 * an actual adapter needs them (docs/implementation-plan.md Milestone 2
 * task 4; ai-context.md §5 "Keep It Simple" — avoid speculative
 * abstractions). Adding them later is source-compatible for existing
 * implementations as long as they're given default implementations or
 * introduced as a separate interface.
 *
 * The core model must never depend on this interface or on any adapter —
 * `parse`/`generate` (plus [metadata]) are the adapter's only points of
 * contact with the rest of the system.
 */
interface DiagramAdapter {
    val metadata: AdapterMetadata

    /**
     * Parses [source] text into a [Diagram]. Never throws for malformed
     * input — syntax problems are reported via [ParseResult.Failure]
     * instead (docs/adapters.md §9).
     */
    fun parse(source: String): ParseResult

    /**
     * Generates source text for [diagram]. For the MVP, adapters may fully
     * regenerate the source file rather than performing a targeted/
     * incremental update (docs/adapters.md §10, §13).
     */
    fun generate(diagram: Diagram): String
}
