package org.octopusden.octopus.provisioning.service.service

import org.octopusden.octopus.provisioning.common.dto.provisioning.ProvisioningRequest
import org.octopusden.octopus.provisioning.common.dto.provisioning.ProvisioningStatus
import org.octopusden.octopus.provisioning.common.dto.resolved.ResolvedConfiguration
import org.octopusden.octopus.provisioning.service.service.NotImplementedException
import org.springframework.stereotype.Service

// The contract is declared before the behaviour exists, so the parameters are not used yet.
@Suppress("UnusedParameter")
@Service
class ProvisioningService {
    /** The configuration that serves the origin, for the given repository URLs. */
    fun resolveConfiguration(
        profile: String?,
        componentTemplate: String?,
        componentTemplateVersion: String?,
        urls: List<String>,
    ): ResolvedConfiguration = throw NotImplementedException()

    /** The state of each repository and of the build chain of the component. */
    fun getStatus(componentId: String): ProvisioningStatus = throw NotImplementedException()

    /** Provisions the component, or continues after a failure. */
    fun provision(
        componentId: String,
        request: ProvisioningRequest,
    ): ProvisioningStatus = throw NotImplementedException()
}
