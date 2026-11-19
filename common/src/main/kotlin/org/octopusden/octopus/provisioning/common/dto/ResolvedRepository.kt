package org.octopusden.octopus.provisioning.common.dto

/**
 * One requested repository URL with its resolved fields. [entry] is the repository entry that matched the URL,
 * or null when none did.
 */
data class ResolvedRepository(
    val url: String,
    val entry: RepositoryEntry? = null,
    val useExisting: ResolvedField,
    val template: TemplateRef? = null,
)
