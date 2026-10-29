# Medication Organizer - Backend

REST API for the medication organizer. The backend stores user accounts, supported organizer models, medication schedules, preparation progress, and user-reported intake history. The Kotlin Multiplatform client communicates with it over JSON/HTTP.

The application organizes information entered by the user or an authorized person. It does not diagnose conditions or recommend treatments or dosage changes.

## Getting Started

1. Ensure Docker is installed and running.
2. Configure your environment variables in `.env`.
3. Generate an RSA Key Pair:

```bash
mkdir -p src/main/resources/certs
cd src/main/resources/certs
openssl genrsa -out keypair.pem 2048
openssl rsa -in keypair.pem -pubout -out public.pem
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem
rm keypair.pem
```

Keep `.env` and RSA keys out of version control.

4. Start the application:

```bash
docker compose up --build --watch
```

The API base URL is `http://localhost:8080`.

## MVP scope

- Registration, login, profile, and synchronized preferences.
- QR/product-code lookup and manual selection of supported organizer models.
- User-to-organizer assignments and compartment layouts.
- Medication records, recurring schedules, daily/weekly plans, and intake history.
- Organizer preparation progress and ready-state validation.
- Backend validation, per-user authorization, and automated tests.

Organizer photo analysis is Stage II; caregiver access and shared history are Stage III. Neither is part of the MVP.

## Tech stack

- Java 25
- Spring Boot 4.1.2
    - Spring Web
    - Spring Security
    - OAuth2 Resource Server
    - Spring Data JPA
    - MySQL Driver
    - Testcontainers
    - Validation
    - Spring Boot DevTools
- Maven
- MySQL
- Docker Compose
- OpenSSL for generating the RSA key pair used by JWT signing/verification

## Package responsibilities

Base package: `com.matt1479.medicationorganizer`

| Package | Responsibility |
| --- | --- |
| `auth` | Registration, login, and token issuance |
| `config` | Security, JWT keys, password encoding, application configuration |
| `security` | Current-user lookup and reusable ownership checks |
| `users` | User account and profile |
| `preferences` | Timezone, locale, accessibility, and reminder preferences |
| `organizers` | Organizer-model catalog, compartment definitions, QR lookup, user assignments |
| `medications` | Medication data and recurring schedule rules |
| `schedule` | Date-specific dose occurrences and daily/weekly plans |
| `preparation` | Preparation sessions and compartment progress |
| `intake` | Taken/skipped records and history |
| `reminders` | Snooze state and reminder-related logic; the mobile OS displays local notifications |
| `common` | Shared error handling and API error DTOs |

Keep controllers focused on HTTP/DTO handling, services responsible for business rules and ownership checks, and repositories responsible for persistence. Do not expose JPA entities directly from controllers. The detailed class/entity model and API contract are in [`design.md`](docs/design.md).

## Implementation order and branches

Implement and merge each branch into `main` before starting the next. This sequence is the recommended development path.

| Step | Branch | Deliverable |
| ---: | --- | --- |
| 1 | `chore/setup` | Maven dependencies, application configuration, Docker Compose, database migrations, test foundation |
| 2 | `feat/users` | User/profile/preferences model, repositories, services, and profile endpoints |
| 3 | `feat/jwt-authentication` | Registration/login, BCrypt, RSA JWT signing/validation, security configuration |
| 4 | `feat/organizers` | Organizer catalog, compartment definitions, QR lookup, user assignments |
| 5 | `feat/medications` | Medication records and recurring schedule rules |
| 6 | `feat/daily-plan-intake` | Dose occurrence generation, daily/weekly plan, intake history, snooze state |
| 7 | `feat/preparation` | Preparation sessions, compartment progress, ready-state validation |
| 8 | `test/integration` | MySQL Testcontainers, JWT, ownership, scheduling, and state-transition tests |
| 9 | `docs/readme-design` | Finalize documentation against the implemented API |

Use Conventional Commit messages, for example `feat(organizers): add QR model lookup`. To preserve a merge commit:

```bash
git switch main
git merge --no-ff branch-name -m "type(scope): description"
```

## Development principles

- Keep the application a modular monolith; controllers handle HTTP/DTOs, services hold business logic, and repositories handle persistence.
- Implement the domain model and API contract in [`design.md`](docs/design.md) as the detailed reference; update it when implementation decisions change.
- Add tests for each feature branch, especially authorization boundaries, scheduling edge cases, and state transitions.
- Keep secrets and private keys out of version control. Never return password hashes or security-sensitive configuration from the API.
