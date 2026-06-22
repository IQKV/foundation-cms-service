# Foundation CMS Service 📄

Content management microservice for the Key Value Platform. Provides centralized page management with hierarchical content structure, multi-language support with en-US fallback, and tenant isolation.

## About

The CMS service owns content management for the platform:

- **Static page management** — create, edit, and publish static pages (legal, marketing, landing pages, etc.) with support for publishing statuses (draft/published) and page templates
- **Full multi-language support** — locale-based content with mandatory English (en-US) fallback, ensuring content is always available
- **Hierarchical content structure** — parent/child page relationships with clean, SEO-friendly slugs
- **SEO-friendly metadata** — per-page SEO fields including title, description, Open Graph tags, and canonical URLs
- **Tenant isolation** — schema-per-tenant architecture ensuring complete data separation between tenants
- **Observability** — instrumented with Micrometer for Prometheus metrics

## Quick Links

- [API Documentation](./docs/api/README.md)
- [Architecture Overview](./docs/architecture/README.md)
- [Deployment Guide](./docs/deployment/README.md)
- [Contributing Guidelines](.github/CONTRIBUTING.md)

## API

Base path: `/api/v1/cms`

### Pages (Public) — `/api/v1/cms/pages`

| Method | Path            | Auth                    | Description                                                         |
| ------ | --------------- | ----------------------- | ------------------------------------------------------------------- |
| `GET`  | `/pages`        | None, `X-Tenant-ID` req | List all published pages with en-US fallback for requested locale   |
| `GET`  | `/pages/{slug}` | None, `X-Tenant-ID` req | Get published page by slug with en-US fallback for requested locale |

### Pages (Platform Admin) — `/api/v1/cms/admin/{tenantKey}/pages`

| Method   | Path                            | Auth                 | Description                |
| -------- | ------------------------------- | -------------------- | -------------------------- |
| `GET`    | `/admin/{tenantKey}/pages`      | JWT `PLATFORM_ADMIN` | List all pages (paginated) |
| `GET`    | `/admin/{tenantKey}/pages/{id}` | JWT `PLATFORM_ADMIN` | Get page by ID             |
| `POST`   | `/admin/{tenantKey}/pages`      | JWT `PLATFORM_ADMIN` | Create a new page          |
| `PUT`    | `/admin/{tenantKey}/pages/{id}` | JWT `PLATFORM_ADMIN` | Update an existing page    |
| `DELETE` | `/admin/{tenantKey}/pages/{id}` | JWT `PLATFORM_ADMIN` | Delete a page              |

## Events

### Published (to RabbitMQ `iqkv.events` exchange)

| Routing Key        | Trigger                          |
| ------------------ | -------------------------------- |
| `cms.page.created` | New page created                 |
| `cms.page.updated` | Page content or metadata updated |
| `cms.page.deleted` | Page deleted                     |

## Tech Stack

- Java 25 / Spring Boot 4.0
- MyBatis 3.x (no JPA) + PostgreSQL 17
- Liquibase for schema migrations
- RabbitMQ for async event publishing
- Micrometer + Prometheus
- springdoc-openapi (Swagger UI)

## Prerequisites

- JDK 25 (Eclipse Temurin)
- Maven 3.9+
- Node.js >= 22.15.0 & pnpm >= 11.0.8 (git hooks)
- Docker & Docker Compose

## Quick Start

```bash
# Clone the repository
git clone https://github.com/IQKV/foundation-cms-service.git
cd foundation-cms-service

# Install git hooks
pnpm install

# Copy environment variables
cp .env.example .env.local
# Edit .env.local — defaults work for local Docker setup

# Start infrastructure (PostgreSQL, RabbitMQ)
docker compose up -d

# Run the service (locally via IDE or CLI)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# → API:      http://localhost:8080
# → Actuator: http://localhost:8081/actuator/health
# → Swagger:  http://localhost:8080/swagger-ui.html
```

## Environment Variables

| Variable              | Default                     | Description                                            |
| --------------------- | --------------------------- | ------------------------------------------------------ |
| `DB_HOST`             | `localhost`                 | PostgreSQL host                                        |
| `DB_PORT`             | `5432`                      | PostgreSQL port                                        |
| `DB_NAME`             | `cms`                       | Database name                                          |
| `DB_USERNAME`         | `svc_cms_dba`               | Database user                                          |
| `DB_PASSWORD`         | `svc_cms_dba`               | Database password                                      |
| `RABBITMQ_HOST`       | `localhost`                 | RabbitMQ host                                          |
| `RABBITMQ_PORT`       | `5672`                      | RabbitMQ AMQP port                                     |
| `RABBITMQ_USERNAME`   | `svc_cms_rmq`               | RabbitMQ user                                          |
| `RABBITMQ_PASSWORD`   | `svc_cms_rmq`               | RabbitMQ password                                      |
| `JWT_PUBLIC_KEY_PATH` | `classpath:keys/public.pem` | RS256 public key (from IAM)                            |
| `ROLLOUT_MODE`        | `MULTI_TENANT`              | Platform mode: `MULTI_TENANT` or `SINGLE_TENANT`       |
| `MESSAGING_ENABLED`   | `true`                      | Toggle RabbitMQ publishing (set `false` for local dev) |

> Copy `.env.example` to `.env.local` / `.env.uat` / `.env.prd` and fill in values per environment.

## Maven Commands

```bash
# Build and test (skip Checkstyle during development)
./mvnw clean verify -Dcheckstyle.skip=true

# Run tests only
./mvnw test -Dcheckstyle.skip=true

# Explicit Checkstyle check
./mvnw checkstyle:check

# Coverage report → target/site/jacoco/index.html
./mvnw jacoco:report

# Production build
./mvnw clean package -Pproduction
```

## Docker

```bash
# Build image
docker build -t iqkv/foundation-cms-service:latest .

# Run full stack (CMS Service + Infrastructure)
docker compose -f compose.container.yaml up -d
```

Note: The root `compose.yaml` is for development purposes only and is self-contained. It starts all required external services (PostgreSQL with pre-initialized `cms` database, RabbitMQ) but excludes the CMS Service itself, which should be run locally in your IDE for a better development experience.

## Monitoring

| Endpoint                   | Description                 |
| -------------------------- | --------------------------- |
| `GET /actuator/health`     | Liveness + readiness probes |
| `GET /actuator/metrics`    | Application metrics         |
| `GET /actuator/prometheus` | Prometheus scrape endpoint  |
| `GET /swagger-ui.html`     | API documentation           |

The service is instrumented with custom business metrics:

- **Content Management**: `cms_pages_total`, `cms_pages_published_total`
- **System Health**: `cms_events_published_total`

## Project Structure

```
src/main/java/com/iqkv/foundation/cms/
├── page/                  # Page management — CRUD, publishing status, multi-language, hierarchical structure
│   ├── dto/               # Page request/response DTOs
│   ├── Page.java          # Page entity
│   ├── PageTranslation.java # Page translation entity
│   ├── PageService.java   # Page service interface
│   ├── PageServiceImpl.java # Page service implementation
│   └── PageMapper.java    # MyBatis mapper interface
├── tenancy/               # Tenant context extraction, schema routing
├── shared/                # Common exceptions, utilities
└── infrastructure/        # Spring config, security, MyBatis, RabbitMQ setup
```

## License

This project is licensed under the Apache License. See the [LICENSE](LICENSE) file for details.

## Contributing

Please read our [Contributing Guidelines](.github/CONTRIBUTING.md) and [Code of Conduct](.github/CODE_OF_CONDUCT.md).

---

## 🧩 Boilerplate Architecture

- **Persistence**: MyBatis with XML mappers + PostgreSQL; Liquibase manages schema migrations (tenant-specific tables)
- **Messaging**: RabbitMQ publisher for content lifecycle events; `iqkv.messaging.rabbitmq.enabled` toggle — disabled in base, enabled per profile
- **Security**: Spring Security + OAuth2 Resource Server; RS256 JWT validated via public key; `@PreAuthorize` on every endpoint
- **Multi-tenancy**: `ROLLOUT_MODE` (`MULTI_TENANT` | `SINGLE_TENANT`); must be identical across all platform services
- **Observability**: Micrometer + Prometheus; structured JSON logging with Logstash encoder; health probes for Kubernetes
- **GitHub Integration**: Issue templates, labels, Dependabot, and CI workflows
- **Quality Tools**: Checkstyle, JaCoCo (60% gate), ArchUnit, commit convention enforcement

> See [AGENTS.md](AGENTS.md) for repository structure, DDD patterns, and agent guidelines.
