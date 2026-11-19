package org.octopusden.octopus.provisioning.common.dto

/** The provisioning status of a component: the selected configuration and the state of each step. */
data class ProvisioningStatus(
    val componentId: String,
    val configurationId: String? = null,
    val configurationVersion: Int? = null,
    val repositories: List<RepositoryStatus> = emptyList(),
    val buildChain: BuildChainStatus? = null,
)
