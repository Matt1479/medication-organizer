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
| `passwordHash` | `String` | BCrypt hash; never returned by the API |
| `displayName` | `String` | Profile name |
| `role` | `UserRole` | MVP: `USER` |
| `enabled` | `boolean` | Account enabled state |
| `createdAt`, `updatedAt` | `Instant` | Audit timestamps |

Relationships: one user has one `UserPreferences`, and can have multiple `UserOrganizer` assignments and `Medication` records.

### UserPreferences

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `userId` | `Long` | Unique foreign key to `User` |
| `timeZone` | `String` | IANA zone ID, e.g. `Europe/Warsaw` |
| `locale` | `String` | BCP 47 tag, e.g. `pl-PL` |
| `simplifiedMode` | `boolean` | Simplified UI preference |
| `textScale` | `Integer` | Optional frontend-interpreted preference |
| `highContrast` | `boolean` | Contrast preference |
| `reminderSoundEnabled`, `vibrationEnabled`, `spokenPromptsEnabled` | `boolean` | Reminder/accessibility preferences |
| `updatedAt` | `Instant` | Last update |

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

Constraint: unique `(organizerModelId, positionIndex)`. Do not hard-code a 7 × 3 layout for every organizer model.

### UserOrganizer

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `userId` | `Long` | Foreign key to `User` |
| `organizerModelId` | `Long` | Foreign key to `OrganizerModel` |
| `displayName` | `String` | Optional user-defined label |
| `active`, `isDefault` | `boolean` | Assignment and default selection state |
| `assignedAt` | `Instant` | Assignment time |

Enforce a single default organizer per user. If individual physical units must be tracked later, add a separate instance/serial identifier.

### Medication

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `userId` | `Long` | Foreign key to `User` |
| `name` | `String` | User-entered name |
| `strength` | `String` | Optional descriptive strength |
| `form` | `MedicationForm` | E.g. `TABLET`, `CAPSULE`, `LIQUID`, `OTHER` |
| `doseAmount` | `BigDecimal` | Positive amount per scheduled occurrence |
| `doseUnit` | `String` | E.g. `TABLET`, `CAPSULE`, `ML` |
| `imageReference` | `String` | Optional storage key/URL |
| `startDate`, `endDate` | `LocalDate` | End date nullable |
| `status` | `MedicationStatus` | `ACTIVE`, `SUSPENDED`, `INACTIVE` |
| `createdAt`, `updatedAt` | `Instant` | Audit timestamps |

This is a user-owned record, not a medication catalogue or prescribing system. Do not use floating-point types for dose quantities.

### MedicationSchedule

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `medicationId` | `Long` | Foreign key to `Medication` |
| `validFrom`, `validUntil` | `LocalDate` | Effective date range; end nullable |
| `daysOfWeek` | `Set<DayOfWeek>` or child rows | Selected weekdays |
| `times` | `List<LocalTime>` or child rows | One or more daily times |
| `createdAt`, `supersededAt` | `Instant` | Version/audit timestamps; `supersededAt` nullable |

Prefer normalized child tables for schedule days and times. Preserve schedule versions or snapshot enough data to explain historical occurrences.

### DoseOccurrence

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `userId`, `medicationId`, `scheduleId` | `Long` | Owner and source references |
| `organizerId` | `Long` | Optional foreign key to `UserOrganizer` |
| `compartmentDefinitionId` | `Long` | Optional intended compartment |
| `scheduledDate` | `LocalDate` | Local calendar date |
| `scheduledTime` | `LocalTime` | Planned local time |
| `status` | `DoseStatus` | `PENDING`, `TAKEN`, `SKIPPED`, `MISSED` |
| `doseAmountSnapshot` | `BigDecimal` | Planned amount snapshot |
| `doseUnitSnapshot` | `String` | Planned unit snapshot |
| `createdAt` | `Instant` | Creation time |

Prevent duplicate generation for the same schedule/date/time. Choose one idempotent occurrence-generation strategy. `MISSED` needs an explicitly defined grace-period rule.

### IntakeRecord

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `doseOccurrenceId` | `Long` | Foreign key to `DoseOccurrence` |
| `status` | `IntakeStatus` | `TAKEN` or `SKIPPED` |
| `occurredAt`, `recordedAt` | `Instant` | User-reported and server-recorded times |
| `note` | `String` | Optional, length-limited note |

Treat intake as user-reported information, not proof of ingestion. Prevent accidental duplicate submissions and avoid silently overwriting historical confirmations.

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

### ReminderSnooze (optional for MVP)

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

- `User` 1-1 `UserPreferences`.
- `User` 1-N `UserOrganizer` and `Medication`.
- `OrganizerModel` 1-N `CompartmentDefinition` and `UserOrganizer`.
- `Medication` 1-N `MedicationSchedule` and `DoseOccurrence`.
- `MedicationSchedule` 1-N `DoseOccurrence`.
- `DoseOccurrence` 1-N or 1-1 `IntakeRecord`, depending on whether corrections are represented as append-only history; choose one explicit policy.
- `UserOrganizer` 1-N `PreparationSession`; `PreparationSession` 1-N `CompartmentPreparation`.

Use foreign keys, unique constraints, and transactions alongside application validation. Prefer soft deactivation when historical records depend on a medication or assignment. Do not cascade-delete intake/history records as a side effect of ordinary edits.

## 4. API contract

Base path: `/api`. This is the proposed MVP contract; catalog read access should be configured consistently as public or authenticated. All user-owned resources require ownership checks.

| Method | Route | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a user |
| `POST` | `/api/auth/login` | Authenticate and issue JWT |
| `GET`, `PATCH` | `/api/users/me` | Read/update current profile |
| `GET`, `PUT` | `/api/users/me/preferences` | Read/replace preferences |
| `GET` | `/api/organizer-models` | List supported models; optional `qrCode` query for lookup |
| `GET` | `/api/organizer-models/{id}` | Read model and compartment layout |
| `GET`, `POST` | `/api/users/me/organizers` | List/assign organizers |
| `PATCH`, `DELETE` | `/api/users/me/organizers/{id}` | Update/remove an assignment |
| `POST`, `GET` | `/api/medications` | Create/list medications |
| `GET`, `PUT`, `DELETE` | `/api/medications/{id}` | Read/update/deactivate a medication |
| `PATCH` | `/api/medications/{id}/suspension` | Suspend/resume a medication |
| `PUT` | `/api/medications/{id}/schedule` | Replace schedule rules |
| `GET` | `/api/plan/daily?date=YYYY-MM-DD&organizerId=...` | Read daily plan |
| `GET` | `/api/plan/weekly?weekStart=YYYY-MM-DD&organizerId=...` | Read weekly plan |
| `GET` | `/api/dose-occurrences/{id}` | Read one planned occurrence |
| `POST` | `/api/dose-occurrences/{id}/intake` | Record taken/skipped outcome |
| `POST` | `/api/dose-occurrences/{id}/snooze` | Request a reminder snooze |
| `GET` | `/api/intake?from=...&to=...` | Query intake history |
| `POST` | `/api/organizers/{id}/preparations` | Start preparation session |
| `GET` | `/api/organizers/{id}/preparations/current` | Read current progress |
| `GET` | `/api/preparations/{id}` | Read preparation session |
| `PATCH` | `/api/preparations/{id}/compartments/{compartmentId}` | Update compartment status |
| `POST` | `/api/preparations/{id}/complete` | Complete after validation |

Later-stage photo and caregiver endpoints are deliberately excluded from the MVP contract.

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
- Store a user's IANA timezone (e.g. `Europe/Warsaw`); schedules use local dates/times, while actual event timestamps use `Instant`.
- Define daylight-saving gap/overlap behavior before implementing occurrence generation.
- The mobile OS/client displays local notifications in the MVP. The API can persist snooze requests but cannot guarantee notification delivery while the client is offline or notifications are disabled.
- Keep medication state (`ACTIVE`, `SUSPENDED`, `INACTIVE`), dose state (`PENDING`, `TAKEN`, `SKIPPED`, `MISSED`), preparation state, and any future image-verification state separate.
- Validate positive dose amounts, date ranges, valid schedule weekdays/times, future/allowed snooze times, and preparation completeness.

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
- User timezone and daylight-saving boundaries.
