# Engineering Rules

These rules make the backend safer for humans and AI agents to change.

## Layer boundaries

- If behavior is HTTP shape, keep it in `controller/*`.
- If behavior is application/business logic, keep it in `service/*`.
- If behavior is persistence access, keep it in `repository/*`.
- If behavior is inbound API shape, keep it in `request/*`.
- If behavior is outbound API shape, keep it in `dto/*`.
- If behavior is entity-to-DTO conversion, keep it in `mapper/*`.
- If behavior is JWT/Spring Security/auth user resolution, keep it in `security/*`.
- If behavior is pure reusable normalization, keep it in `util/*`.
- Do not place reusable utilities inside services or controllers.

## API changes

For any endpoint, request, response, enum, auth, progress, or error behavior change:

1. Update backend controller/service/DTO/request/enum.
2. Update or add backend tests at the service/security/repository seam.
3. Update `FRONTEND_API.md`.
4. Update frontend `D:\toeic-vocab-app\config\api.ts` if paths changed.
5. Update frontend `D:\toeic-vocab-app\types\*` if DTOs/enums changed.
6. Update frontend `D:\toeic-vocab-app\services\*` if payloads or params changed.
7. Update frontend hooks/cache if mutation side effects changed.
8. Update both repos' `docs/ai/api-contract.md` if the durable contract changed.

## Progress flow

- Guest progress must stay stateless unless an ADR explicitly changes that decision.
- `POST /public/progress` resolves guest progress and imports guest progress into an authenticated user when bearer auth and a guest token are present.
- `submitAnswer` and `restartUnit` responses must include `progress` so the frontend can persist the latest `progressToken`.
- Completion payloads must include enough data for the frontend completion modal without requiring extra ad hoc calls.

## Auth flow

- JWT is the access token used by the frontend.
- `AuthTokenFilter`, `JwtUtils`, `BearerTokenResolver`, and `CurrentUserProvider` are the security seam.
- Controllers should not parse bearer tokens except through dedicated security helpers.
- Admin endpoints require role-based authorization through Spring Security configuration.

## Database changes

- Schema changes require Flyway migrations under `src/main/resources/db/migration/`.
- Do not mutate an existing migration that may have been applied locally unless the user explicitly asks for a local reset.
- If a migration changes API-visible data shape, update DTOs, frontend types, and contract docs.

## Testing and verification

- Prefer service tests for business rules.
- Prefer repository tests for query behavior.
- Prefer security tests for JWT/filter behavior.
- API contract changes should include at least one test or explicit manual verification path.

