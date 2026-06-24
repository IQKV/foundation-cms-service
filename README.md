> ## 🤔 What is this service all about?
>
> - Content management microservice for the IQ Key Value Platform.
> - Centralized page management with hierarchical content structure and multi-language support (with en-US fallback).
> - Quick-start documentation
> - Manage issues with 20+ issue labels.
> - Make community healthier with all the guides like code of conduct, contributing, support, security...

---

# 📄 IQ Key Value CMS Service

Content management microservice for the IQ Key Value Platform. Provides centralized page management with hierarchical content structure, multi-language support (with en-US fallback), and tenant isolation.

## About

The CMS service owns content management for the platform:

- **Static page management** — create, edit, and publish static pages (legal, marketing, landing pages, etc.) with support for publishing statuses (draft/published) and page templates
- **Full multi-language support** — locale-based content with fallback to English (en-US), ensuring content is always available
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

### Pages (Public)

| Method | Path            | Auth                    | Description                                                         |
| ------ | --------------- | ----------------------- | ------------------------------------------------------------------- |
| `GET`  | `/pages`        | None, `X-Tenant-ID` req | List all published pages with en-US fallback for requested locale   |
| `GET`  | `/pages/{slug}` | None, `X-Tenant-ID` req | Get published page by slug with en-US fallback for requested locale |

### Pages (Platform Admin)

| Method   | Path                            | Auth                 | Description                |
| -------- | ------------------------------- | -------------------- | -------------------------- |
| `GET`    | `/admin/{tenantKey}/pages`      | JWT `PLATFORM_ADMIN` | List all pages (paginated) |
| `GET`    | `/admin/{tenantKey}/pages/{id}` | JWT `PLATFORM_ADMIN` | Get page by ID             |
| `POST`   | `/admin/{tenantKey}/pages`      | JWT `PLATFORM_ADMIN` | Create a new page          |
| `PUT`    | `/admin/{tenantKey}/pages/{id}` | JWT `PLATFORM_ADMIN` | Update an existing page    |
| `DELETE` | `/admin/{tenantKey}/pages/{id}` | JWT `PLATFORM_ADMIN` | Delete a page              |

## Events & Messaging

The CMS service publishes content lifecycle events to RabbitMQ for downstream consumption.

**Exchange**: `iqkv.events` (Topic)

### Page Events

| Routing Key        | Event Type         | Description                      |
| ------------------ | ------------------ | -------------------------------- |
| `cms.page.created` | `CMS_PAGE_CREATED` | New page created                 |
| `cms.page.updated` | `CMS_PAGE_UPDATED` | Page content or metadata updated |
| `cms.page.deleted` | `CMS_PAGE_DELETED` | Page deleted                     |

## Tech Stack

- Java 25 / Spring Boot 4.1
- MyBatis 3.x (no JPA) + PostgreSQL 17
- Liquibase for schema migrations
- RabbitMQ for async event publishing
- Micrometer + Prometheus

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

| Variable            | Default       | Description        |
| ------------------- | ------------- | ------------------ |
| `DB_HOST`           | `localhost`   | PostgreSQL host    |
| `DB_PORT`           | `5432`        | PostgreSQL port    |
| `DB_NAME`           | `cms`         | Database name      |
| `DB_USERNAME`       | `svc_cms_dba` | Database user      |
| `DB_PASSWORD`       | `svc_cms_dba` | Database password  |
| `RABBITMQ_HOST`     | `localhost`   | RabbitMQ host      |
| `RABBITMQ_PORT`     | `5672`        | RabbitMQ AMQP port |
| `RABBITMQ_USERNAME` | `svc_cms_rmq` | RabbitMQ user      |
| `RABBITMQ_PASSWORD` | `svc_cms_rmq` | RabbitMQ password  |

Copy `.env.example` to `.env.local` (or `.env.uat` / `.env.prd`) and fill in production values.

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

The Dockerfile uses a multi-stage build: Maven compiles in `eclipse-temurin:25-jdk-alpine`, the runtime stage uses `eclipse-temurin:25-jre-alpine` with a non-root `appuser` and layered JAR extraction for optimal cache reuse.

Note: The root `compose.yaml` is for development purposes only and is self-contained. It starts all required external services but excludes the CMS Service itself, which should be run locally in your IDE for a better development experience.

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

> See [AGENTS.md](AGENTS.md) for repository structure, DDD patterns, and agent guidelines.
