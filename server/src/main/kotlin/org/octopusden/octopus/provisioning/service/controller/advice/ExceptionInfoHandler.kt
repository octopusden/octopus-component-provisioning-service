package org.octopusden.octopus.provisioning.service.controller.advice

import org.octopusden.octopus.provisioning.common.dto.ApiError
import org.octopusden.octopus.provisioning.common.dto.ApiErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionInfoHandler {
    /**
     * Spring's own exceptions (unknown path, wrong method, unsupported media type) implement
     * [ErrorResponse] and carry their status and headers, such as `Allow` on a 405; only the rest is
     * a server error.
     */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ApiErrorResponse> {
        if (e is ErrorResponse) {
            return errorResponse(HttpStatus.valueOf(e.statusCode.value()), e.message ?: e.statusCode.toString()) {
                headers(e.headers)
            }
        }
        log.error(e.message, e)
        // The exception text can carry an upstream system's URL and response body; it stays in the log.
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE)
    }

    private fun errorResponse(
        status: HttpStatus,
        message: String,
        headers: ResponseEntity.BodyBuilder.() -> Unit = {},
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(status)
            .apply(headers)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiErrorResponse(ApiError(status.value(), message)))

    private companion object {
        const val INTERNAL_ERROR_MESSAGE = "Internal server error; the service log has the details"
        val log = LoggerFactory.getLogger(ExceptionInfoHandler::class.java)
    }
}
