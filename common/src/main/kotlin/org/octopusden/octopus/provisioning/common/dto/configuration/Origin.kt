package org.octopusden.octopus.provisioning.common.dto.configuration

/** A component profile, or a component template with an optional version. Exactly one of the two is set. */
data class Origin(
    val profile: String? = null,
    val componentTemplate: String? = null,
    val componentTemplateVersion: String? = null,
)
