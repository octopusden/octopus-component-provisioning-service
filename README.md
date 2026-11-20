# octopus-component-provisioning-service

Serves the provisioning-configuration catalogue: for each kind of component, which repository templates,
build chain options and extra stages it gets when it is provisioned.

Endpoints:

- `GET /rest/api/1/configurations` returns the catalogue as configured.
- `GET /rest/api/1/configurations?profile=…` (or `?componentTemplate=…&componentTemplateVersion=…`), with
  `url=…` repeatable, returns the configuration that serves the origin, each field fixed or open. Not
  implemented yet: answers 501.
- `GET /rest/api/1/components/{id}/provisioning` returns the provisioning status of a component. Not
  implemented yet: answers 501.
- `POST /rest/api/1/components/{id}/provisioning` provisions the component, or continues after a failure; it is
  safe to repeat. Not implemented yet: answers 501.

`{id}` is the component's id in the components registry. Every endpoint except
`/actuator/**` and the Swagger UI needs a valid Keycloak bearer token. Any authenticated user may read the
catalogue.

See [docs/architecture.md](docs/architecture.md) for how the modules and packages fit together.

## Modules

- `common`: the catalogue DTOs, with no framework dependencies. It is published to the shared GitHub Packages
  registry as `org.octopusden.octopus.provisioning:common`.
- `server` (Gradle project `component-provisioning-service`): the Spring Boot service, built into a Docker
  image `octopusden/component-provisioning-service`.

## Build

JDK 25 (Gradle resolves the toolchain), then:

```
./gradlew build
```

`./gradlew dockerBuildImage` builds the image locally; `dockerPushImage` pushes it. The registries come from
the `DOCKER_REGISTRY` and `OCTOPUS_GITHUB_DOCKER_REGISTRY` environment variables, or the `docker.registry`
and `octopus.github.docker.registry` Gradle properties.

`./gradlew :component-provisioning-service:bootRun` starts the service with the `dev` profile, which turns
the Spring Cloud Config client off; the settings below must then be given some other way.

## Configuration

The service reads its settings from Spring Cloud Config (token in `VAULT_TOKEN`).

| Key | Meaning |
|---|---|
| `auth-server.url`, `auth-server.realm` | The Keycloak server and realm; the token issuer (`spring.security.oauth2.resourceserver.jwt.issuer-uri`) is derived from them |
| `technical-account.username`, `technical-account.password` | The technical account (from Vault) for vcs-facade and teamcity-automation-service, sent as HTTP Basic auth through the gateway; not used yet |
| `octopus-security.roles` | Role to permission map; may be empty |
| `component-provisioning.configurations` | The catalogue: a list of provisioning configurations |
| `vcs-facade.url`, `teamcity-automation-service.url`, `components-registry.url` | Base URLs of the services to call; declared, not used yet |

The catalogue is served as written and is not validated. A catalogue entry looks like this:

```yaml
component-provisioning:
  configurations:
    - id: solution
      version: 3
      origins:
        - { componentTemplate: solution }
      repositories:
        - template:
            byLabel:
              helm: "<groupId>:helm-template:<version>"
      buildChain:
        parentProjectId: <project id>
        extraStages:
          - name: "UAT"
```
