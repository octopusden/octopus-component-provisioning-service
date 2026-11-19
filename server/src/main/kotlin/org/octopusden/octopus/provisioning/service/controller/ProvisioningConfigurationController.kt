package org.octopusden.octopus.provisioning.service.controller

import org.octopusden.octopus.provisioning.common.dto.ProvisioningConfiguration
import org.octopusden.octopus.provisioning.service.service.ProvisioningConfigurationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rest/api/1/configurations")
class ProvisioningConfigurationController(
    private val service: ProvisioningConfigurationService,
) {
    @GetMapping
    fun getConfigurations(): List<ProvisioningConfiguration> = service.getConfigurations()
}
