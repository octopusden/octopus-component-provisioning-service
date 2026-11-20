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
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ProvisioningController::class, ProvisioningConfigurationController::class)
@Import(WebSecurityConfig::class, ProvisioningService::class, ProvisioningConfigurationService::class)
@EnableConfigurationProperties(ComponentProvisioningProperties::class)
@TestPropertySource(
    properties = [
        "auth-server.url=https://auth.example.com",
        "auth-server.realm=example",
        "component-provisioning.configurations[0].id=regular",
        "component-provisioning.configurations[0].version=1",
    ],
)
class ProvisioningControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authServerClient: AuthServerClient

    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    private val requestBody =
        """
        {
          "configurationId": "solution",
          "buildChain": {"parentProjectId": "Root_Team", "minorVersion": "1.2"},
          "repositories": [{"url": "ssh://git@example.com/team/repo.git", "useExisting": true}]
        }
        """.trimIndent()

    @Test
    fun catalogueListStillWorks() {
        mockMvc
            .perform(get("/rest/api/1/configurations").with(jwt()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value("regular"))
    }

    @Test
    fun catalogueListIgnoresUrlWithoutOrigin() {
        mockMvc
            .perform(get("/rest/api/1/configurations").param("url", "ssh://git@example.com/a.git").with(jwt()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value("regular"))
    }

    @Test
    fun resolvedConfigurationWithoutTokenIsUnauthorized() {
        mockMvc
            .perform(get("/rest/api/1/configurations").param("profile", "regular-external"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun resolvedConfigurationByProfileIsNotImplemented() {
        mockMvc
            .perform(get("/rest/api/1/configurations").param("profile", "regular-external").with(jwt()))
            .andExpect(status().isNotImplemented)
            .andExpect(jsonPath("$.error.code").value(501))
    }

    @Test
    fun resolvedConfigurationByTemplateIsNotImplemented() {
        mockMvc
            .perform(
                get("/rest/api/1/configurations")
                    .param("componentTemplate", "solution")
                    .param("componentTemplateVersion", "2")
                    .param("url", "ssh://git@example.com/a.git")
                    .param("url", "ssh://git@example.com/b.git")
                    .with(jwt()),
            ).andExpect(status().isNotImplemented)
            .andExpect(jsonPath("$.error.code").value(501))
    }

    @Test
    fun statusWithoutTokenIsUnauthorized() {
        mockMvc.perform(get("/rest/api/1/components/c-1/provisioning")).andExpect(status().isUnauthorized)
    }

    @Test
    fun statusIsNotImplemented() {
        mockMvc
            .perform(get("/rest/api/1/components/c-1/provisioning").with(jwt()))
            .andExpect(status().isNotImplemented)
            .andExpect(jsonPath("$.error.code").value(501))
    }

    @Test
    fun provisionWithoutTokenIsUnauthorized() {
        mockMvc
            .perform(
                post("/rest/api/1/components/c-1/provisioning")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun provisionAcceptsTypedBodyAndIsNotImplemented() {
        mockMvc
            .perform(
                post("/rest/api/1/components/c-1/provisioning")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andExpect(status().isNotImplemented)
            .andExpect(jsonPath("$.error.code").value(501))
    }
}
