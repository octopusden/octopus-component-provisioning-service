package org.octopusden.octopus.provisioning.common.dto.configuration

data class ExtraStage(
    val name: String,
    val template: String? = null,
    val checkout: String? = null,
    val parametersFromCompile: List<String> = emptyList(),
)
