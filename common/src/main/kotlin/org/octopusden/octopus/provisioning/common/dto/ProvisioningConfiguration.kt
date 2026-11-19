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

/** A component profile, or a component template with an optional version. Exactly one of the two is set. */
data class Origin(
    val profile: String? = null,
    val componentTemplate: String? = null,
    val componentTemplateVersion: String? = null,
)

/**
 * A repository entry. An entry without [urlRegex] is the default entry. An absent [useExisting] is open for
 * the user to choose; an absent [template] means the repository is created empty.
 */
data class RepositoryEntry(
    val urlRegex: String? = null,
    val useExisting: Boolean? = null,
    val template: TemplateRef? = null,
)

/**
 * The repository template: either one fixed GAV, or a GAV per component label.
 *
 * Both YAML forms bind:
 *
 * ```yaml
 * template: "<groupId>:ww-template:<version>"      # a fixed GAV
 * ```
 *
 * ```yaml
 * template:
 *   byLabel:                                       # picked by the component's label
 *     helm: "<groupId>:helm-template:<version>"
 *     ansible: "<groupId>:ansible-template:<version>"
 * ```
 *
 * Spring Boot's binder turns a plain string into [gav] through [of].
 */
data class TemplateRef(
    val gav: String? = null,
    val byLabel: Map<String, String>? = null,
) {
    companion object {
        @JvmStatic
        fun of(gav: String) = TemplateRef(gav = gav)
    }
}

/** The build chain options passed to the automation service. An absent field is open for the caller. */
data class BuildChain(
    val parentProjectId: String? = null,
    val minorVersion: String? = null,
    val extraStages: List<ExtraStage> = emptyList(),
)

data class ExtraStage(
    val name: String,
    val template: String? = null,
    val checkout: String? = null,
    val parametersFromCompile: List<String> = emptyList(),
)
