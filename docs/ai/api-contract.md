# API Contract

Backend source of truth for frontend integration: `FRONTEND_API.md`.

Frontend paired snapshot: `D:\toeic-vocab-app\docs\ai\api-contract.md`.

If this file disagrees with controllers or `FRONTEND_API.md`, treat controllers plus `FRONTEND_API.md` as source of truth and update this file.

## Base

- API prefix: `/api/v1`
- Response wrapper: `ApiResponse<T>`
- Pagination wrapper: `PagedResponse<T>`
- IDs: UUID values serialized as strings
- Frontend browser calls: same-origin `/api/v1/*`

## Response envelopes

`ApiResponse<T>` fields:

- `success`
- `message`
- `data`
- `timestamp`

`PagedResponse<T>` fields:

- `items`
- `page`
- `size`
- `totalElements`
- `totalPages`
- `last`

## Auth

| Method | Path | Backend owner | Frontend owner |
| --- | --- | --- | --- |
| POST | `/auth/register` | `AuthController.register` | `authService.register` |
| POST | `/auth/login` | `AuthController.login` | `authService.login` |
| GET | `/auth/me` | `AuthController.getCurrentUser` | `authService.me` |
| POST | `/auth/logout` | `AuthController.logout` | `authService.logout` |

Auth responses include `accessToken`, `expiresAt`, and `user`.

## Public study

| Method | Path | Backend owner | Frontend owner |
| --- | --- | --- | --- |
| POST | `/public/progress` | `PublicStudyController.resolveProgress` | `publicStudyService.resolveStudyProgress` |
| GET | `/public/study-sets` | `PublicStudyController.getStudySets` | `publicStudyService.getStudySets` |
| GET | `/public/study-sets/{slug}` | `PublicStudyController.getStudySetDetail` | `publicStudyService.getStudySetDetail` |
| GET | `/public/study-sets/{slug}/units` | `PublicStudyController.getStudySetUnits` | `publicStudyService.getStudySetUnits` |
| GET | `/public/study-sets/{slug}/units/{unitId}/activities/{mode}` | `PublicStudyController.getStudyActivity` | `publicStudyService.getStudyActivity` |
| POST | `/public/progress/{progressToken}/answers` | `PublicStudyController.submitAnswer` | `publicStudyService.submitAnswer` |
| POST | `/public/progress/{progressToken}/study-sets/{slug}/units/{unitId}/restart` | `PublicStudyController.restartUnit` | `publicStudyService.restartUnit` |

Public-study payload ownership:

- `POST /public/progress` returns `created` plus `progress`, where `progress` contains only `progressToken`, `displayName`, and `persistent`.
- `GET /public/study-sets` returns list rows with `id`, `title`, `slug`, `description`, `learningStatus`, `totalUnits`, and `totalWords`.
- `GET /public/study-sets/{slug}` returns study-set metadata plus aggregate progress only: `title`, `description`, and `progress`.
- `GET /public/study-sets/{slug}/units` owns paginated unit rows and unit progress/status.
- `GET /public/study-sets/{slug}/units/{unitId}/activities/{mode}` returns `mode`, `studySetTitle`, `unitTitle`, `studySetProgress`, `unitProgress`, and `items`.
- `POST /public/progress/{progressToken}/answers` returns `vocabularyId`, `practiceMode`, `correct`, `correctAnswer`, `unitCompleted`, `studySetProgress`, `unitProgress`, `progress`, optional `studyActivity`, and optional `unitCompletion`.
- `POST /public/progress/{progressToken}/study-sets/{slug}/units/{unitId}/restart` returns `unitProgress`, `studySetProgress`, and `progress`.
- `unitCompletion.vocabularies` is a slim review list with `vocabularyId`, `word`, and `meaning` only.

Practice modes:

- `GUESS_WORD`
- `FLASHCARD`
- `MULTIPLE_CHOICE`
- `REVERSE_MULTIPLE_CHOICE`

Progress invariant: responses from progress mutations must expose `progress.progressToken` for frontend persistence.

Submit-answer invariant: when `correct = true` and `unitCompleted = false`, `/public/progress/{progressToken}/answers` embeds `studyActivity` for the next in-unit state so the frontend does not immediately refetch `/activities` after the refreshed progress token.

Submit-answer invariant: when `unitCompleted = true`, `/public/progress/{progressToken}/answers` embeds `unitCompletion`; there is no dedicated public `/completion` endpoint anymore.

## Admin catalog

| Method | Path | Backend owner | Frontend owner |
| --- | --- | --- | --- |
| GET | `/admin/study-sets` | `AdminCatalogController.getStudySets` | `adminCatalogService.getStudySets` |
| GET | `/admin/study-sets/{studySetId}` | `AdminCatalogController.getStudySet` | `adminCatalogService.getStudySet` |
| POST | `/admin/study-sets` | `AdminCatalogController.createStudySet` | `adminCatalogService.createStudySet` |
| PUT | `/admin/study-sets/{studySetId}` | `AdminCatalogController.updateStudySet` | `adminCatalogService.updateStudySet` |
| DELETE | `/admin/study-sets/{studySetId}` | `AdminCatalogController.deleteStudySet` | `adminCatalogService.deleteStudySet` |
| GET | `/admin/study-sets/{studySetId}/units` | `AdminCatalogController.getUnits` | `adminCatalogService.getUnits` |
| POST | `/admin/study-sets/{studySetId}/units` | `AdminCatalogController.createUnit` | `adminCatalogService.createUnit` |
| PUT | `/admin/units/{unitId}` | `AdminCatalogController.updateUnit` | `adminCatalogService.updateUnit` |
| DELETE | `/admin/units/{unitId}` | `AdminCatalogController.deleteUnit` | `adminCatalogService.deleteUnit` |
| GET | `/admin/units/{unitId}/vocabularies` | `AdminCatalogController.getVocabularies` | `adminCatalogService.getVocabularies` |
| POST | `/admin/units/{unitId}/vocabularies` | `AdminCatalogController.createVocabulary` | `adminCatalogService.createVocabulary` |
| PUT | `/admin/vocabularies/{vocabularyId}` | `AdminCatalogController.updateVocabulary` | `adminCatalogService.updateVocabulary` |
| DELETE | `/admin/vocabularies/{vocabularyId}` | `AdminCatalogController.deleteVocabulary` | `adminCatalogService.deleteVocabulary` |

Admin invariant: endpoints require a JWT for an admin user.