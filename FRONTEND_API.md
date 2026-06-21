# TOEIC Vocabulary API Contract

Base URL: `/api/v1`

Luu y:

- Tat ca `studySetId`, `unitId`, `vocabularyId`, `user.id` la chuoi `UUID`.

## 1. Public learning flow

### Tao hoac resolve progress

`POST /public/progress`

Request body:

```json
{
  "displayName": "Guest",
  "progressToken": "existing-progress-token-if-any",
  "clientKey": "browser-stable-key"
}
```

Backend xu ly theo thu tu:

- Neu dang la guest:
  - neu co `progressToken` stateless hop le thi refresh lai dung progress do
- neu khong co token thi tao progress stateless, khong ghi DB
- neu user da dang nhap:
  - neu gui `progressToken` guest hien tai thi backend import tien do do sang progress cua user
  - du lieu that duoc luu theo `user_id`

Response tra ve:

- `created = true`: progress vua duoc khoi tao
- `created = false`: backend da resolve progress hien tai
- `progress` chi gom `progressToken`, `displayName`, `persistent`
- `progress.persistent = false`: guest stateless, FE phai tu luu progress token hien tai
- `progress.persistent = true`: tien do dang gan truc tiep voi user dang nhap

### Lay danh sach bo tu public

`GET /public/study-sets?progressToken={token}&page=0&size=10`

Response `data` la `PagedResponse`:

- `items`: danh sach study set cua page hien tai
- `page`, `size`, `totalElements`, `totalPages`, `last`

Moi study set row trong `items` gom:

- `id`, `title`, `slug`, `description`
- `learningStatus`: `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`
- `totalUnits`, `totalWords`

### Lay chi tiet bo tu + tien do tong

`GET /public/study-sets/{slug}?progressToken={token}`

Response chi gom:

- `title`
- `description`
- `progress`

Danh sach unit va trang thai tung unit duoc lay tu endpoint rieng:

`GET /public/study-sets/{slug}/units?progressToken={token}&page=0&size=10`

Response `data` la `PagedResponse`, moi `item` la 1 `StudyUnitProgressDto` gom:

- `id`, `title`, `unitOrder`
- `totalWords`, `learnedWords`, `masteredWords`, `percentage`
- `status`: `AVAILABLE`, `IN_PROGRESS`, `COMPLETED`

### Lay du lieu hoc cho 1 unit

`GET /public/study-sets/{slug}/units/{unitId}/activities/{mode}?progressToken={token}`

`mode` ho tro:

- `GUESS_WORD`
- `FLASHCARD`
- `MULTIPLE_CHOICE`
- `REVERSE_MULTIPLE_CHOICE`

Response gom:

- `mode`
- `studySetTitle`
- `unitTitle`
- `studySetProgress`
- `unitProgress`
- `items`

Moi `item` phuc vu public-study flow gom:

- `vocabularyId`, `mastered`
- `word`, `meaning`, `definition`
- `exampleSentence`, `exampleSentenceMasked`, `exampleTranslation`
- `phoneticUs`, `phoneticUk`
- `pronunciationUsUrl`, `pronunciationUkUrl`
- `hint`, `partOfSpeech`
- `choices`

### Nop dap an

`POST /public/progress/{progressToken}/answers`

Request body:

```json
{
  "vocabularyId": "77777777-7777-7777-7777-777777777777",
  "practiceMode": "GUESS_WORD",
  "answer": "abide by"
}
```

Response tra ve:

- `vocabularyId`
- `practiceMode`
- `correct`
- `correctAnswer`
- `unitCompleted`
- `studySetProgress`
- `unitProgress`
- `progress`: FE phai luu lai `progress.progressToken` sau moi lan submit neu la guest
- `studyActivity`: chi co khi `correct = true` va `unitCompleted = false`
- `unitCompletion`: chi co khi `unitCompleted = true`

`studyActivity` duoc embed de FE chuyen sang trang thai cau tiep theo trong cung unit ma khong can goi them request `activities` ngay lap tuc.

`unitCompletion` duoc embed de FE mo popup hoan thanh unit ma khong can goi request completion rieng.

`unitCompletion` gom:

- `unitProgress`
- `studySetProgress`
- `nextUnit`
- `vocabularies`

Moi word-review row trong `unitCompletion.vocabularies` chi gom:

- `vocabularyId`, `word`, `meaning`

Khong con dedicated public `/completion` endpoint trong contract nay.

### Hoc lai tu dau 1 unit

`POST /public/progress/{progressToken}/study-sets/{slug}/units/{unitId}/restart`

Response chi gom:

- `unitProgress`
- `studySetProgress`
- `progress`

## 2. Admin catalog flow

Tat ca endpoint `admin` yeu cau header JWT:

`Authorization: Bearer {accessToken}`

### Study set

- `GET /admin/study-sets`
- `GET /admin/study-sets/{studySetId}`
- `POST /admin/study-sets`
- `PUT /admin/study-sets/{studySetId}`
- `DELETE /admin/study-sets/{studySetId}`

Luu y:

- FE khong can gui `slug` khi tao/sua study set.
- Backend tu generate `slug` tu `title` va tra lai slug trong response.
- Neu `title` bi trung va slug goc da ton tai, backend tu them hau to nhu `-2`, `-3` de dam bao duy nhat.

### Unit

- `GET /admin/study-sets/{studySetId}/units`
- `POST /admin/study-sets/{studySetId}/units`
- `PUT /admin/units/{unitId}`
- `DELETE /admin/units/{unitId}`

### Vocabulary

- `GET /admin/units/{unitId}/vocabularies`
- `POST /admin/units/{unitId}/vocabularies`
- `PUT /admin/vocabularies/{vocabularyId}`
- `DELETE /admin/vocabularies/{vocabularyId}`

## 3. Auth flow

### Dang ky user

`POST /auth/register`

```json
{
  "email": "user@example.com",
  "password": "secret123",
  "fullName": "Toeic User"
}
```

### Dang nhap

`POST /auth/login`

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

Neu muon mang tien do guest sang user ngay sau login:

1. Goi `POST /auth/login`
2. Goi tiep `POST /public/progress` voi bearer token vua nhan va `progressToken` guest hien tai

Response cua `register/login` tra ve:

- `accessToken`
- `expiresAt`
- `user`
- Token la JWT stateless, FE chi can luu token va gui lai qua `Authorization: Bearer ...`

### Lay user hien tai

`GET /auth/me`

Header:

`Authorization: Bearer {accessToken}`

### Dang xuat

`POST /auth/logout`

Header:

`Authorization: Bearer {accessToken}`

Hien tai logout o backend la stateless theo JWT, nen FE chi can xoa token local sau khi goi endpoint.

## 4. Goi y mapping voi frontend

- Trang landing: goi `GET /public/study-sets`
  - infinite scroll: tang dan `page`, kiem tra `data.last`
  - neu da co progress token thi nen truyen `progressToken` de lay luon `learningStatus` dung theo tien do
- App boot:
  - neu co `accessToken` thi goi `GET /auth/me`
  - goi `POST /public/progress` voi `progressToken` hien tai neu da co
- Trang bo tu: goi `GET /public/study-sets/{slug}` de lay metadata + tien do tong
  - danh sach unit kieu infinite scroll: goi `GET /public/study-sets/{slug}/units`
- Moi tab hoc: goi `GET /public/study-sets/{slug}/units/{unitId}/activities/{mode}`
- Khi user tra loi quiz: goi `POST /public/progress/{progressToken}/answers`
  - sau do cap nhat lai `progressToken` tu `response.data.progress.progressToken`
- Neu `correct = true` va `unitCompleted = false`: dung truc tiep `response.data.studyActivity` de render cau ke tiep trong cung unit
- Neu `unitCompleted = true`: dung truc tiep `response.data.unitCompletion` de mo popup hoan thanh unit
- Nut `Hoc lai tu dau`: goi `POST /public/progress/{progressToken}/study-sets/{slug}/units/{unitId}/restart`
  - sau do cap nhat lai `progressToken` tu `response.data.progress.progressToken`

## 5. Luu y hien tai

- Public study flow van mo cho guest.
- Guest progress gio la stateless, khong con ghi truc tiep vao DB.
- Với user da dang nhap, `study_progress` duoc luu truc tiep theo `user_id`.
- Neu progress dang gan voi user da dang nhap, cac request dung progress do nen gui kem bearer token cua chinh user do.