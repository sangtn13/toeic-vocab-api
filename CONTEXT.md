# TOEIC Vocabulary API Context

This file is the compact backend memory for agents. Keep it current and small.

## Product

This repo is the Spring Boot backend for VocaSa, a TOEIC vocabulary learning app. The paired frontend lives at `D:\toeic-vocab-app`.

The backend supports:

- Public guest learning flow.
- Authenticated learning flow with JWT.
- Admin catalog management for study sets, units, and vocabulary.
- PostgreSQL persistence with Flyway migrations.
- Frontend-facing REST contract documented in `FRONTEND_API.md`.

## Domain glossary

- Study set: a vocabulary collection, publicly addressed by `slug` and internally by UUID.
- Study unit: an ordered group inside a study set.
- Vocabulary: one word or phrase in a unit.
- Practice mode: one of `GUESS_WORD`, `FLASHCARD`, `MULTIPLE_CHOICE`, `REVERSE_MULTIPLE_CHOICE`.
- Study activity: payload used by the frontend to render one learning mode for one unit.
- Study progress: learner progress. Guests use stateless `progressToken`; authenticated users persist progress by `user_id`.
- Progress token: signed/stateless guest progress token. It can refresh after progress mutations and must be returned to the frontend.
- Unit completion: payload shown when a unit is complete, including next unit and vocabulary review data.
- Admin catalog: admin CRUD for study sets, units, and vocabulary.
- App user: authenticated user with role `ADMIN` or `USER`.

## Backend architecture

- `controller/`: REST endpoints and HTTP response shape.
- `request/`: inbound request bodies.
- `dto/`: outbound DTOs exposed to the frontend.
- `response/`: shared response envelopes such as `ApiResponse` and `PagedResponse`.
- `service/`: application use cases and business rules.
- `repository/`: Spring Data persistence interfaces and queries.
- `model/`: JPA entities.
- `mapper/`: MapStruct/manual mapping between models and DTOs.
- `enums/`: API-visible enum names.
- `security/`: JWT, Spring Security config, filters, auth helpers.
- `config/`: application configuration and bootstrap helpers.
- `util/`: reusable pure helpers.
- `src/main/resources/db/migration/`: Flyway migrations.
- `src/test/java/`: tests for service, repository, security, and application wiring.

## Frontend relationship

Frontend path: `D:\toeic-vocab-app`.

Important frontend files:

- `docs/ai/api-contract.md`: frontend contract snapshot.
- `config/api.ts`: endpoint builders.
- `lib/axios.ts`: API client, auth header, error normalization.
- `services/*`: frontend HTTP calls.
- `types/*`: DTOs/enums mirrored from backend.
- `hooks/*`: React Query, mutation, cache, and bootstrap behavior.

The frontend calls same-origin `/api/v1/*`; Next.js rewrites/proxies to this backend through `NEXT_PUBLIC_API_URL`.

## Durable invariants

- All frontend-facing responses are wrapped as `ApiResponse<T>` with `success`, `message`, `data`, and `timestamp`.
- Paginated payloads use `PagedResponse<T>` with `items`, `page`, `size`, `totalElements`, `totalPages`, and `last`.
- All API-visible IDs are UUID values.
- Public study endpoints are open to guests.
- Admin endpoints require a valid JWT for an admin user.
- JWT is stateless; logout is frontend-local plus backend token resolution behavior.
- Guest progress is stateless and must not require a database row.
- Authenticated progress is persisted by user.
- Login/register responses return `accessToken`, `expiresAt`, and `user`.
- Changing enum names, DTO field names, endpoint paths, or response wrappers is a frontend contract change.

## Read next by task

- API path or DTO change: `docs/ai/api-contract.md`, `FRONTEND_API.md`, relevant controller/request/dto/enum, frontend `config/api.ts`, `types/*`, `services/*`.
- Public study change: `controller/study/PublicStudyController.java`, `service/study/*`, `dto/study/*`, `request/study/*`, tests in `service/study`.
- Auth change: `controller/auth/AuthController.java`, `service/auth/*`, `security/**`, `dto/auth/*`, `request/auth/*`, auth tests.
- Admin change: `controller/admin/AdminCatalogController.java`, `service/admin/*`, `dto/admin/*`, `request/admin/*`, admin tests.
- Persistence change: `model/**`, `repository/**`, `db/migration/**`, repository tests.
- Security change: `security/**`, `config/SecurityConfig.java`, security tests, frontend auth hooks if contract changes.
- Architecture or rule change: update this file, `AGENTS.md`, and possibly an ADR in `docs/adr/`.

