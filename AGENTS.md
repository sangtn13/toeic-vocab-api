# TOEIC Vocabulary API Agent Guide

This is the required entry point for AI work in this backend repo. Read it before editing.

## Read protocol

Use the smallest context slice that can safely answer the task:

1. Read `AGENTS.md` and `CONTEXT.md`.
2. Read `docs/ai/module-map.md` to find the owning backend layer before opening code.
3. For API, DTO, auth, progress, admin, or frontend-facing changes, read `docs/ai/api-contract.md`, `FRONTEND_API.md`, and the frontend contract docs at `D:\toeic-vocab-app\docs\ai\api-contract.md`.
4. For non-trivial features or refactors, read `docs/ai/spec-driven-workflow.md` and create/update a spec under `docs/specs/`.
5. Only then inspect the exact controller/service/dto/request/entity/test files listed by the map or spec.

Do not read the whole project by default. Start from the map, then drill down.

## Hard rules

- Controllers define HTTP shape only. Business logic belongs in `service/*`.
- Request payloads belong in `request/*`; response payloads belong in `dto/*`; response wrappers belong in `response/*`.
- DTO conversion belongs in `mapper/*`. Do not hand-map the same DTO shape repeatedly in controllers.
- Persistence access belongs in `repository/*`. Services may coordinate repositories, but controllers must not.
- Domain models belong in `model/*`; enums belong in `enums/*`; reusable pure helpers belong in `util/*`.
- Auth and JWT behavior belongs in `security/*` plus `service/auth/*`. Do not duplicate token parsing in controllers/services.
- Public study logic belongs in `service/study/*`; admin catalog logic belongs in `service/admin/*`.
- All frontend-facing endpoints must return `ApiResponse<T>` and paginated endpoints must return `PagedResponse<T>` inside `ApiResponse`.
- All domain IDs exposed to the API are UUID values and must remain UUID strings on the frontend.
- If a controller path, request, DTO, enum, error shape, or auth behavior changes, update `FRONTEND_API.md`, frontend `types/*`, frontend `services/*`, and relevant frontend hooks/cache in the same change.
- Guest progress is stateless token based. Any mutation that can refresh progress must return the latest `progress.progressToken` for the frontend to persist.
- Keep tests near the durable seam: service tests for business logic, repository tests for persistence behavior, security tests for auth/JWT behavior.
- Keep docs updated when a change alters domain language, API contract, module ownership, or a durable architecture rule.

## Verification

Before finishing backend code changes, run the narrowest useful checks:

- Backend unit/integration checks: `.\mvnw.cmd test`.
- Build-sensitive backend work: `.\mvnw.cmd verify` when appropriate.
- API contract touched: manually compare `FRONTEND_API.md`, backend controllers/DTOs/requests/enums, frontend `D:\toeic-vocab-app\types\*`, frontend `D:\toeic-vocab-app\services\*`, and frontend `D:\toeic-vocab-app\docs\ai\api-contract.md`.
- Frontend behavior touched: run frontend `npm run lint` from `D:\toeic-vocab-app` when feasible.

If a check cannot run, report why and what risk remains.

## Backend layer rules

- `controller/*`: endpoint paths, status codes, validation annotations, `ApiResponse` wrapping.
- `service/*`: use cases and transaction/business decisions.
- `repository/*`: database queries only.
- `mapper/*`: DTO conversion only.
- `request/*`: inbound API payload types.
- `dto/*`: outbound API payload types.
- `model/*`: JPA entities.
- `enums/*`: API-visible enum names; changing these is a contract change.
- `security/*`: JWT, filters, user details, auth entry points, access denied handling.
- `util/*`: reusable pure helpers such as slug/text/time normalization.

## Agent skills

### Issue tracker

Work is tracked in GitHub Issues for `https://github.com/sangtn13/toeic-vocab-api`. See `docs/agents/issue-tracker.md`.

### Triage labels

Use the default triage vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

This repo uses a single backend context plus an external frontend contract reference. See `docs/agents/domain.md`.

