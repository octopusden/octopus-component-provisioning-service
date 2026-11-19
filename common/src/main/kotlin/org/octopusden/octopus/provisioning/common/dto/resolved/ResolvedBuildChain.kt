package org.octopusden.octopus.provisioning.common.dto.resolved

import org.octopusden.octopus.provisioning.common.dto.configuration.ExtraStage

/** The build chain options of a resolved configuration. Extra stages are never open. */
data class ResolvedBuildChain(
    val parentProjectId: ResolvedField,
    val minorVersion: ResolvedField,
    val extraStages: List<ExtraStage> = emptyList(),
)
