# Medication Organizer - Backend Design

## 1. Purpose and architecture

The backend is a Java 25 / Spring Boot 4.1.2 REST API for the medication organizer. The Kotlin Multiplatform client provides the UI; the backend persists accounts, organizer catalog data, medication schedules, preparation state, and intake history.

Use a **modular monolith** with one Spring Boot application and a MySQL database. The package map and implementation branch sequence are maintained in [`README.md`](README.md); this document focuses on domain design, API behavior, and important rules.

```text
Kotlin Multiplatform client
        | HTTPS / JSON + Bearer JWT
        v
Spring Security -> REST controllers -> services -> Spring Data JPA -> MySQL
```

Controllers handle HTTP and DTOs; services enforce business rules, transactions, and ownership; repositories handle persistence. Use request/response DTOs rather than serializing JPA entities.

## 2. High-level entity model

Types are conceptual Java types. `Long` is assumed for identifiers; use `Instant` for event timestamps, `LocalDate` for calendar dates, `LocalTime` for local schedule times, and enums for constrained states.

### User

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `email` | `String` | Required, unique, normalized |
| `passwordHash` | `String` | Required password hash; never returned by the API |
| `displayName` | `String` | Profile name |
| `enabled` | `boolean` | Account enabled state |
| `createdAt`, `updatedAt` | `Instant` | Audit timestamps |

User roles are stored separately in `UserRole`, represented by the `user_roles` table. A user can have multiple roles, with `USER` as the default role.

Relationships: one user has multiple roles, one `UserPreferences`, and can have multiple `UserOrganizer` assignments and `Medication` records.

### UserPreferences

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `userId` | `Long` | Unique foreign key to `User` |
| `simplifiedMode` | `boolean` | Simplified UI preference |
| `textScale` | `Integer` | Text scaling preference |
| `highContrast` | `boolean` | High-contrast UI preference |
| `updatedAt` | `Instant` | Last update |

Timezone, locale, and reminder delivery preferences are not currently represented in the database design. They may be added if required by later application features.

Device-specific rendering and notification delivery remain client/OS responsibilities.

### OrganizerModel

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `productCode` | `String` | Unique model/product identifier |
| `name` | `String` | Human-readable name |
| `qrCodeValue` | `String` | Optional unique QR lookup value |
| `description` | `String` | Optional description |
| `daysCovered`, `compartmentsPerDay` | `Integer` | Model layout metadata |
| `active` | `boolean` | Available for new assignments |
| `createdAt` | `Instant` | Record creation timestamp |

QR codes identify a product/model or registered product identifier, not a user or their permissions.

### CompartmentDefinition

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `organizerModelId` | `Long` | Foreign key to `OrganizerModel` |
| `positionIndex` | `Integer` | Stable position within the model |
| `label` | `String` | Display label |
| `dayOfWeek` | `DayOfWeek` | Nullable for non-day-oriented models |
| `timeOfDay` | `TimeOfDay` | E.g. `MORNING`, `NOON`, `EVENING`; nullable if not applicable |

Constraint: unique `(organizerModelId, positionIndex)`.

### UserOrganizer

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `userId` | `Long` | Foreign key to `User` |
| `organizerModelId` | `Long` | Foreign key to `OrganizerModel` |
| `displayName` | `String` | Optional user-defined label |
| `active`, `isDefault` | `boolean` | Assignment and default selection state |
| `assignedAt` | `Instant` | Assignment time |

Enforce at most one active default organizer per user in the service layer. If individual physical units must be tracked later, add a separate instance/serial identifier.

### Medication

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `userId` | `Long` | Foreign key to `User` |
| `name` | `String` | User-entered name |
| `strengthValue` | `BigDecimal` | Optional numeric medication strength |
| `strengthUnit` | `String` | Optional strength unit |
| `form` | `MedicationForm` | E.g. `TABLET`, `CAPSULE`, `LIQUID`, `OTHER` |
| `doseAmount` | `BigDecimal` | Positive amount per scheduled occurrence |
| `doseUnit` | `String` | E.g. `TABLET`, `CAPSULE`, `ML` |
| `startDate` | `LocalDate` | Required start date |
| `endDate` | `LocalDate` | Optional end date |
| `status` | `MedicationStatus` | `ACTIVE`, `SUSPENDED`, `INACTIVE` |
| `createdAt`, `updatedAt` | `Instant` | Audit timestamps |

The current database design does not include a medication image reference. This can be introduced later if medication image storage becomes necessary.

This is a user-owned record, not a medication catalogue or prescribing system. Do not use floating-point types for dose quantities.

### MedicationSchedule

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `medicationId` | `Long` | Foreign key to `Medication` |
| `validFrom`, `validUntil` | `LocalDate` | Effective date range; end nullable |
| `createdAt` | `Instant` | Record creation timestamp |
| `replacedAt` | `Instant` | Timestamp when the schedule was replaced; nullable |

Schedule weekdays and times are stored in separate child tables:

- `ScheduleDay`: composite key of `scheduleId` and `dayOfWeek`.
- `ScheduleTime`: individual scheduled time, with an optional `timeOfDay` category.

A schedule can contain multiple weekdays and multiple times. Preserve previous schedule versions so historical occurrences remain explainable.

### DoseOccurrence

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `userId`, `medicationId`, `scheduleId` | `Long` | Owner and source references |
| `userOrganizerId` | `Long` | Optional foreign key to `UserOrganizer` |
| `compartmentDefinitionId` | `Long` | Optional intended compartment |
| `scheduledDate` | `LocalDate` | Local calendar date |
| `scheduledTime` | `LocalTime` | Planned local time |
| `status` | `DoseStatus` | `PENDING`, `TAKEN`, `SKIPPED`, `MISSED` |
| `doseAmountSnapshot` | `BigDecimal` | Planned amount snapshot |
| `doseUnitSnapshot` | `String` | Planned unit snapshot |
| `createdAt` | `Instant` | Creation time |
| `updatedAt` | `Instant` | Last update timestamp |

Prevent duplicate generation for the same schedule/date/time. Choose one idempotent occurrence-generation strategy. `MISSED` needs an explicitly defined grace-period rule.

### IntakeRecord

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `doseOccurrenceId` | `Long` | Foreign key to `DoseOccurrence` |
| `status` | `IntakeStatus` | `TAKEN` or `SKIPPED` |
| `occurredAt` | `Instant` | Nullable user-reported event time |
| `recordedAt` | `Instant` | Server-recorded timestamp |
| `note` | `String` | Optional note |

A dose occurrence can have multiple intake records. Treat these as user-reported events, not proof of ingestion. Define an application-level policy for duplicate submissions and corrections.

### PreparationSession

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `userId` | `Long` | Foreign key to owner |
| `userOrganizerId` | `Long` | Foreign key to assigned organizer |
| `periodStart`, `periodEnd` | `LocalDate` | Cycle/date range |
| `status` | `PreparationStatus` | `IN_PROGRESS`, `READY`, `CANCELLED` |
| `startedAt`, `completedAt` | `Instant` | Completion timestamp nullable until ready |

Completing a session must transactionally verify all required compartments have been checked.

### CompartmentPreparation

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `preparationSessionId` | `Long` | Foreign key to `PreparationSession` |
| `compartmentDefinitionId` | `Long` | Foreign key to model position |
| `status` | `CompartmentPreparationStatus` | `UNCHECKED`, `PREPARED`, `INTENTIONALLY_EMPTY` |
| `checkedAt` | `Instant` | Nullable until checked |
| `note` | `String` | Optional note |

Constraint: unique `(preparationSessionId, compartmentDefinitionId)`. An intentionally empty compartment is not the same as an unchecked compartment.

### ReminderSnooze (future, not MVP)

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `doseOccurrenceId` | `Long` | Foreign key to `DoseOccurrence` |
| `requestedAt` | `Instant` | Request timestamp |
| `remindAt` | `Instant` | Requested next reminder time |

Persist snoozes only if the backend needs cross-device consistency or history; otherwise the client may keep this state locally.

### Later-stage entities (not MVP)

- `VerificationPhoto`: owner/organizer or preparation reference, storage key, capture time, analysis status, and review outcome.
- `VerificationResult`: photo reference, detected compartment, result/confidence, and review status. It must not be treated as proof of medication ingestion.
- `CaregiverInvitation` / `CaregiverGrant`: inviter, invitee or invitation token metadata, recipient/user reference, permission scope, status, and timestamps. Design these only when Stage III access rules are approved.

## 3. Relationships and persistence rules

- `User` 1-N `UserRole`.
- `User` 1-1 `UserPreferences`.
- `User` 1-N `UserOrganizer` and `Medication`.
- `OrganizerModel` 1-N `CompartmentDefinition` and `UserOrganizer`.
- `Medication` 1-N `MedicationSchedule`.
- `MedicationSchedule` 1-N `ScheduleDay` and `ScheduleTime`.
- `MedicationSchedule` 1-N `DoseOccurrence`.
- `Medication` 1-N `DoseOccurrence`.
- `UserOrganizer` 1-N `DoseOccurrence` (optional association).
- `CompartmentDefinition` 1-N `DoseOccurrence` (optional association).
- `DoseOccurrence` 1-N `IntakeRecord`.
- `UserOrganizer` 1-N `PreparationSession`.
- `PreparationSession` 1-N `CompartmentPreparation`.

Use foreign keys, unique constraints, and transactions alongside application validation. Preserve historical dose occurrences and intake records when schedules or medication details change.

## 4. API contract

Base path: `/api`. Registration and login are public; other endpoints require a valid bearer JWT. All user-owned resources require ownership checks. Organizer-model catalog reads are public.

| Method | Route | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a user |
| `POST` | `/api/auth/login` | Authenticate and issue JWT |
| `GET`, `PATCH` | `/api/users/me` | Read/update profile |
| `GET`, `PUT` | `/api/users/me/preferences` | Read/replace preferences |
| `GET` | `/api/organizer-models` | List models; optional `qrCode` or `productCode` lookup |
| `GET` | `/api/organizer-models/{id}` | Read model and compartment layout |
| `GET`, `POST` | `/api/users/me/organizers` | List/assign organizers |
| `GET`, `PATCH`, `DELETE` | `/api/users/me/organizers/{id}` | Read/update/remove assignment |
| `GET`, `POST` | `/api/medications` | List/create medications |
| `GET`, `PATCH` | `/api/medications/{id}` | Read/update medication |
| `PATCH` | `/api/medications/{id}/status` | Change medication status |
| `PUT` | `/api/medications/{id}/schedule` | Replace complete schedule |
| `GET` | `/api/plan/daily?date=YYYY-MM-DD&organizerId={id}` | Read daily plan |
| `GET` | `/api/plan/weekly?weekStart=YYYY-MM-DD&organizerId={id}` | Read weekly plan |
| `GET` | `/api/dose-occurrences/{id}` | Read one occurrence |
| `POST` | `/api/dose-occurrences/{id}/intake` | Add intake record |
| `GET` | `/api/intake?from=YYYY-MM-DD&to=YYYY-MM-DD` | Query intake history |
| `POST` | `/api/users/me/organizers/{id}/preparations` | Start preparation |
| `GET` | `/api/users/me/organizers/{id}/preparations/current` | Read current preparation |
| `GET` | `/api/preparations/{id}` | Read preparation session |
| `PATCH` | `/api/preparations/{id}/compartments/{compartmentId}` | Update compartment status |
| `POST` | `/api/preparations/{id}/complete` | Complete after validation |

Here, organizer `{id}` refers to a `UserOrganizer` assignment. The compartment update must verify that the compartment belongs to that assignment's organizer model.

### Request/response examples

Examples use abbreviated IDs and fields for clarity.

**Register — `POST /api/auth/register`**

Request:
```json
{
  "email": "user@example.com",
  "password": "secure-password",
  "displayName": "Alex"
}
```

Response `201`:
```json
{
  "id": 1,
  "email": "user@example.com",
  "displayName": "Alex"
}
```

**Login — `POST /api/auth/login`**

Request:
```json
{
  "email": "user@example.com",
  "password": "secure-password"
}
```

Response `200`:
```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Create medication — `POST /api/medications`**

Request:
```json
{
  "name": "Example medication",
  "strengthValue": 500,
  "strengthUnit": "mg",
  "doseAmount": 1,
  "doseUnit": "TABLET",
  "startDate": "2026-10-30",
  "endDate": null
}
```

Response `201`:
```json
{
  "id": 12,
  "name": "Example medication",
  "status": "ACTIVE"
}
```

**Replace schedule — `PUT /api/medications/12/schedule`**

Request:
```json
{
  "validFrom": "2026-10-30",
  "validUntil": null,
  "days": [1, 3, 5],
  "times": [
    {
      "time": "08:00",
      "timeOfDay": "MORNING"
    },
    {
      "time": "20:00",
      "timeOfDay": "EVENING"
    }
  ]
}
```

Response `200`:
```json
{
  "id": 25,
  "medicationId": 12,
  "validFrom": "2026-10-30",
  "validUntil": null,
  "days": [1, 3, 5],
  "times": [
    {
      "time": "08:00",
      "timeOfDay": "MORNING"
    },
    {
      "time": "20:00",
      "timeOfDay": "EVENING"
    }
  ]
}
```

**Record intake — `POST /api/dose-occurrences/50/intake`**

Request:
```json
{
  "status": "TAKEN",
  "occurredAt": "2026-10-30T08:05:00Z",
  "note": null
}
```

Response `201`:
```json
{
  "id": 90,
  "doseOccurrenceId": 50,
  "status": "TAKEN",
  "occurredAt": "2026-10-30T08:05:00Z",
  "recordedAt": "2026-10-30T08:06:00Z"
}
```

**Update compartment — `PATCH /api/preparations/30/compartments/8`**

Request:
```json
{
  "status": "PREPARED",
  "note": null
}
```

Response `200`:
```json
{
  "id": 100,
  "compartmentDefinitionId": 8,
  "status": "PREPARED",
  "checkedAt": "2026-10-30T08:00:00Z"
}
```

### General rules

- Use JSON request/response bodies and ISO 8601 timestamps.
- Use `400` for invalid input, `401` for missing/invalid authentication, `403` for insufficient permissions, `404` for missing or inaccessible resources, and `409` for state/uniqueness conflicts.
- Use `201` for resource creation, `200` for successful reads/updates, and `204` when no response body is needed.
- Never expose password hashes or internal security details.
- Preserve historical occurrences and intake records when schedules or medications change.
- The current schema does not persist timezone or snooze state. Scheduling uses local dates/times; reminder snoozing is client-side in the MVP.
- Later-stage photo verification and caregiver endpoints are excluded.

## 5. Authentication and authorization

This section defines the required API security behavior; the README contains only general development principles.

- **Authentication:** use stateless bearer JWTs with Spring Security OAuth2 Resource Server. Registration (`POST /api/auth/register`) and login (`POST /api/auth/login`) are public; other API routes require a valid token unless explicitly designated otherwise.
- **Token signing:** sign JWTs with the RSA private key and validate signatures with the matching public key. Configure token expiry and reject expired, malformed, or incorrectly signed tokens.
- **Password and role handling:** store passwords as BCrypt hashes and never return hashes. Public registration assigns `USER` on the server; ignore or reject any caller-supplied role.
- **Ownership:** derive the current user ID from the authenticated principal, never from a request body or query parameter. Before reading or mutating a user-owned resource, verify that it belongs to that user. Knowing or guessing a resource ID must not grant access.
- **Response behavior:** return `401` for missing or invalid authentication. Use `403` for authenticated callers lacking permission; `404` may be used for inaccessible resources when hiding their existence is appropriate. Use `409` for state or uniqueness conflicts.
- **Future delegation:** caregiver access is excluded from the MVP. If added in Stage III, authorize it through explicit, scoped grants rather than treating caregivers as the resource owner.

Organizer-model catalog records are shared reference data; `UserOrganizer` is the user-owned assignment. This distinction must be reflected in authorization checks.

## 6. Scheduling and state rules

- A `MedicationSchedule` is a recurring rule; a `DoseOccurrence` is a date-specific planned dose. Schedule changes must not rewrite past occurrences or intake records.
- The MVP does not persist a user timezone. Schedule dates and times are interpreted as local calendar values. Store an IANA timezone (e.g. `Europe/Warsaw`) in `UserPreferences` before implementing timezone-aware scheduling and daylight-saving handling.
- Define daylight-saving gap/overlap behavior before implementing occurrence generation.
- The mobile OS/client handles notifications and snoozing in the MVP. Persistent backend snooze state is deferred.
- Keep medication state (`ACTIVE`, `SUSPENDED`, `INACTIVE`), dose state (`PENDING`, `TAKEN`, `SKIPPED`, `MISSED`), preparation state, and any future image-verification state separate.
- Validate positive dose amounts, date ranges, valid schedule weekdays/times, and preparation completeness.

## 7. Errors and testing

Use Jakarta Bean Validation for request DTOs and service-level validation for domain rules. Return a consistent error payload containing timestamp, HTTP status, error/code, safe message, and request path. Do not expose stack traces or internal exception details.

Suggested status codes: `400` invalid input, `401` missing/invalid authentication, `403` insufficient permission, `404` missing or intentionally undisclosed foreign resource, `409` state/uniqueness conflict, and `500` unexpected failure.

Test at minimum:

- Schedule validation and idempotent occurrence generation.
- Intake duplication/correction policy and dose-state transitions.
- Preparation completion with unchecked compartments.
- Ownership isolation for every resource family.
- JWT signature, expiry, and malformed/missing tokens.
- MySQL persistence constraints using Testcontainers.
- Schedule date/time boundaries and, when timezone support is implemented, daylight-saving transitions.
