package org.octopusden.octopus.provisioning.service.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.octopusden.octopus.provisioning.common.dto.error.ApiErrorResponse
import org.octopusden.octopus.provisioning.common.dto.provisioning.ProvisioningRequest
import org.octopusden.octopus.provisioning.common.dto.provisioning.ProvisioningStatus
import org.octopusden.octopus.provisioning.service.service.ProvisioningService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rest/api/1/components/{id}/provisioning")
@Tag(name = "Provisioning", description = "Provision a component and read the state of what was provisioned")
@ApiResponse(
    responseCode = "401",
    description = "No valid token",
    content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
)
class ProvisioningController(
    private val service: ProvisioningService,
) {
    @GetMapping
    @Operation(summary = "The state of each repository and of the build chain of the component")
    @ApiResponse(responseCode = "200", description = "The provisioning status")
    @ApiResponse(
        responseCode = "404",
        description = "Unknown component",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    @ApiResponse(
        responseCode = "501",
        description = "Not implemented yet",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    fun getStatus(
        @Parameter(description = "The component's id in the components registry")
        @PathVariable id: String,
    ): ProvisioningStatus = service.getStatus(id)

    @PostMapping
    @Operation(
        summary = "Provision the component, or continue after a failure",
        description = "Safe to repeat: each step does only what its state says is missing.",
    )
    @ApiResponse(responseCode = "200", description = "The provisioning status after the request")
    @ApiResponse(
        responseCode = "403",
        description = "A failed authorization check",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Unknown component",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    @ApiResponse(
        responseCode = "409",
        description = "A resource exists but was not created for this component",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    @ApiResponse(
        responseCode = "422",
        description = "A component the configuration cannot serve: no configuration for the origin, a missing template " +
            "parameter, an open field without a value, or a fixed field given a different value",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    @ApiResponse(
        responseCode = "501",
        description = "Not implemented yet",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    @ApiResponse(
        responseCode = "502",
        description = "A template zip that is needed is missing",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    fun provision(
        @Parameter(description = "The component's id in the components registry")
        @PathVariable id: String,
        @RequestBody request: ProvisioningRequest,
    ): ProvisioningStatus = service.provision(id, request)
}
