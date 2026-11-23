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
    // CSRF is intentionally disabled: the service is a stateless OAuth2 resource server. There is
    // no session cookie a CSRF attacker could ride: every request authenticates with a Bearer JWT
    // in the Authorization header, which a cross-site form post cannot set. With CSRF on, every
    // POST would be refused with 403 even with a valid token. The cookie-session surface is the
    // Portal's gateway, which enforces CSRF itself. CodeQL "Disabled Spring CSRF protection"
    // (java/spring-disabled-csrf-protection) is acknowledged here.
    @Bean
    override fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
        return super.securityFilterChain(http)
    }
}
