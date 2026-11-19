package org.octopusden.octopus.provisioning.service.configuration

import org.octopusden.octopus.provisioning.common.dto.ProvisioningConfiguration
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
 *   clients:
 *     vcs-facade:
 *       url: https://vcs-facade.example.com
 * ```
 *
 * The service account for the downstream calls comes from the security library's
 * `spring.security.oauth2.client.registration.keycloak.client-id` and `client-secret`.
 */
@ConfigurationProperties("component-provisioning")
data class ComponentProvisioningProperties(
    val configurations: List<ProvisioningConfiguration> = emptyList(),
    val clients: Clients = Clients(),
) {
    /** Base URLs of the services this one will call; declared here, not used yet. */
    data class Clients(
        val vcsFacade: Client = Client(),
        val automationService: Client = Client(),
        val componentsRegistry: Client = Client(),
    )

    data class Client(
        val url: String? = null,
    )
}
