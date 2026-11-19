package org.octopusden.octopus.provisioning.service.configuration

import org.octopusden.cloud.commons.security.client.AuthServerClient
import org.octopusden.cloud.commons.security.config.CloudCommonWebSecurityConfig
import org.octopusden.cloud.commons.security.config.SecurityProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@Import(AuthServerClient::class)
@EnableConfigurationProperties(SecurityProperties::class)
class WebSecurityConfig(
    authServerClient: AuthServerClient,
    securityProperties: SecurityProperties,
) : CloudCommonWebSecurityConfig(
        authServerClient = authServerClient,
        securityProperties = securityProperties,
    ) {
    // Bearer tokens only, no session or cookies, so CSRF does not apply.
    @Bean
    override fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
        return super.securityFilterChain(http)
    }
}
