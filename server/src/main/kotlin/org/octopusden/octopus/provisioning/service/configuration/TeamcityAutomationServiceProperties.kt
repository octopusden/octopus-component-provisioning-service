package org.octopusden.octopus.provisioning.service.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

/** Base URL of the TeamCity automation service; declared, not used yet. */
@ConfigurationProperties("teamcity-automation-service")
data class TeamcityAutomationServiceProperties(
    val url: String? = null,
)
