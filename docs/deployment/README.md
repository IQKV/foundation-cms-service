## 📜 Deployment Guide

This guide reflects the current deployment shape of `foundation-cms-service` as implemented in the repository today.

## Deployment Modes

| Mode | Entry Point | What Runs | Typical Use |
| ---- | ----------- | --------- | ----------- |
| Local IDE / CLI | `compose.yaml` + `local` profile | PostgreSQL, RabbitMQ, MailHog, DbGate, SonarQube, Prometheus, Grafana | Day-to-day development |
| Local Container Stack | `compose.container.yaml` | CMS service container plus PostgreSQL, RabbitMQ, MailHog, DbGate | End-to-end local validation |
| SIT / UAT / PRD | Spring profile + external infra | CMS service connected to managed PostgreSQL and RabbitMQ | Cluster deployment |

## Runtime Topology

- Application port: `8080`
- Management / Actuator port: `8081`
- Persistence: PostgreSQL
- Messaging: RabbitMQ
- API docs: enabled outside hardened production profile
- Authentication: RS256 JWT resource server using the IAM public key
- Multi-tenancy: controlled by `ROLLOUT_MODE` and tenant schema routing

## Profiles

| Profile | Liquibase | Demo Data | RabbitMQ Publishing | Notes |
| ------- | --------- | --------- | ------------------- | ----- |
| `local` | enabled | `platform`, `acme0001`, `demo0001` | enabled | Exposes all actuator endpoints and debug logging |
| `sit` | enabled | `platform`, `acme0001`, `demo0001` | enabled | Uses SIT service names by default |
| `uat` | enabled | `platform`, `acme0001`, `demo0001` | enabled | All connection values come from env vars |
| `prd` | enabled | `platform` only | enabled | Swagger disabled, health details hidden, no demo context |

## Local Development

### Infrastructure-Only Flow

Start the local dependencies:

```bash
docker compose up -d
```

This starts:

- `postgres-cms`
- `rabbitmq-cms`
- `mailhog`
- `dbgate`
- `sonar`
- `prometheus`
- `grafana`

Run the service from your IDE or CLI:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Useful local URLs:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator health: `http://localhost:8081/actuator/health`
- RabbitMQ UI: `http://localhost:15672`
- MailHog UI: `http://localhost:8025`
- DbGate: `http://localhost:3100`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- SonarQube: `http://localhost:9000`

### Full Local Container Flow

Build and run the CMS service inside Docker together with its dependencies:

```bash
docker compose -f compose.container.yaml up -d
```

This uses the repository `Dockerfile` and starts the `cms` container with the `local` profile, PostgreSQL, RabbitMQ, MailHog, and DbGate.

## Container Image

The repository `Dockerfile` is a multi-stage build:

- Builder: `eclipse-temurin:25-jdk-alpine`
- Runtime: `eclipse-temurin:25-jre-alpine`
- Runtime user: non-root `appuser` (`uid=1001`)
- Exposed ports: `8080`, `8081`
- Container healthcheck: `GET /actuator/health/readiness`

Build locally with:

```bash
docker build -t iqkv/foundation-cms-service:latest .
```

## Required Configuration

The service reads its runtime configuration from environment variables mapped into `application.yml` and the active profile overrides.

### Core Variables

| Variable | Default | Required In | Purpose |
| -------- | ------- | ----------- | ------- |
| `DB_HOST` | `localhost` | all envs | PostgreSQL host |
| `DB_PORT` | `5432` | all envs | PostgreSQL port |
| `DB_NAME` | `cms` | all envs | Database name |
| `DB_USERNAME` | `svc_cms_dba` | all envs | Database user |
| `DB_PASSWORD` | `svc_cms_dba` | all envs | Database password |
| `RABBITMQ_HOST` | `localhost` | all envs | RabbitMQ host |
| `RABBITMQ_PORT` | `5672` | all envs | RabbitMQ port |
| `RABBITMQ_USERNAME` | `svc_cms_rmq` | all envs | RabbitMQ user |
| `RABBITMQ_PASSWORD` | `svc_cms_rmq` | all envs | RabbitMQ password |
| `JWT_PUBLIC_KEY_PATH` | `classpath:keys/public.pem` | all envs | Public key for validating IAM-issued JWTs |
| `BILLING_SERVICE_URI` | `http://foundation-billing-service` | env-specific | Billing service base URL for plan catalog refresh |
| `PLAN_CATALOG_REFRESH_INTERVAL` | `PT10M` | optional | Plan catalog refresh cadence |

### Platform / Tenancy Variables

| Variable | Default | Purpose |
| -------- | ------- | ------- |
| `ROLLOUT_MODE` | `MULTI_TENANT` | Platform-wide mode; must match other platform services |
| `DEFAULT_TENANT_KEY` | `platform` | Default tenant key for `SINGLE_TENANT` mode |
| `DEFAULT_TENANT_NAME` | `Acme Corp.` | Display name for the default tenant |

## Environment Preparation

The repository includes `.env.example` as a starting point:

```bash
cp .env.example .env.local
```

For local CLI runs, typical values are:

```dotenv
SPRING_PROFILES_ACTIVE=local
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cms
DB_USERNAME=svc_cms_dba
DB_PASSWORD=svc_cms_dba
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=svc_cms_rmq
RABBITMQ_PASSWORD=svc_cms_rmq
JWT_PUBLIC_KEY_PATH=classpath:keys/public.pem
ROLLOUT_MODE=MULTI_TENANT
```

For `uat` and `prd`, do not rely on local defaults. Supply explicit values for database, RabbitMQ, JWT public key, billing URL, and rollout mode.

## Liquibase and Demo Tenants

- Base `application.yml` disables Spring Boot Liquibase auto-configuration and delegates to the service’s explicit Liquibase setup.
- `local`, `sit`, and `uat` enable Liquibase with `demo` context and seed demo tenants.
- `prd` enables Liquibase with `!demo`, so production skips demo data.

## Cluster Deployment Notes

For SIT, UAT, and production-style deployments:

- Run with the matching Spring profile: `sit`, `uat`, or `prd`
- Provide PostgreSQL and RabbitMQ connectivity via environment variables or platform secret/config injection
- Ensure `JWT_PUBLIC_KEY_PATH` points to the IAM public key material used by the platform
- Keep `ROLLOUT_MODE` aligned with IAM, Billing, and Gateway
- In production, expect:
  - Swagger UI disabled
  - OpenAPI docs disabled
  - Actuator health details hidden

## Health and Operations

Primary operational endpoints:

| Endpoint | Purpose |
| -------- | ------- |
| `GET /actuator/health` | Overall health |
| `GET /actuator/health/liveness` | Liveness probe |
| `GET /actuator/health/readiness` | Readiness probe |
| `GET /actuator/info` | Build and app metadata |
| `GET /actuator/prometheus` | Prometheus scrape endpoint |

Profile behavior:

- `local` exposes all actuator endpoints
- `prd` keeps the standard exposed set from base config and hides health details

## Deployment Checklist

- PostgreSQL reachable and database created
- RabbitMQ reachable and credentials valid
- Correct Spring profile selected
- `JWT_PUBLIC_KEY_PATH` points to the IAM public key
- `ROLLOUT_MODE` matches the rest of the platform
- Liquibase migration strategy matches the target environment
- Readiness probe on port `8081` is green before traffic is sent
