# Repository Guidelines & Agent Instructions

## Overview

This document provides guidelines for repository management, development workflows, and collaboration standards for this Maven-based Java project. It serves as a reference for both human developers and AI agents working with this codebase.

## 🏛️ Repository Structure & Organization

### Project Layout

```
foundation-cms-service/
├── .github/
│   └── workflows/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/iqkv/foundation/cmsservice/
│   │   │       ├── CmsApplication.java           # Spring Boot entry point
│   │   │       ├── infrastructure/               # Technical cross-cutting concerns
│   │   │       │   ├── config/                   # Spring configuration beans
│   │   │       │   │   ├── SecurityConfig.java
│   │   │       │   │   ├── AuthConfigurationProperties.java
│   │   │       │   │   ├── TenancyConfigurationProperties.java
│   │   │       │   │   ├── MyBatisConfig.java
│   │   │       │   │   ├── RabbitMQConfig.java
│   │   │       │   │   ├── GlobalExceptionHandler.java
│   │   │       │   │   ├── RolloutMode.java
│   │   │       │   │   └── AllTenantsKeyProvider.java
│   │   │       │   ├── security/
│   │   │       │   │   ├── CorrelationIdFilter.java
│   │   │       │   │   └── JwtClaimNames.java
│   │   │       │   ├── messaging/
│   │   │       │   │   └── TenantProvisioningConsumer.java
│   │   │       │   ├── mybatis/
│   │   │       │   │   └── UuidTypeHandler.java
│   │   │       │   └── persistence/
│   │   │       ├── page/                         # CMS page bounded context (flat layout)
│   │   │       │   ├── Page.java                 # Domain model
│   │   │       │   ├── PageTranslation.java
│   │   │       │   ├── PageStatus.java
│   │   │       │   ├── PageService.java           # Service interface (port)
│   │   │       │   ├── PageServiceImpl.java       # Service implementation
│   │   │       │   ├── PageMapper.java            # MyBatis mapper interface
│   │   │       │   ├── PageRestResource.java      # Public REST controller
│   │   │       │   ├── AdminPageRestResource.java # Admin REST controller
│   │   │       │   ├── TenantPageRestResource.java
│   │   │       │   └── dto/
│   │   │       │       ├── PageDtos.java          # All DTOs as nested records
│   │   │       │       ├── PageDtoMapper.java
│   │   │       │       ├── PageHierarchyRow.java
│   │   │       │       └── PageSummaryRow.java
│   │   │       ├── shared/
│   │   │       │   ├── exception/
│   │   │       │   │   ├── PageNotFoundException.java
│   │   │       │   │   └── InvalidPlatformModeException.java
│   │   │       │   └── util/
│   │   │       └── tenancy/
│   │   │           └── TenantExtractionFilter.java
│   │   └── resources/
│   │       ├── application.yml           # Base configuration
│   │       ├── application-local.yml     # Local dev profile
│   │       ├── application-sit.yml       # SIT profile
│   │       ├── application-uat.yml       # UAT profile
│   │       ├── application-prd.yml       # Production profile
│   │       ├── mappers/page/             # MyBatis XML mapper files
│   │       ├── db/changelog/
│   │       │   ├── system/              # System-level Liquibase migrations
│   │       │   └── tenant/              # Per-tenant Liquibase migrations
│   │       ├── keys/public.pem          # RSA public key for JWT verification
│   │       ├── i18n/messages*.properties
│   │       └── logback-spring.xml
│   └── test/
│       └── java/com/iqkv/foundation/cmsservice/
│           ├── CmsApplicationTests.java
│           ├── IntegrationTest.java       # @SpringBootTest composite annotation
│           └── TechnicalStructureTest.java # ArchUnit onion architecture test
├── docker/                               # Grafana, dbgate, etc.
├── compose.yaml                          # Docker Compose for local infra
├── pom.xml
└── AGENTS.md
```

### Architecture Notes

This project uses a **flat bounded-context layout** rather than deeply nested DDD subdirectories. All classes for a context (model, service, mapper, controllers, DTOs) live directly in the context package (e.g., `page/`). This is intentional — do not reorganize into `domain/`, `application/`, `adapter/` subdirectories unless explicitly instructed.

The ArchUnit test (`TechnicalStructureTest`) validates the **onion architecture** with `withOptionalLayers(true)`, meaning layers are enforced when present but are not required to exist.

**Multi-tenancy** is a first-class concern. Every database operation is scoped to a tenant via `TenantContext`. Liquibase runs separate migration sets per tenant (`db/changelog/tenant/`) and for the system schema (`db/changelog/system/`). The platform supports `MULTI_TENANT` and `SINGLE_TENANT` rollout modes, controlled by `iqkv.platform.rollout-mode`.

## 🤖 AI Agent Guidelines

### AI Communication Standards

**CRITICAL: Agents must communicate concisely and avoid unnecessary verbosity.**

#### Response Length Guidelines

```yaml
summaries:
    max_length: "2-3 sentences"
    focus: "What was done, not how it was done"
    avoid: "Bullet point lists, detailed recaps, obvious statements"

explanations:
    when_detailed: "Complex technical concepts, architecture decisions, security implications"
    when_brief: "Simple changes, routine operations, status updates"

verification:
    format: "Minimal wording - state outcome only"
    example: "Tests pass. Coverage at 65%."
    avoid: "Lengthy descriptions of what was verified"
```

#### Prohibited Outputs

**NEVER create these files:**

- ❌ `SUMMARY.md`
- ❌ `CHANGES.md`
- ❌ `REVIEW.md`
- ❌ `ANALYSIS.md`
- ❌ Any markdown file documenting agent work unless explicitly requested

#### Concise vs Verbose Examples

**❌ VERBOSE (Don't do this):**

```
I have successfully completed the implementation of the new page endpoint.
Here's a comprehensive summary of everything I did: ...
```

**✅ CONCISE (Do this):**

```
Added tenant page endpoint with locale fallback. Tests pass, coverage at 68%.
```

#### When to Be Detailed

Provide detailed explanations ONLY for:

1. Complex architecture decisions
2. Security implications
3. Breaking changes
4. Non-obvious technical choices

#### Response Templates

```
# Simple change
Changed X to Y. Tests pass.

# Bug fix
Fixed [issue]. Root cause: [brief explanation]. Added regression test.

# New feature
Implemented [feature]. Includes [key components]. Tests pass, coverage [X]%.

# Refactoring
Refactored [component] to [improvement]. No behavior changes. Tests pass.
```

### Technology Stack

**Runtime & Framework**

- Java 25 with modern features (records, pattern matching, text blocks, `var`)
- Spring Boot (via `com.iqkv:boot-parent-pom:0.24.23` — version managed by parent POM)
- Maven build system

**Persistence**

- PostgreSQL (production/local runtime database)
- **MyBatis** with XML mappers — there is no Spring Data JPA / Hibernate in this project
- Liquibase for schema migrations, split into `system/` and `tenant/` changelogs
- H2 in-memory database for tests
- MyBatis mapper XMLs live in `src/main/resources/mappers/`
- `mybatis.configuration.map-underscore-to-camel-case=true` is enabled

**Messaging**

- RabbitMQ via `spring-boot-starter-amqp`
- Messaging is **disabled by default** (`iqkv.messaging.rabbitmq.enabled=false`); enabled per profile (local, sit, uat, prd)
- Inbound event: `TenantProvisioningConsumer` handles tenant lifecycle events

**Security**

- Spring Security + OAuth2 Resource Server (JWT validation)
- JWT decoded using an **RSA public key** (`keys/public.pem`), not a symmetric secret
- Authorities extracted from JWT claim `"authorities"` (not `"roles"` or `"scope"`)
- Stateless sessions; CSRF disabled
- `@EnableMethodSecurity` active for `@PreAuthorize` on methods
- Custom filters: `CorrelationIdFilter` → `TenantExtractionFilter` (ordered after `BearerTokenAuthenticationFilter`)
- Endpoint access matrix:
    - `/actuator/**`, `/api-docs/**`, `/swagger-ui/**` — public
    - `/api/v1/cms/pages/**` — public (tenant-scoped reads)
    - `/api/v1/cms/admin/**` — requires `PLATFORM_ADMIN` authority
    - `/api/v1/cms/tenant/**` — requires `TENANT_OWNER` or `ADMIN` authority
    - all other requests — authenticated

**Observability**

- Spring Boot Actuator (management port **8081**, main port **8080**)
- Prometheus metrics via `micrometer-registry-prometheus` at `/actuator/prometheus`
- Structured JSON logs via `logstash-logback-encoder`
- Git commit info exposed via `io.github.git-commit-id:git-commit-id-maven-plugin`

**API Documentation**

- SpringDoc OpenAPI 3 / Swagger UI
- API docs: `GET /api-docs`
- Swagger UI: `GET /swagger-ui.html`

**Testing**

- JUnit 5 + Mockito (via `spring-boot-starter-test`)
- `spring-security-test` for security context setup
- `mybatis-spring-boot-starter-test` for MyBatis slice tests
- `spring-rabbit-test` for AMQP mocking
- Testcontainers: `junit-jupiter`, `postgresql`, `rabbitmq`
- ArchUnit (`archunit-junit5`) — architecture rules enforced in `TechnicalStructureTest`
- `@IntegrationTest` composite annotation wraps `@SpringBootTest(classes = CmsApplication.class)`
- JaCoCo minimum coverage: **60%** (enforced in build; `Application.class` excluded)
- JaCoCo is **skipped by default** (`jacoco.skip=true`); enable explicitly with `-Djacoco.skip=false`

**Internal Libraries (iQKV Foundation)**

- `foundation-entitlement-plan-resolver-mvc` — billing plan quota enforcement
- `foundation-tenancy` — `TenantContext`, tenant schema routing
- `foundation-audit-model` + `foundation-audit-spi` — audit trail primitives

## 📋 Development Standards

### Branch Strategy

```
main (production-ready code)
├── develop (main development branch)
├── feature/* (new features)
├── bugfix/* (bug fixes)
├── improvement/* (enhancements)
├── hotfix/* (production fixes)
└── rfc/* (request for comments)
```

### Commit Message Format (Conventional Commits)

**Format:**

```
type(scope): subject

[optional body]

[optional footer]
```

**Allowed Types:** `feat`, `fix`, `rfc`, `docs`, `style`, `improvement`, `refactor`, `perf`, `test`, `chore`, `build`, `ci`, `revert`

**Rules:** Subject line 6–220 characters, lowercase type, imperative mood, no trailing period.

**Examples:**

```bash
feat(page): add tenant page list endpoint with locale fallback

fix(tenancy): resolve context leak when tenant key not present

perf(page): add index on page slug column for faster lookups
```

### AI Commit Message Generation

After completing multi-file changes, generate and present a commit message for user approval before committing:

```
feat(page): add admin summary endpoint

Adds paginated summary list for the admin UI with en-US fallback title.
Includes MyBatis mapper query and DTO mapping.
---
Accept this message?
```

### Code Quality Standards

#### Java Code Standards (Java 25)

**Use modern Java features:**

```java
// DTOs as nested records inside a container class (project convention)
public final class PageDtos {
  private PageDtos() {}

  public record CreatePageRequest(
      String slug,
      UUID parentId,
      String template,
      PageStatus status,
      List<PageTranslationRequest> translations) {}
}

// Pattern matching with switch expressions
String label = switch (page.status()) {
  case DRAFT -> "Draft";
  case PUBLISHED -> "Published";
  case ARCHIVED -> "Archived";
};

// var for local variables when type is obvious
var pages = pageMapper.findAllSummary(limit, offset);
```

**Constructor injection (no @Autowired, no @RequiredArgsConstructor in this codebase — use explicit constructors):**

```java
@Service
public class PageServiceImpl implements PageService {

  private final PageMapper pageMapper;

  public PageServiceImpl(final PageMapper pageMapper) {
    this.pageMapper = pageMapper;
  }
}
```

**Import order (Checkstyle enforced):**

```java
// 1. Static imports (alphabetically sorted)
import static org.assertj.core.api.Assertions.assertThat;

// 2. Standard Java/Jakarta packages (alphabetically sorted)
import java.util.List;
import java.util.UUID;

// 3. Third-party packages (alphabetically sorted)
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
```

**Import rules:**

- No wildcard imports — explicit only
- No unused imports
- Alphabetical within each group, groups separated by blank line
- No line-wrapping on import/package statements

#### MyBatis Patterns

SQL queries go in XML mapper files under `src/main/resources/mappers/<context>/`. The Java mapper interface lives in the bounded-context package alongside the domain model.

```java
// Mapper interface in page package
@Mapper
public interface PageMapper {
  List<Page> findAll(@Param("limit") int limit, @Param("offset") int offset);
  Optional<Page> findBySlug(@Param("slug") String slug);
  void insert(Page page);
  void update(Page page);
  void deleteById(@Param("id") UUID id);
}
```

```xml
<!-- src/main/resources/mappers/page/PageMapper.xml -->
<mapper namespace="com.iqkv.foundation.cmsservice.page.PageMapper">
  <select id="findBySlug" resultType="com.iqkv.foundation.cmsservice.page.Page">
    SELECT id, slug, parent_id, template, status, created_at, updated_at
    FROM pages
    WHERE slug = #{slug}
  </select>
</mapper>
```

#### REST Controller Pattern

Controllers are named `*RestResource` (not `*Controller`). Each bounded context typically has separate resources for public, tenant-scoped, and admin operations.

```java
@RestController
@RequestMapping("/api/v1/cms/pages")
@Tag(name = "Public Pages", description = "Endpoints for retrieving published CMS pages (tenant-scoped)")
public class PageRestResource {

  private final PageService pageService;

  public PageRestResource(final PageService pageService) {
    this.pageService = pageService;
  }

  @GetMapping("/{slug}")
  @Operation(summary = "Get page by slug")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Page retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Page not found")
  })
  public ResponseEntity<PageDtos.PageResponse> getBySlug(
      @RequestHeader("X-Tenant-ID") final String tenantKey,
      @PathVariable final String slug,
      final Locale resolvedLocale) {
    try {
      TenantContext.setCurrentTenant(tenantKey);
      return ResponseEntity.ok(pageService.getBySlug(slug, resolvedLocale));
    } finally {
      TenantContext.clear();  // Always clear tenant context in finally block
    }
  }
}
```

**Important:** Always clear `TenantContext` in a `finally` block after setting it in a controller.

#### Liquibase Migration Patterns

Migrations are split into two separate changelog trees:

- `db/changelog/system/db.changelog-system.xml` — shared/system tables
- `db/changelog/tenant/db.changelog-tenant.xml` — per-tenant schema tables

When adding a new table that belongs to a tenant's data, add the changeset to the **tenant** changelog. System-level tables (e.g., tenant registry) go in the **system** changelog.

```xml
<!-- Example tenant migration -->
<changeSet id="20240801-01" author="developer">
  <createTable tableName="pages">
    <column name="id" type="uuid">
      <constraints primaryKey="true" nullable="false"/>
    </column>
    <column name="slug" type="varchar(255)">
      <constraints nullable="false" unique="true"/>
    </column>
    <column name="status" type="varchar(50)">
      <constraints nullable="false"/>
    </column>
    <column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP"/>
    <column name="updated_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP"/>
  </createTable>
</changeSet>
```

#### Testing Standards

**Unit Tests — AAA Pattern:**

```java
@ExtendWith(MockitoExtension.class)
class PageServiceImplTest {

  @Mock
  private PageMapper pageMapper;

  @InjectMocks
  private PageServiceImpl pageService;

  @Test
  @DisplayName("Should throw PageNotFoundException when page does not exist")
  void shouldThrowWhenPageNotFound() {
    // Arrange
    when(pageMapper.findById(any(UUID.class))).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(PageNotFoundException.class, () -> pageService.getById(UUID.randomUUID()));
  }
}
```

**Integration Tests:**

Use the `@IntegrationTest` composite annotation (wraps `@SpringBootTest`). Testcontainers are available for PostgreSQL and RabbitMQ.

```java
@IntegrationTest
@Testcontainers
class PageServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

  @DynamicPropertySource
  static void configureProperties(final DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private PageService pageService;
}
```

**Architecture Tests:**

The existing `TechnicalStructureTest` uses `onionArchitecture().withOptionalLayers(true)`. Add new `@ArchTest` rules there — do not create separate architecture test classes.

```java
@AnalyzeClasses(packagesOf = CmsApplication.class, importOptions = DoNotIncludeTests.class)
class TechnicalStructureTest {

  @ArchTest
  static final ArchRule respectsTechnicalArchitectureLayers = onionArchitecture()
      .withOptionalLayers(true)
      .ignoreDependency(belongToAnyOf(CmsApplication.class), alwaysTrue());
}
```

**Security Tests:**

```java
@Test
@WithMockUser(authorities = "PLATFORM_ADMIN")
void shouldAllowAdminAccess() throws Exception {
  mockMvc.perform(get("/api/v1/cms/admin/pages"))
      .andExpect(status().isOk());
}

@Test
@WithMockUser(authorities = "USER")
void shouldDenyNonAdminToAdminEndpoint() throws Exception {
  mockMvc.perform(get("/api/v1/cms/admin/pages"))
      .andExpect(status().isForbidden());
}
```

Note: use `authorities` not `roles` — the security config maps JWT `"authorities"` claim directly to `GrantedAuthority`.

### Maven Command Best Practices for AI Agents

**STRICT RECOMMENDATION: Always use `-Dcheckstyle.skip=true` during active development.**

```bash
# Development and testing
mvn clean verify -Dcheckstyle.skip=true
mvn test -Dcheckstyle.skip=true

# Enable coverage explicitly (skipped by default)
mvn verify -Dcheckstyle.skip=true -Djacoco.skip=false
mvn jacoco:report -Djacoco.skip=false
# View: target/site/jacoco/index.html

# Style check when ready
mvn checkstyle:check

# Architecture tests only
mvn test -Dtest=TechnicalStructureTest -Dcheckstyle.skip=true

# Full quality gate
mvn clean verify
```

```yaml
workflow:
    1. develop: "Implement with -Dcheckstyle.skip=true"
    2. test: "Run tests with -Dcheckstyle.skip=true"
    3. style: "Run mvn checkstyle:check separately before committing"
    4. commit: "CI/CD enforces Checkstyle automatically"

rationale:
    - "Checkstyle violations block build; skip during iteration"
    - "jacoco.skip=true is the default — explicitly opt-in to coverage"
    - "CI/CD enforces all quality gates before merge"
```

## 🔄 Workflow Management

### Pull Request Guidelines

#### PR Title Format

```
type(scope): description

Examples:
feat(page): add slug-based page retrieval with locale fallback
fix(tenancy): resolve tenant context leak on request error
improvement(page): add pagination to admin page list
```

#### PR Description Template

```markdown
## Description

Brief description of changes and motivation.

## Type of Change

- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update
- [ ] Performance improvement
- [ ] Refactoring

## Changes Made

- List specific changes
- Include affected components

## How Has This Been Tested?

- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Test Coverage

- Current coverage: X%
- Coverage change: +/-X%

## Checklist

- [ ] Code follows style guidelines
- [ ] Commit messages follow Conventional Commits
- [ ] Self-review completed
- [ ] Tests cover new/modified code
- [ ] All tests pass locally
- [ ] Tenant context always cleared in `finally` blocks

## Security Considerations

- [ ] No sensitive data in logs
- [ ] Input validation implemented
- [ ] Authorization authority strings match JWT claim values (not role prefixes)
- [ ] Tenant scoping enforced for all data access

## Additional Notes
```

#### Review Criteria

**Automated Checks (Must Pass):**

- ✅ All tests pass
- ✅ Checkstyle validation passes
- ✅ Code coverage meets 60% minimum
- ✅ Commit messages follow Conventional Commits
- ✅ No merge conflicts

**Manual Review Focus:**

```yaml
review_checklist:
    code_quality:
        - Modern Java features used (records, pattern matching, var)
        - Explicit constructor injection (no @Autowired, no @RequiredArgsConstructor)
        - No code duplication; DTOs grouped as nested records in *Dtos class

    tenancy:
        - TenantContext.setCurrentTenant() always paired with TenantContext.clear() in finally
        - New tables added to the correct changelog (system vs tenant)
        - Multi-tenant queries scoped correctly

    security:
        - Authority strings match actual JWT claim values (e.g., PLATFORM_ADMIN not ROLE_ADMIN)
        - New endpoints explicitly configured in SecurityConfig
        - No sensitive data logged

    persistence:
        - MyBatis mapper XML added for new mapper methods
        - UUIDs used as primary keys (not Long/int)
        - underscore_case column names (auto-mapped to camelCase)

    testing:
        - AAA pattern in unit tests
        - @IntegrationTest for Spring context tests
        - Testcontainers used for PostgreSQL/RabbitMQ integration tests
```

## 🔒 Security Guidelines

### Security Checklist

```yaml
authentication:
  - [ ] Service acts as OAuth2 Resource Server (not auth server — no login/register)
  - [ ] RSA public key file present in keys/public.pem
  - [ ] JWT public key path configured via JWT_PUBLIC_KEY_PATH env var in production

authorization:
  - [ ] Authority strings in @PreAuthorize match JWT "authorities" claim values exactly
  - [ ] New endpoints added to SecurityConfig.securityFilterChain()
  - [ ] PLATFORM_ADMIN for cross-tenant admin operations
  - [ ] TENANT_OWNER / ADMIN for tenant-scoped management operations

input_validation:
  - [ ] Bean Validation annotations on request DTOs (@NotBlank, @Valid, etc.)
  - [ ] MyBatis parameterized queries — no string concatenation in SQL
  - [ ] Tenant key validated before setting on TenantContext

data_protection:
  - [ ] No secrets logged (passwords, tokens, keys)
  - [ ] Environment variables for all secrets (DB_PASSWORD, RABBITMQ_PASSWORD, etc.)
  - [ ] .env files gitignored
  - [ ] Tenant data strictly isolated at query level

secrets_management:
  - [ ] No hardcoded credentials in source code or config files
  - [ ] application-prd.yml references only env var placeholders
  - [ ] JWT public key loaded from configurable path, not hardcoded
```

### Environment Variables Reference

| Variable              | Default (local)                     | Description                          |
| --------------------- | ----------------------------------- | ------------------------------------ |
| `DB_HOST`             | `localhost`                         | PostgreSQL host                      |
| `DB_PORT`             | `5432`                              | PostgreSQL port                      |
| `DB_NAME`             | `cmsservice`                        | Database name                        |
| `DB_USERNAME`         | `svc_cms_dba`                       | Database user                        |
| `DB_PASSWORD`         | `svc_cms_dba`                       | Database password                    |
| `RABBITMQ_HOST`       | `localhost`                         | RabbitMQ host                        |
| `RABBITMQ_PORT`       | `5672`                              | RabbitMQ port                        |
| `RABBITMQ_USERNAME`   | `svc_cms_rmq`                       | RabbitMQ user                        |
| `RABBITMQ_PASSWORD`   | `svc_cms_rmq`                       | RabbitMQ password                    |
| `JWT_PUBLIC_KEY_PATH` | `classpath:keys/public.pem`         | RSA public key location              |
| `ROLLOUT_MODE`        | `MULTI_TENANT`                      | `MULTI_TENANT` or `SINGLE_TENANT`    |
| `DEFAULT_TENANT_KEY`  | `platform`                          | Used only in `SINGLE_TENANT` mode    |
| `DEFAULT_TENANT_NAME` | `Acme Corp.`                        | Display name in `SINGLE_TENANT` mode |
| `BILLING_SERVICE_URI` | `http://foundation-billing-service` | Billing service base URL             |

## 📊 Quality Assurance

### Code Quality Metrics

```yaml
test_coverage:
    minimum_threshold: ">= 60%"
    excluded: "**/*Application.class"
    note: "jacoco.skip=true by default; run with -Djacoco.skip=false to generate report"

code_style:
    tool: "Checkstyle (via parent POM configuration)"
    enforcement: "Build fails on violations"
    skip_flag: "-Dcheckstyle.skip=true"

architecture:
    tool: "ArchUnit"
    test_class: "TechnicalStructureTest"
    rule: "onionArchitecture().withOptionalLayers(true)"
```

### Local Development Setup

Start required infrastructure:

```bash
docker compose up postgres-cms rabbitmq-cms
```

Then run the app with the `local` profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile enables:

- Liquibase with `demo` context (seeds demo tenants: `platform`, `acme0001`, `demo0001`)
- All actuator endpoints exposed
- DEBUG logging for `com.iqkv` and MyBatis
- RabbitMQ messaging enabled

### Application Profiles

| Profile     | Purpose                                                       |
| ----------- | ------------------------------------------------------------- |
| _(default)_ | Base config, Liquibase disabled, messaging disabled           |
| `local`     | Local dev with Docker Compose infra, demo data, full actuator |
| `sit`       | System integration testing                                    |
| `uat`       | User acceptance testing                                       |
| `prd`       | Production                                                    |

## 🚀 CI/CD Pipeline

No Java CI workflow is currently present in `.github/workflows/` — only a Node.js project workflow exists. When adding a Java CI pipeline, follow this structure:

```yaml
name: CI/CD Pipeline

on:
    push:
        branches: [main, develop]
    pull_request:
        branches: [main, develop]

jobs:
    build-and-test:
        runs-on: ubuntu-latest
        steps:
            - uses: actions/checkout@v4

            - name: Set up JDK 25
              uses: actions/setup-java@v4
              with:
                  java-version: "25"
                  distribution: "temurin"
                  cache: "maven"

            - name: Build and Test
              run: mvn clean verify -Djacoco.skip=false

            - name: Upload Coverage
              uses: codecov/codecov-action@v3
              with:
                  files: target/site/jacoco/jacoco.xml
```

## 📚 Documentation Standards

### API Documentation with OpenAPI

All REST controllers must use SpringDoc annotations. Follow the pattern established in `PageRestResource`:

```java
@RestController
@RequestMapping("/api/v1/cms/pages")
@Tag(name = "Public Pages", description = "Tenant-scoped public CMS page endpoints")
public class PageRestResource {

  @GetMapping("/{slug}")
  @Operation(
      summary = "Get page by slug",
      description = "Retrieves a published page by slug with locale fallback (exact → language → en-US)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Page found"),
      @ApiResponse(responseCode = "404", description = "Page not found")
  })
  public ResponseEntity<PageDtos.PageResponse> getBySlug(
      @Parameter(description = "8-char tenant key") @RequestHeader("X-Tenant-ID") final String tenantKey,
      @PathVariable final String slug,
      final Locale resolvedLocale) { ... }
}
```

### Code Documentation

Document:

- Complex business logic (e.g., locale fallback chains)
- Non-obvious algorithms
- Security-critical code
- Public service interface methods

Do not document:

- Self-explanatory getters/setters
- Obvious wrapper methods
- Boilerplate constructors

## 🎯 Agent Decision Framework

### User Confirmation Policy

**CRITICAL RULE: Always ask for user confirmation before applying changes.**

```yaml
before_making_changes:
    1. analyze: "Read relevant files and understand the request"
    2. explain: "Describe what changes will be made and why"
    3. assess_impact: "Evaluate impact, effort, and risk"
    4. wait_for_approval: "STOP and wait for explicit user confirmation"
    5. apply_changes: "Only after user approves"
    6. verify: "Run tests; confirm changes work"

exceptions:
    - read_only_operations: "Reading files, searching, analyzing"
    - information_requests: "Answering questions, explaining concepts"

never_auto_apply:
    - code_changes: "Any modification to source files"
    - configuration_changes: "application.yml, pom.xml, etc."
    - dependency_updates: "Adding or updating dependencies"
    - schema_changes: "Liquibase changelog files"
    - security_changes: "SecurityConfig, JWT config, public keys"
```

### Common Patterns and Solutions

**Pattern: Adding a new REST endpoint**

```
1. Add method to PageService interface
2. Implement in PageServiceImpl (with tenant context awareness)
3. Add mapper method to PageMapper interface
4. Add SQL query to mappers/page/PageMapper.xml
5. Add controller method to the appropriate *RestResource class
6. Update SecurityConfig if the endpoint has new access rules
7. Add unit test for service method
8. Run: mvn test -Dcheckstyle.skip=true
9. Run: mvn checkstyle:check
```

**Pattern: Adding a Liquibase migration**

```
Tenant data table → db/changelog/tenant/
System/shared table → db/changelog/system/
Use UUID primary keys, timestamp columns (created_at, updated_at)
Use underscore_case for column names (auto-mapped to camelCase by MyBatis)
```

**Pattern: Adding a new bounded context**

Follow the flat layout of the existing `page/` context:

- Domain model, service interface, service impl, mapper, REST resources, and DTOs all in `com.iqkv.foundation.cmsservice.<context>/`
- DTOs grouped as nested records in a single `<Context>Dtos.java` class
- Mapper XML in `src/main/resources/mappers/<context>/`
- The ArchUnit onion test validates structure automatically

---

This document is authoritative for this repository. When in doubt about a pattern or convention, check the `page/` bounded context as the reference implementation.
