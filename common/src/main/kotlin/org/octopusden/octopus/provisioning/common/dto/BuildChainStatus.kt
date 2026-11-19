package org.octopusden.octopus.provisioning.common.dto

/** The state of the build chain; [detail] explains a conflict or a failed read. */
data class BuildChainStatus(
    val state: ProvisioningState,
    val detail: String? = null,
)
