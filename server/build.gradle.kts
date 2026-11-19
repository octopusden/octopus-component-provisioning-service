plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("com.bmuschko.docker-spring-boot-application")
}

tasks.named<Jar>("jar") {
    enabled = false
}

springBoot {
    buildInfo()
}

// TeamCity passes the registries as environment variables, GitHub Actions as -P properties.
fun registry(
    environmentVariable: String,
    property: String,
) = System.getenv(environmentVariable) ?: project.property(property) as String

docker {
    springBootApplication {
        baseImage.set("${registry("DOCKER_REGISTRY", "docker.registry")}/eclipse-temurin:25-jre")
        maintainer.set("octopus")
        ports.set(listOf(8080))
        images.set(
            setOf(
                "${registry("OCTOPUS_GITHUB_DOCKER_REGISTRY", "octopus.github.docker.registry")}/octopusden/" +
                    "${project.name}:${project.version}",
            ),
        )
    }
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    args("--spring.profiles.active=dev")
}

dependencies {
    implementation(project(":common"))

    implementation(platform("org.springframework.boot:spring-boot-dependencies:${project.property("spring-boot.version")}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${project.property("spring-cloud.version")}"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.property("springdoc.version")}")
    implementation("org.octopusden.octopus-cloud-commons:octopus-security-common:${project.property("octopus-cloud-commons.version")}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
