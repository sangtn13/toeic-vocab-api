# Module Map

Use this file to find the smallest safe backend context slice.

## Backend ownership

| Area | Owner files | Rules |
| --- | --- | --- |
| Endpoints | `src/main/java/com/toeic/vocab/controller/**` | HTTP shape, validation entry point, status codes, `ApiResponse` wrapping. |
| Requests | `src/main/java/com/toeic/vocab/request/**` | Inbound payloads. Changing fields is a contract change. |
| DTOs | `src/main/java/com/toeic/vocab/dto/**` | Outbound payloads mirrored by frontend `types/*`. |
| Response envelopes | `src/main/java/com/toeic/vocab/response/**` | Shared wrappers. Changing these affects every frontend service. |
| Services | `src/main/java/com/toeic/vocab/service/**` | Use cases, business rules, progress/auth/admin behavior. |
| Repositories | `src/main/java/com/toeic/vocab/repository/**` | Persistence queries only. |
| Models | `src/main/java/com/toeic/vocab/model/**` | JPA entities and persistence structure. |
| Mappers | `src/main/java/com/toeic/vocab/mapper/**` | Entity-to-DTO conversion. |
| Enums | `src/main/java/com/toeic/vocab/enums/**` | API-visible enum values. |
| Security | `src/main/java/com/toeic/vocab/security/**` | JWT, filters, auth user details, token resolution. |
| Config | `src/main/java/com/toeic/vocab/config/**` | Spring/OpenAPI/JPA/bootstrap configuration. |
| Utilities | `src/main/java/com/toeic/vocab/util/**` | Reusable pure helpers. |
| Migrations | `src/main/resources/db/migration/**` | Database schema/data changes. |
| Tests | `src/test/java/com/toeic/vocab/**` | Regression coverage at service/repository/security seams. |

## Frontend ownership

Frontend repo: `D:\toeic-vocab-app`

| Area | Frontend files |
| --- | --- |
| Endpoint builders | `config/api.ts` |
| HTTP adapter | `lib/axios.ts` |
| Services | `services/**` |
| DTOs | `types/**` |
| Study hooks/cache | `hooks/use-study.ts`, `hooks/study-cache.ts` |
| Auth hooks/store | `hooks/use-auth.ts`, `store/auth-store.ts` |
| Progress store | `store/progress-store.ts` |
| Admin hooks | `hooks/use-admin-catalog.ts` |

## Task slices

Public study:

- Backend: `controller/study/PublicStudyController.java`, `service/study/**`, `dto/study/**`, `request/study/**`, `enums/PracticeMode.java`.
- Frontend: `services/public-study.service.ts`, `hooks/use-study.ts`, `hooks/study-cache.ts`, `types/study.ts`.

Admin catalog:

- Backend: `controller/admin/AdminCatalogController.java`, `service/admin/**`, `dto/admin/**`, `request/admin/**`.
- Frontend: `services/admin-catalog.service.ts`, `hooks/use-admin-catalog.ts`, `types/admin.ts`.

Auth:

- Backend: `controller/auth/AuthController.java`, `service/auth/**`, `security/**`, `dto/auth/**`, `request/auth/**`.
- Frontend: `services/auth.service.ts`, `hooks/use-auth.ts`, `store/auth-store.ts`, `types/auth.ts`.

Persistence:

- Backend: `model/**`, `repository/**`, `src/main/resources/db/migration/**`, repository tests.
- Frontend only changes if API-visible fields or behavior change.

API contract:

- Start with `docs/ai/api-contract.md` and `FRONTEND_API.md`.
- Then compare backend controller/request/DTO/enum/response wrapper with frontend `config/api.ts`, `types/*`, `services/*`, and relevant hooks/cache.

