package org.octopusden.octopus.provisioning.service.configuration

import org.octopusden.octopus.provisioning.common.dto.configuration.ProvisioningConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * The service's settings. The catalogue is per-environment configuration, served as it is written; it is
 * not validated here.
 *
 * ```yaml
 * component-provisioning:
 *   configurations:
 *     - id: regular
 *       version: 1
 *       origins: [ { profile: regular-external }, { profile: regular-internal } ]
 * ```
 *
 * The calls to vcs-facade and teamcity-automation-service go through the gateway with a technical
 * account (`technical-account.username` and `password`, from Vault), sent as HTTP Basic auth.
 * The gateway turns it into a Bearer token. The components registry is called with the caller's token.
 */
@ConfigurationProperties("component-provisioning")
data class ComponentProvisioningProperties(
    val configurations: List<ProvisioningConfiguration> = emptyList(),
)
