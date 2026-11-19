package org.octopusden.octopus.provisioning.common.dto

/** The state of one repository; [detail] explains a conflict or a failed read. */
data class RepositoryStatus(
    val url: String,
    val state: ProvisioningState,
    val detail: String? = null,
)
