package org.octopusden.octopus.provisioning.common.dto

/** The build chain options of a resolved configuration. Extra stages are never open. */
data class ResolvedBuildChain(
    val parentProjectId: ResolvedField,
    val minorVersion: ResolvedField,
    val extraStages: List<ExtraStage> = emptyList(),
)
