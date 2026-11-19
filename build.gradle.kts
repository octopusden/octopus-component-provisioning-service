import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.InetAddress
import java.util.zip.CRC32

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    id("org.springframework.boot") apply false
    id("com.bmuschko.docker-spring-boot-application") apply false
    id("dev.detekt") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("org.jetbrains.kotlinx.kover") apply false
    id("org.octopusden.octopus-quality")
    id("org.sonarqube")
}

octopusQuality {
    // The deliverable is the Docker image built from the server; nothing is published to Maven.
    kotlin {
        failOnViolation.set(true)
    }
    // Data classes only: no behaviour of its own to cover. The server tests exercise it.
    excludeProjects(":common")
}

val defaultVersion = "${
    with(CRC32()) {
        update(InetAddress.getLocalHost().hostName.toByteArray())
        value
    }
}-SNAPSHOT"

allprojects {
    group = "org.octopusden.octopus.provisioning"
    if (version == "unspecified") {
        version = defaultVersion
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    // detekt and ktlint are applied per subproject so that qualityStatic finds a `detekt` and a
    // `ktlintCheck` task in every Kotlin module (octopus-quality's hollow-gate guard).
    apply(plugin = "dev.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
        compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
    }

    // octopus-quality configures only the io.gitlab.arturbosch.detekt plugin, and detekt 1.x
    // cannot run on JDK 25, so detekt 2 is configured here: the default rule set, no baseline.
    extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
        buildUponDefaultConfig.set(true)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
