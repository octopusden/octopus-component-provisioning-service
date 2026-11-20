package org.octopusden.octopus.provisioning.common.dto.resolved

/** The configuration that serves an origin, with each field marked fixed or open; the Portal builds its form from it. */
data class ResolvedConfiguration(
    val id: String,
    val version: Int,
    val buildChain: ResolvedBuildChain? = null,
    val repositories: List<ResolvedRepository> = emptyList(),
)
