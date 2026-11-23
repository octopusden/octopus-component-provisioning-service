package org.octopusden.octopus.provisioning.common.dto.provisioning

/**
 * The state of a repository or of the build chain. The constants are the design's `not-started`,
 * `in-progress`, `done`, `existing`, `conflict` and `unknown` states.
 */
enum class ProvisioningState {
    /** Nothing has been created yet. */
    NOT_STARTED,

    /** Content is pending, or the build chain is incomplete. */
    IN_PROGRESS,

    /** The step is finished. */
    DONE,

    /** A repository or an older build chain that this service did not create. */
    EXISTING,

    /** What was found conflicts with the request; the finding is in `detail`. */
    CONFLICT,

    /** Reading the state failed. */
    UNKNOWN,
}
