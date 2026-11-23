package org.octopusden.octopus.provisioning.service.service

import org.octopusden.octopus.provisioning.common.dto.configuration.ProvisioningConfiguration
import org.octopusden.octopus.provisioning.service.configuration.ComponentProvisioningProperties
import org.springframework.stereotype.Service

@Service
class ProvisioningConfigurationService(
    private val properties: ComponentProvisioningProperties,
) {
    /** The catalogue as configured. */
    fun getConfigurations(): List<ProvisioningConfiguration> = properties.configurations
}
