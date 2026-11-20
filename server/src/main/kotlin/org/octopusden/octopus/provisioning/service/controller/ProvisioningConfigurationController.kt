package org.octopusden.octopus.provisioning.service.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.octopusden.octopus.provisioning.common.dto.configuration.ProvisioningConfiguration
import org.octopusden.octopus.provisioning.common.dto.error.ApiErrorResponse
import org.octopusden.octopus.provisioning.common.dto.resolved.ResolvedConfiguration
import org.octopusden.octopus.provisioning.service.service.ProvisioningConfigurationService
import org.octopusden.octopus.provisioning.service.service.ProvisioningService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rest/api/1/configurations")
@Tag(name = "Configurations", description = "The provisioning-configuration catalogue")
@ApiResponse(
    responseCode = "401",
    description = "No valid token",
    content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
)
class ProvisioningConfigurationController(
    private val service: ProvisioningConfigurationService,
    private val provisioningService: ProvisioningService,
) {
    @GetMapping
    @Operation(
        summary = "The catalogue, or the configuration that serves an origin",
        description = "Without `profile` and `componentTemplate`, returns the catalogue: a list of provisioning " +
            "configurations. With `profile`, or `componentTemplate` and an optional `componentTemplateVersion`, " +
            "returns the one configuration that serves that origin, each field listed as fixed or open, and each " +
            "`url` with the repository entry that matched it. The origin lookup answers 501 until implemented.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "A list of provisioning configurations, or the resolved configuration when an origin is given",
        content = [
            Content(
                schema = Schema(
                    oneOf = [ResolvedConfiguration::class, Array<ProvisioningConfiguration>::class],
                ),
            ),
        ],
    )
    @ApiResponse(
        responseCode = "404",
        description = "The origin has no configuration",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    @ApiResponse(
        responseCode = "501",
        description = "Origin lookup is not implemented yet",
        content = [Content(schema = Schema(implementation = ApiErrorResponse::class))],
    )
    fun getConfigurations(
        @Parameter(description = "A component profile") @RequestParam(required = false) profile: String?,
        @Parameter(description = "A component template") @RequestParam(required = false) componentTemplate: String?,
        @Parameter(description = "The component template's version") @RequestParam(required = false) componentTemplateVersion: String?,
        @Parameter(description = "A repository SSH URL; repeatable") @RequestParam(name = "url", required = false) urls: List<String>?,
    ): Any =
        if (profile == null && componentTemplate == null) {
            service.getConfigurations()
        } else {
            provisioningService.resolveConfiguration(profile, componentTemplate, componentTemplateVersion, urls.orEmpty())
        }
}
