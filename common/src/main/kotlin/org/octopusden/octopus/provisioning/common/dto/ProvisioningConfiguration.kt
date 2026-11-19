package org.octopusden.octopus.provisioning.common.dto

/**
 * One provisioning configuration, as the catalogue holds it.
 *
 * ```yaml
 * - id: solution
 *   version: 3
 *   origins:
 *     - { componentTemplate: solution }
 *   repositories:
 *     - template:
 *         byLabel:
 *           helm: "<groupId>:helm-template:<version>"
 *   buildChain:
 *     parentProjectId: <build server project id>
 * ```
 */
data class ProvisioningConfiguration(
    val id: String,
    val version: Int,
    /** The component profiles or component templates this configuration serves. */
    val origins: List<Origin> = emptyList(),
    /** Whether a caller may name this configuration for a component that has no origin. */
    val selectable: Boolean = false,
    val repositories: List<RepositoryEntry> = emptyList(),
    val buildChain: BuildChain? = null,
)
