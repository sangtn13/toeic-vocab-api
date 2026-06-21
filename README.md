# TOEIC Vocabulary API

Spring Boot backend cho app hoc tu vung TOEIC don gian, gom:

- `public API` cho user/guest hoc theo `study set -> unit -> activity`
- `auth API` dung JWT cho dang ky, dang nhap, lay thong tin user hien tai
- `admin API` duoc khoa bang role `ADMIN` de quan ly bo tu, unit va tu vung
- PostgreSQL 15 + Flyway migration

## Chay database

```bash
docker compose up -d
```

Neu Flyway bao checksum mismatch tren database local, co the reset lai volume truoc khi start app:

```bash
docker compose down -v
docker compose up -d
```

## Bien moi truong chinh

- Docker Compose:
  - `POSTGRES_DB`
  - `POSTGRES_USER`
  - `POSTGRES_PASSWORD`
  - `POSTGRES_PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`
- `JPA_SHOW_SQL`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `GUEST_PROGRESS_EXPIRATION_MS`
- `ADMIN_EMAIL`
- `ADMIN_PASSWORD`
- `ADMIN_NAME`

`.env.example` chi dung cho bien cua Docker Compose.

`.env.render.example` la danh sach bien mau cho backend khi deploy production len Render. File nay chi chua placeholder, khong chua secret that.

Phan bien backend local van duoc tham khao trong `src/main/resources/application.yml.template`.

## Render production

Repo nay da duoc bo sung:

- `src/main/resources/application-prod.yml`: override cho production, dac biet la `PORT` cua Render va health endpoint.
- `Dockerfile`: cach deploy phu hop vi Render khong native ho tro Java/Spring Boot.
- `render.yaml`: Blueprint an toan, khong commit secret.
- `.env.render.example`: danh sach env de copy/import vao Render.

Render xu ly env theo 2 cach:

- `Render Dashboard > Environment`: luu secret tren Render, khong nam trong GitHub.
- `render.yaml`: chi nen de gia tri khong nhay cam hoac placeholder `sync: false` / `generateValue: true`.

Secret khong nen commit vao repo:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `ADMIN_EMAIL`
- `ADMIN_PASSWORD`

Luu y cho Spring Boot:

- `DB_URL` tren Render can la JDBC URL, vi du `jdbc:postgresql://host:5432/database`.
- Neu ban tao Render Postgres, hay copy Internal Database URL cua no va doi sang dinh dang JDBC truoc khi luu vao `DB_URL`.
- `JWT_SECRET` co the de Render tu sinh qua `generateValue: true` trong `render.yaml`.

Quy trinh deploy goi y:

1. Push repo len GitHub.
2. Tao Web Service tren Render tu repo nay va dung `render.yaml`, hoac tao service bang tay roi import `.env.render.example`.
3. Dien cac bien `sync: false` trong Dashboard.
4. Neu dung Render Postgres, tao database truoc roi copy thong tin ket noi vao `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
5. Deploy, sau do Render se health check qua `/actuator/health`.

## Chay ung dung

```bash
.\mvnw.cmd spring-boot:run
```

Swagger:

- `http://localhost:5050/swagger-ui.html`

## Domain

- `study_sets`: bo tu, vi du `600 Tu Vung TOEIC`, co `status = DRAFT | PUBLISHED | ARCHIVED`
- `study_units`: unit ben trong bo tu
- `vocabularies`: tung tu/cum tu
- `users`: tai khoan dang nhap cua app voi role `ADMIN` hoac `USER`
- `study_progress`: tien do theo tung tu cua user da dang nhap

Tat ca ID domain trong API va database deu dung `UUID`.

Chi tiet endpoint co trong [FRONTEND_API.md](/D:/toeic-vocab-api/FRONTEND_API.md).

## Auth mac dinh cho local

Khi app khoi dong, backend se tu seed 1 tai khoan admin neu email do chua ton tai:

Gia tri mac dinh trong local:

- email: `admin@toeic.local`
- password: `admin123`

JWT secret can la chuoi Base64 du manh.

## Goi y tich hop progress voi frontend

- Với guest: goi `POST /public/progress` de lay `progressToken` stateless, roi luu token do phia client.
- Sau moi request lam thay doi tien do nhu `submit answer` hoac `restart unit`, FE can cap nhat lai `progressToken` hien tai tu response.
- Khi user vua login, FE nen goi lai `POST /public/progress` kem bearer token va `progressToken` guest hien tai de import tien do guest sang tai khoan hien tai.
- Với user da dang nhap, backend persist tien do truc tiep theo `user_id`.
