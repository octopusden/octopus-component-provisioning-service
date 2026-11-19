package org.octopusden.octopus.provisioning.common.dto

/** A field that is fixed with its [value], or open for the caller to supply. */
data class ResolvedField(
    val fixed: Boolean,
    val value: String? = null,
)
