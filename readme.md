# MinoBank

![CI](https://github.com/MA9IK/MinoBank/actions/workflows/ci.yml/badge.svg)

A banking backend built with Java and Spring Boot, as a long-term project to learn and practice how a bank-like system is designed: an immutable double-entry ledger, safe handling of concurrent money movement, and a secure API.

> **Work in progress.** The project skeleton, database foundation and test setup are done. The ledger is the next step. See [Status](#status).

---

## Status

| Phase | Goal | State |
|---|---|---|
| 0. Skeleton | Project setup, Docker, Flyway, Testcontainers, CI | Done (`v0.0.1`) |
| 1. Ledger core | Accounts, transactions, double-entry ledger, idempotency, concurrency safety | Next |
| 2. Identity and security | Registration, login, JWT with rotating refresh tokens, RBAC | Planned |
| 3. Customers, KYC and admin | KYC workflow, admin API, account freezing, audit log | Planned |
| 4. Statements and insights | Categories, spending summaries, reconciliation job | Planned |
| 5. Observability and docs | Metrics, dashboards, OpenAPI, Docker image | Planned |
| 6. Showcase | Minimal frontend, load test, final documentation | Planned |

Also planned: a CD workflow (publishing a Docker image on each release).

**Done so far**
- Spring Boot project with the Spring Modulith dependency and an architecture test
- PostgreSQL in Docker Compose, schema managed only by Flyway migrations
- `users`, `roles` and `user_roles` tables with named constraints
- Integration tests against a real PostgreSQL (Testcontainers) that check the schema rules
- GitHub Actions pipeline that builds and tests every push and pull request
- Git flow with a first tagged release

---

## Tech stack

**Current:** Java 21, Spring Boot, Spring Modulith, PostgreSQL 16, Flyway, Maven, Docker Compose, Testcontainers, JUnit 5, AssertJ, GitHub Actions

**Planned:** Spring Security (JWT), Spring Data JPA, Micrometer, Prometheus and Grafana, springdoc OpenAPI, React

---

## Architecture

MinoBank is a **modular monolith** built with Spring Modulith: one deployable application, split into modules with strict boundaries. It sits between a plain monolith and microservices: simple to run and test, while keeping modules independent enough to be extracted into services later.

Planned modules:

| Module | Responsibility |
|---|---|
| `identity` | Users, roles, authentication, refresh tokens |
| `customer` | Customer profiles, KYC review |
| `ledger` | Accounts, transactions, ledger entries |
| `audit` | Append-only audit log |

Modules communicate through their public APIs and domain events only. An architecture test (`ModulithTests`) verifies the boundaries on every build.

---

## Key design decisions

- **Money is stored as integers in minor units** (`bigint`, `long` in Java), never as floating point.
- **The ledger is the source of truth.** Ledger entries are append-only and sum to zero per transaction. An account's stored balance is only a cached projection of the ledger, and mistakes are corrected with reversal transactions, not edits.
- **Users, customers and accounts are separate concepts:** credentials, the verified legal person, and the money.
- **Rules live in the database too.** Constraints are named (`pk_`, `uq_`, `ck_`, `fk_`, `ix_` prefixes) and covered by tests, so a future migration can't silently remove them.
- **Schema changes only through Flyway migrations,** grouped by module, and never edited after they are merged.
- **Tests run against real PostgreSQL** with Testcontainers, not an in-memory database, because the ledger relies on PostgreSQL-specific behavior (locking, triggers, `citext`).

---

## Getting started

**Prerequisites:** Java 21, Docker (with Compose), Git

```bash
git clone https://github.com/MA9IK/MinoBank.git
cd MinoBank

# create your local configuration
cp .env.example .env

# start PostgreSQL
docker compose up -d

# run the application (Flyway applies the migrations on startup)
./mvnw spring-boot:run
```

To stop the database: `docker compose down` (add `-v` to also delete its data).

`.env` holds local credentials and is not committed. `.env.example` is the template.

### Run the tests

```bash
./mvnw verify
```

Docker must be running: the integration tests start their own temporary PostgreSQL container.

---

## Testing approach

- **Unit tests** for pure logic: fast, no Spring, no database.
- **Integration tests** for anything that depends on the database or Spring wiring. They share one base class, so the application context and the PostgreSQL container start once per test run.
- **Schema tests** check each rule that matters (unique emails ignoring case, allowed statuses, cascade and restrict behavior), including both the rejected and the accepted cases.
- **Architecture test** with Spring Modulith checks module boundaries.

---

## Git workflow

The project uses **git flow**:

- `main`: released versions only, each tagged (`v0.0.1`)
- `develop`: integration branch, always green
- `feature/*`: one small piece of work, merged into `develop` through a pull request
- `release/*` and `hotfix/*`: merged into `main` and back into `develop`

Commits and pull request titles follow [Conventional Commits](https://www.conventionalcommits.org/) (`feat(identity): ...`, `test(ledger): ...`). CI must pass before anything is merged.

---

## Project structure

```
src/main/java/com/aiven/minobank/   application code (modules)
src/main/resources/db/              Flyway migrations
src/test/java/com/aiven/minobank/   tests (base class, schema tests, architecture test)
compose.yaml                        local PostgreSQL
.github/workflows/                  CI pipeline
```

---

## Scope and non-goals

This is a learning project. It follows banking design principles, but it is not a real bank:

- no real SEPA/SWIFT connectivity (external payments will be simulated)
- no formal PSD2, PCI or AML compliance
- no multi-currency conversion

---