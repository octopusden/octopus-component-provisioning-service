package org.octopusden.octopus.provisioning.service.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.octopusden.cloud.commons.security.client.AuthServerClient
import org.octopusden.cloud.commons.security.config.AuthClientProperties
import org.octopusden.cloud.commons.security.config.AuthServerProperties
import org.octopusden.octopus.provisioning.service.ComponentProvisioningServiceApplication
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Runs the real octopus-security-common code against a stub OIDC server: nothing in the library is
 * mocked. octopus-security-common is compiled for Spring Framework 6; on Spring 7 its
 * `AuthServerClient` fails with a ClassCastException (HttpHeaders is no longer a MultiValueMap), which
 * the controller tests miss because they mock the client or bypass the converter with `jwt()`.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [ComponentProvisioningServiceApplication::class],
)
class SecurityCommonIntegrationTest {
    @Value("\${local.server.port}")
    private var port: Int = 0

    private val http = HttpClient.newHttpClient()

    private fun send(
        method: String,
        path: String,
        token: String? = tokenValue,
    ): HttpResponse<String> {
        val builder = HttpRequest.newBuilder(URI("http://localhost:$port$path"))
        token?.let { builder.header("Authorization", "Bearer $it") }
        if (method == "POST") {
            builder.header("Content-Type", "application/json")
            builder.POST(HttpRequest.BodyPublishers.ofString(REQUEST_BODY))
        } else {
            builder.GET()
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    @Test
    fun authenticatedGetCallsUserInfoAndReturnsCatalogue() {
        val callsBefore = userInfoCalls.size
        val response = send("GET", "/rest/api/1/configurations")
        assertEquals(200, response.statusCode(), response.body())
        assertTrue(response.body().contains("\"id\":\"regular\""), response.body())
        assertEquals(callsBefore + 1, userInfoCalls.size, "userinfo endpoint was not called")
        assertEquals("Bearer $tokenValue", userInfoCalls.last())
    }

    @Test
    fun authenticatedPostWithoutCsrfReachesTheController() {
        val response = send("POST", "/rest/api/1/components/x/provisioning")
        assertEquals(501, response.statusCode(), response.body())
    }

    @Test
    fun healthEndpointsAreOpenWithoutToken() {
        listOf("/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness").forEach { path ->
            val response = send("GET", path, token = null)
            assertEquals(200, response.statusCode(), "$path: ${response.body()}")
            assertTrue(response.body().contains("\"status\":\"UP\""), "$path: ${response.body()}")
        }
    }

    @Test
    fun requestWithoutTokenIsUnauthorized() {
        assertEquals(401, send("GET", "/rest/api/1/configurations", token = null).statusCode())
    }

    @Test
    fun generateOfflineJwtUsesTheTokenEndpoint() {
        val client =
            AuthServerClient(
                AuthServerProperties(url = baseUrl, realm = REALM),
                AuthClientProperties(clientId = "client", clientSecret = "secret"),
            )
        val jwt = client.generateOfflineJwt("alice", "password")
        assertEquals("offline-access-token", jwt.accessToken)
        assertEquals("offline-refresh-token", jwt.refreshToken)
        assertTrue(tokenRequests.any { it.contains("grant_type=password") && it.contains("username=alice") })
        assertFalse(client.refreshOfflineJwt(jwt.refreshToken).accessToken.isEmpty())
        assertTrue(tokenRequests.any { it.contains("grant_type=refresh_token") })
    }

    companion object {
        private const val REALM = "test"
        private const val REQUEST_BODY =
            """{"configurationId":"regular","buildChain":{"parentProjectId":"Root","minorVersion":"1.0"},""" +
                """"repositories":[{"url":"ssh://git@example.com/team/repo.git","useExisting":true}]}"""

        private val rsaKey: RSAKey = RSAKeyGenerator(2048).keyID("test-key").generate()
        private val userInfoCalls = CopyOnWriteArrayList<String>()
        private val tokenRequests = CopyOnWriteArrayList<String>()
        private val server: HttpServer =
            HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
                createContext("/") { exchange -> handle(exchange) }
                start()
            }
        private val baseUrl = "http://localhost:${server.address.port}"
        private val issuer = "$baseUrl/realms/$REALM"
        private val tokenValue: String by lazy {
            val claims =
                JWTClaimsSet
                    .Builder()
                    .issuer(issuer)
                    .subject("alice")
                    .claim("preferred_username", "alice")
                    .expirationTime(Date(System.currentTimeMillis() + 300_000))
                    .build()
            SignedJWT(JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.keyID).build(), claims)
                .apply { sign(RSASSASigner(rsaKey)) }
                .serialize()
        }

        private fun reply(
            exchange: HttpExchange,
            body: String,
        ) {
            val bytes = body.toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        private fun handle(exchange: HttpExchange) {
            when (exchange.requestURI.path) {
                "/realms/$REALM/.well-known/openid-configuration" ->
                    reply(
                        exchange,
                        """{"issuer":"$issuer","jwks_uri":"$issuer/certs",""" +
                            """"userinfo_endpoint":"$issuer/userinfo","token_endpoint":"$issuer/token"}""",
                    )
                "/realms/$REALM/certs" -> reply(exchange, JWKSet(rsaKey.toPublicJWK()).toString())
                "/realms/$REALM/userinfo" -> {
                    userInfoCalls.add(exchange.requestHeaders.getFirst("Authorization"))
                    reply(exchange, """{"roles":["TEST_ROLE"],"groups":["test-group"]}""")
                }
                "/realms/$REALM/token" -> {
                    tokenRequests.add(exchange.requestBody.readAllBytes().decodeToString())
                    reply(
                        exchange,
                        """{"access_token":"offline-access-token","refresh_token":"offline-refresh-token",""" +
                            """"expires_in":300,"refresh_expires_in":0}""",
                    )
                }
                else -> {
                    exchange.sendResponseHeaders(404, -1)
                    exchange.close()
                }
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("auth-server.url") { baseUrl }
            registry.add("auth-server.realm") { REALM }
            registry.add("component-provisioning.configurations[0].id") { "regular" }
            registry.add("component-provisioning.configurations[0].version") { "1" }
        }

        @JvmStatic
        @AfterAll
        fun stopServer() {
            server.stop(0)
        }
    }
}
