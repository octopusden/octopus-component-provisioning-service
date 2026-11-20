package org.octopusden.octopus.provisioning.common.dto.provisioning

/**
 * Values for the build chain fields the configuration leaves open. A fixed field may be omitted; a different
 * value is a 422.
 */
data class BuildChainRequest(
    val parentProjectId: String? = null,
    val minorVersion: String? = null,
)
