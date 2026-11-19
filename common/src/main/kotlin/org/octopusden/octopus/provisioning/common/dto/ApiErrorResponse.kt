package org.octopusden.octopus.provisioning.common.dto

/** The error envelope of the Octopus REST API Guidelines: `{"error": {"code": 404, "message": "..."}}`. */
data class ApiErrorResponse(
    val error: ApiError,
)
