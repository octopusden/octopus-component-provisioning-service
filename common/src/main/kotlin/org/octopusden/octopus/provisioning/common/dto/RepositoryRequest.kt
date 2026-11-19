package org.octopusden.octopus.provisioning.common.dto

/** The caller's values for one repository, identified by its URL. */
data class RepositoryRequest(
    val url: String,
    val useExisting: Boolean? = null,
)
