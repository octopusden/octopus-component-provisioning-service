package org.octopusden.octopus.provisioning.common.dto.configuration

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
