package org.octopusden.octopus.provisioning.service.configuration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.octopusden.octopus.provisioning.common.dto.configuration.ExtraStage
import org.octopusden.octopus.provisioning.common.dto.configuration.Origin
import org.octopusden.octopus.provisioning.common.dto.configuration.TemplateRef
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

class ComponentProvisioningPropertiesTest {
    @Configuration
    @EnableConfigurationProperties(ComponentProvisioningProperties::class)
    class TestConfig

    private val runner =
        ApplicationContextRunner()
            .withInitializer(ConfigDataApplicationContextInitializer())
            .withUserConfiguration(TestConfig::class.java)

    @Test
    fun emptyCatalogueByDefault() {
        runner.run { context ->
            val properties = context.getBean(ComponentProvisioningProperties::class.java)
            assertTrue(properties.configurations.isEmpty())
        }
    }

    @Test
    fun sampleCatalogueBinds() {
        runner.withPropertyValues("spring.config.location=classpath:/sample-catalogue.yaml").run { context ->
            val properties = context.getBean(ComponentProvisioningProperties::class.java)
            val (solution, regular, ww) = properties.configurations
            assertEquals(3, properties.configurations.size)

            assertEquals("solution", solution.id)
            assertEquals(3, solution.version)
            assertFalse(solution.selectable)
            assertEquals(listOf(Origin(componentTemplate = "solution")), solution.origins)
            assertEquals(
                TemplateRef(
                    byLabel =
                        mapOf(
                            "helm" to "com.example:helm-template:1.0",
                            "ansible" to "com.example:ansible-template:1.0",
                        ),
                ),
                solution.repositories.single().template,
            )
            assertNull(solution.repositories.single().urlRegex)
            assertNull(solution.repositories.single().useExisting)
            val chain = solution.buildChain!!
            assertEquals("example-project", chain.parentProjectId)
            assertNull(chain.minorVersion)
            assertEquals(
                listOf(
                    ExtraStage(
                        name = "Deploy to QA [Manual]",
                        template = "CDComponentDeploy",
                        checkout = "compile",
                        parametersFromCompile = listOf("DISTRIBUTION_ARTIFACTS_COORDINATES", "BUILD_VERSION"),
                    ),
                    ExtraStage(name = "UAT"),
                ),
                chain.extraStages,
            )

            assertEquals("regular", regular.id)
            assertEquals(listOf(Origin(profile = "regular-external"), Origin(profile = "regular-internal")), regular.origins)
            assertTrue(regular.repositories.isEmpty())
            assertNull(regular.buildChain)

            assertTrue(ww.selectable)
            assertEquals(Origin(componentTemplate = "ww", componentTemplateVersion = "2"), ww.origins.single())
            val (shared, default) = ww.repositories
            assertEquals("ssh://git@.*/shared\\.git", shared.urlRegex)
            assertEquals(true, shared.useExisting)
            assertNull(shared.template)
            assertNull(default.urlRegex)
            assertNull(default.useExisting)
            assertEquals(TemplateRef(gav = "com.example:ww-template:1.0"), default.template)
        }
    }
}
