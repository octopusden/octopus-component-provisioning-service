package org.octopusden.octopus.provisioning.service.controller

import org.junit.jupiter.api.Test
import org.octopusden.cloud.commons.security.client.AuthServerClient
import org.octopusden.octopus.provisioning.service.configuration.ComponentProvisioningProperties
import org.octopusden.octopus.provisioning.service.configuration.WebSecurityConfig
import org.octopusden.octopus.provisioning.service.service.ProvisioningConfigurationService
import org.octopusden.octopus.provisioning.service.service.ProvisioningService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ProvisioningConfigurationController::class)
@Import(WebSecurityConfig::class, ProvisioningConfigurationService::class, ProvisioningService::class)
@EnableConfigurationProperties(ComponentProvisioningProperties::class)
@TestPropertySource(
    properties = [
        "auth-server.url=https://auth.example.com",
        "auth-server.realm=example",
        "component-provisioning.configurations[0].id=regular",
        "component-provisioning.configurations[0].version=1",
        "component-provisioning.configurations[0].origins[0].profile=regular-external",
        "component-provisioning.configurations[1].id=ww",
        "component-provisioning.configurations[1].version=2",
        "component-provisioning.configurations[1].repositories[0].template=com.example:ww-template:1.0",
    ],
)
class ProvisioningConfigurationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authServerClient: AuthServerClient

    // The resource server needs a decoder; in production it comes from the issuer-uri setting.
    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun noTokenIsUnauthorized() {
        mockMvc.perform(get("/rest/api/1/configurations")).andExpect(status().isUnauthorized)
    }

    @Test
    fun configuredCatalogueIsReturnedAsIs() {
        mockMvc
            .perform(get("/rest/api/1/configurations").with(jwt()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("regular"))
            .andExpect(jsonPath("$[0].version").value(1))
            .andExpect(jsonPath("$[0].selectable").value(false))
            .andExpect(jsonPath("$[0].origins[0].profile").value("regular-external"))
            .andExpect(jsonPath("$[1].id").value("ww"))
            .andExpect(jsonPath("$[1].repositories[0].template.gav").value("com.example:ww-template:1.0"))
    }
}
