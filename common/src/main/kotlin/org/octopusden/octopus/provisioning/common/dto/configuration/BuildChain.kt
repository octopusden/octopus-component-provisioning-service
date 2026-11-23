package org.octopusden.octopus.provisioning.common.dto.configuration

/** The build chain options passed to the automation service. An absent field is open for the caller. */
data class BuildChain(
    val parentProjectId: String? = null,
    val minorVersion: String? = null,
    val extraStages: List<ExtraStage> = emptyList(),
)
