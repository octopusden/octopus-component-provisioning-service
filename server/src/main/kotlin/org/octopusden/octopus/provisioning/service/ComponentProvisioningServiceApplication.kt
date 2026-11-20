package org.octopusden.octopus.provisioning.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ComponentProvisioningServiceApplication

fun main(args: Array<String>) {
    runApplication<ComponentProvisioningServiceApplication>(*args)
}
