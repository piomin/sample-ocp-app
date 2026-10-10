# AGENTS.md

Instructions for AI coding agents working in this repository.
This file is the source of truth for conventions. Follow it for any code you
create or modify.

## Project

Sample Spring Boot REST service used to demonstrate Backstage app skeletons and
OpenShift deployment. Group id `pl.piomin.services`, artifact `sample-ocp-app`.

| Item            | Value                                                        |
|-----------------|--------------------------------------------------------------|
| Language        | Java 25 (`java.version` in `pom.xml`)                        |
| Framework       | Spring Boot 4.1.1 (parent POM)                               |
| Build           | Maven                                                        |
| Persistence     | Spring Data JPA, H2 (in-memory)                              |
| Auth            | OAuth2 resource server, JWT, Keycloak (`realm_access.roles`) |
| Docs            | springdoc-openapi (`/swagger-ui.html`, `/v3/api-docs`)       |
| Tests           | JUnit 5, Spring Security Test, Instancio, MockMvc            |
| CI              | CircleCI: `mvn -B compile` then `mvn -B test`                |

## Commands

```bash
mvn -B compile        # compile only
mvn -B test           # run test suite (same as CI)
mvn -B clean verify   # full build
mvn -Pjib jib:dockerBuild   # build OCI image (jib profile)
```

Always run `mvn -B test` before you consider a change complete. A PR that does
not compile or breaks an existing test is not finished.

## Build hygiene

- **Never commit `target/`.** It is generated output. If it shows up in a diff,
  it is a mistake — remove it from the change and add it to `.gitignore`.
- Never edit files under `src/test/resources/k6/` unless the task is explicitly
  about load testing.
- Do not bump dependency versions. `renovate.json` owns upgrades; manual version
  churn creates conflicts.
- Do not add a dependency without saying why. Prefer the existing Spring
  starters; do not introduce Lombok (the codebase is written without it).

## Package structure

Layered packages under the single base package `pl.piomin.services`. Match the
existing layout; do not restructure it.

```
pl.piomin.services
├── Application.java          # @SpringBootApplication entry point
├── config/                   # @Configuration classes (SecurityConfig, KeycloakRoleConverter)
├── controller/               # @RestController
├── domain/                   # JPA @Entity classes
├── repository/               # Spring Data repository interfaces
└── service/                  # @Service business logic
```

Responsibilities are strictly separated:

- **Controller** — HTTP concerns only: mapping, status codes, request/response
  bodies. No business logic, no repository access.
- **Service** — all business logic. Stateless, delegates persistence to
  repositories, logs state changes.
- **Repository** — Spring Data interface extending `JpaRepository`. No logic.
- **Domain** — the JPA entity. Plain getters/setters, no framework annotations
  beyond JPA.
- **Config** — security and infrastructure beans.

## Java style

- 4 spaces, no tabs. No wildcard imports.
- Constructor injection only. No `@Autowired` field injection.
  ```java
  private final PersonService personService;

  public PersonController(PersonService personService) {
      this.personService = personService;
  }
  ```
- Fields are `private final` when set via constructor.
- SLF4J for logging, declared as
  `private static final Logger log = LoggerFactory.getLogger(X.class);`
  (this codebase uses the name `log`). Use parameterized messages
  (`log.info("Added: {}", saved)`) — never string concatenation.
- No `null` returns to signal absence where a caller has to null-check.
  Prefer `Optional` in the service signature when a lookup may miss; do not
  return bare `null` to a controller.
- No `System.out`. No commented-out code. No TODO without a linked issue.
- Comments explain *why*, not *what*. Security-relevant decisions (CSRF
  disabled, frame options) must keep an explanatory comment — see
  `config/SecurityConfig.java` for the expected style.

## Security

The API is a stateless JWT resource server. Changes must preserve these rules
from `config/SecurityConfig.java`:

- `SessionCreationPolicy.STATELESS` — no server-side sessions.
- CSRF disabled **because** the API is JWT-only. If you ever introduce cookie
  or form-based auth, CSRF must be re-enabled.
- Roles: `GET /api/**` → `USER` or `ADMIN`; `POST`/`PUT`/`DELETE /api/**` →
  `ADMIN` only.
- `/actuator/health/**` and `/actuator/info` are public; all other
  `/actuator/**` endpoints require `ADMIN`. Do not widen actuator exposure
  (`management.endpoints.web.exposure.include: '*'` in `application.yml` is
  already broad — do not extend it).
- Keycloak roles come from the `realm_access.roles` claim via
  `KeycloakRoleConverter`. Do not invent a second role source.

Hard rules:

- **No secrets in code or config.** No credentials, tokens, keys, or issuer URIs
  with credentials in `application.yml`. Use environment variables or
  `${...}` placeholders.
- Never commit anything under `keycloak/` that looks like realm export
  credentials.
- Do not disable or weaken security configuration to make a test pass. Fix the
  test's auth setup instead (`jwt().authorities(...)`).

## API design

- Endpoints live under `/api` (`@RequestMapping("/api")`).
- Sub-resource paths are declared once as a constant on the controller
  (`private final String idPath = "/{id}";`) and reused, as in
  `PersonController`.
- Return `200` with the resource for reads and writes, as the existing
  endpoints do. Do not introduce `201`/`204` inconsistently within a controller.
- **New endpoints must use DTOs, not JPA entities**, for request and response
  bodies. Entities are an internal persistence concern; do not couple the public
  API to the database schema.
- New write endpoints must validate input with Bean Validation
  (`@Valid` + `@NotNull`/`@Size`/etc.).
- Add error handling via a `@RestControllerAdvice` with `@ExceptionHandler`
  so failures return a consistent error body, rather than default stack traces.
- New or changed endpoints must appear correctly in the OpenAPI docs — annotate
  with `@Operation`/`@Tag`/`@ResponseStatus` as appropriate.

## Configuration

- `application.yml` only, in `src/main/resources`. Prefer YAML over properties.
- Environment-specific settings go into Spring profiles
  (`application-dev.yml`, `application-prod.yml`) selected via
  `SPRING_PROFILES_ACTIVE`. Do not branch on an ad-hoc property.
- Bind configuration to types with `@ConfigurationProperties` instead of
  scattering `@Value` strings across classes.
- Logging configuration lives in the same file, not in code.

## Testing

Tests mirror the class under test and live in `src/test/java/pl/piomin/services`.

- JUnit 5. Naming: `ClassUnderTestTests` (e.g. `PersonControllerTests`) to match
  the current convention in this repo.
- Test method names describe behaviour:
  `addWithUserRoleShouldReturn403`, not `testAdd2`.
- Follow Arrange-Act-Assert, one behaviour per test.
- **Every production change needs a test.** A new endpoint without a test is
  incomplete.
- Build request payloads with **Instancio** (`Instancio.create(Person.class)`),
  not hand-written fixtures. Set generated ids to `null` before `POST`.
- Controller tests use `@SpringBootTest(webEnvironment = MOCK)` with
  `MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()`
  — this is how the security filter chain is actually exercised.
- Every request that must succeed is authenticated with
  `.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_...")))`. Negative
  authorization tests must omit the JWT to assert `401`, or pass the wrong role
  to assert `403`.
- Service unit tests use Mockito with `@Mock` / `@InjectMocks`.
- Tests must be order-independent by default. The existing
  `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` with `@Order` exists
  because the suite shares a database — do not extend that pattern to new tests.
  If a test needs a specific record, create it inside the test.
- Do not weaken, delete, or `@Disabled` an existing test to get a green build.
  Only disable with an explicit reason and a linked issue.
- Use `@DisplayName` for non-obvious scenarios.

## Definition of done

Before reporting a task complete:

1. `mvn -B test` passes.
2. New behaviour is covered by tests.
3. No `target/`, IDE files, secrets, or unrelated formatting churn in the diff.
4. The diff contains only what the task required.

## Existing code to leave alone

These files deviate from the guidance above. They are pre-existing; do not
"fix" them as a drive-by change, and do not copy their patterns into new code:

- `controller/PersonController` — exposes `domain.Person` directly, uses a
  wildcard import, returns `null` for missing records.
- `service/PersonService` — returns `null` instead of `Optional`, no
  `@Transactional`.
- `domain/Person` — no DTO, no validation annotations.
