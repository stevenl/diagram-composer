package dev.diagramcomposer.core.validation

/**
 * Outcome of [DiagramValidator.validate].
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult

    data class Invalid(
        val reason: String,
    ) : ValidationResult
}
