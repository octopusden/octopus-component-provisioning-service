package org.octopusden.octopus.provisioning.service.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

/** Base URL of the components registry; declared, not used yet. */
@ConfigurationProperties("components-registry")
data class ComponentsRegistryProperties(
    val url: String? = null,
)
