package org.octopusden.octopus.provisioning.service.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

/** Base URL of vcs-facade; declared, not used yet. */
@ConfigurationProperties("vcs-facade")
data class VcsFacadeProperties(
    val url: String? = null,
)
