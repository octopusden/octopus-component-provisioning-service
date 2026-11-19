package org.octopusden.octopus.provisioning.common.dto

/** The body of a provisioning request. */
data class ProvisioningRequest(
    /** Only for a component without an origin, and it must name a `selectable` configuration. */
    val configurationId: String? = null,
    val buildChain: BuildChainRequest? = null,
    /**
     * Per repository URL, the values for the fields its entry leaves open. `useExisting: true` is accepted for
     * any repository.
     */
    val repositories: List<RepositoryRequest> = emptyList(),
)
