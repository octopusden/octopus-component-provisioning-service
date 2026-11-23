package org.octopusden.octopus.provisioning.service.configuration

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.octopusden.octopus.provisioning.service.ComponentProvisioningServiceApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * Starts the application with the Config Server client enabled against a stub that answers 503 to the
 * first requests. Spring Cloud Config retries only when Spring Retry and AOP are on the classpath;
 * without them the first failure aborts startup (`fail-fast`).
 */
class ConfigServerRetryTest {
    private val requests = CopyOnWriteArrayList<String>()
    private val responses = CopyOnWriteArrayList<Int>()
    private val counter = AtomicInteger()
    private lateinit var server: HttpServer
    private var context: ConfigurableApplicationContext? = null

    private val baseUrl get() = "http://localhost:${server.address.port}"

    @BeforeEach
    fun startStub() {
        server =
            HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
                createContext("/") { exchange -> handle(exchange) }
                start()
            }
    }

    @AfterEach
    fun stopAll() {
        context?.close()
        server.stop(0)
    }

    private fun send(
        exchange: HttpExchange,
        status: Int,
        body: String = "",
    ) {
        responses.add(status)
        val bytes = body.toByteArray()
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(status, if (bytes.isEmpty()) -1 else bytes.size.toLong())
        if (bytes.isNotEmpty()) exchange.responseBody.use { it.write(bytes) }
        exchange.close()
    }

    private fun handle(exchange: HttpExchange) {
        val path = exchange.requestURI.path
        if (path.startsWith("/realms/")) {
            // The OIDC discovery document for the resource server.
            val issuer = "$baseUrl/realms/test"
            exchange.responseHeaders.add("Content-Type", "application/json")
            val body = (
                """{"issuer":"$issuer","jwks_uri":"$issuer/certs",""" +
                    """"userinfo_endpoint":"$issuer/userinfo","token_endpoint":"$issuer/token"}"""
            ).toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
            return
        }
        requests.add(path)
        if (counter.incrementAndGet() <= FAILURES) {
            send(exchange, 503)
        } else {
            send(
                exchange,
                200,
                """{"name":"component-provisioning-service","profiles":["default"],"label":null,"version":null,""" +
                    """"state":null,"propertySources":[{"name":"stub","source":{""" +
                    """"auth-server.url":"$baseUrl","auth-server.realm":"test",""" +
                    """"component-provisioning.configurations[0].id":"served",""" +
                    """"component-provisioning.configurations[0].version":"7"}}]}""",
            )
        }
    }

    @Test
    fun startupSurvivesTransientConfigServerFailures() {
        context =
            SpringApplicationBuilder(ComponentProvisioningServiceApplication::class.java)
                .run(
                    // The test bootstrap.yml shadows the main one, so the name and retry settings are given here.
                    "--spring.application.name=component-provisioning-service",
                    "--spring.cloud.bootstrap.enabled=true",
                    "--spring.cloud.config.enabled=true",
                    "--spring.cloud.config.uri=$baseUrl",
                    "--spring.cloud.config.token=test",
                    "--spring.cloud.config.fail-fast=true",
                    "--spring.cloud.config.retry.initial-interval=100",
                    "--spring.cloud.config.retry.max-interval=200",
                    "--spring.cloud.config.retry.max-attempts=5",
                    "--server.port=0",
                )

        val catalogue = context!!.getBean(ComponentProvisioningProperties::class.java)
        assertEquals("served", catalogue.configurations.single().id)
        assertTrue(requests.size > FAILURES, "stub saw only ${requests.size} requests: $requests")
        assertEquals(List(FAILURES) { 503 } + 200, responses.toList().take(FAILURES + 1))
        assertTrue(requests.all { it.startsWith("/component-provisioning-service/") }, requests.toString())
    }

    private companion object {
        const val FAILURES = 2
    }
}
