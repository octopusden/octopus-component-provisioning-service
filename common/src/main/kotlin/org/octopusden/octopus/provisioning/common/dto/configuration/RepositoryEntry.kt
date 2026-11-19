package org.octopusden.octopus.provisioning.common.dto.configuration

/**
 * A repository entry. An entry without [urlRegex] is the default entry. An absent [useExisting] is open for
 * the user to choose; an absent [template] means the repository is created empty.
 */
data class RepositoryEntry(
    val urlRegex: String? = null,
    val useExisting: Boolean? = null,
    val template: TemplateRef? = null,
)
