package org.octopusden.octopus.provisioning.service.configuration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

class ClientUrlPropertiesTest {
    @Configuration
    @EnableConfigurationProperties(
        VcsFacadeProperties::class,
        TeamcityAutomationServiceProperties::class,
        ComponentsRegistryProperties::class,
    )
    class TestConfig

    private val runner = ApplicationContextRunner().withUserConfiguration(TestConfig::class.java)

    @Test
    fun nullByDefault() {
        runner.run { context ->
            assertNull(context.getBean(VcsFacadeProperties::class.java).url)
            assertNull(context.getBean(TeamcityAutomationServiceProperties::class.java).url)
            assertNull(context.getBean(ComponentsRegistryProperties::class.java).url)
        }
    }

    @Test
    fun bindsFromTopLevelKeys() {
        runner
            .withPropertyValues(
                "vcs-facade.url=https://vcs-facade.example.com",
                "teamcity-automation-service.url=https://automation.example.com",
                "components-registry.url=https://registry.example.com",
            ).run { context ->
                assertEquals("https://vcs-facade.example.com", context.getBean(VcsFacadeProperties::class.java).url)
                assertEquals(
                    "https://automation.example.com",
                    context.getBean(TeamcityAutomationServiceProperties::class.java).url,
                )
                assertEquals("https://registry.example.com", context.getBean(ComponentsRegistryProperties::class.java).url)
            }
    }

    @Test
    fun unknownSubkeysAreIgnored() {
        runner
            .withPropertyValues(
                "components-registry.url=https://registry.example.com",
                "components-registry.vcs.something=x",
            ).run { context ->
                assertNull(context.startupFailure)
                assertEquals("https://registry.example.com", context.getBean(ComponentsRegistryProperties::class.java).url)
            }
    }
}
