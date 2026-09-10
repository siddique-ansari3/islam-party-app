# Islam Party Backend

Node.js/Express + PostgreSQL REST API for the Islam Party worker management app.

## Features
- JWT auth with two roles: `super_admin` (all cities) and `city_admin` (scoped to one city)
- Worker CRUD with photo + Aadhaar photo upload
- Aadhaar numbers encrypted at rest (AES-256-GCM), masked in list views
- Search workers by name / mobile / address, with pagination
- Bulk SMS/WhatsApp via MSG91 to selected workers or all workers (city-scoped for city_admins)
- Protected file serving (no direct static access to uploaded photos)

## Setup

1. Install PostgreSQL and create a database:
   ```bash
   createdb islam_party
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Copy `.env.example` to `.env` and fill in real values:
   ```bash
   cp .env.example .env
   ```
   - Generate `AADHAAR_ENCRYPTION_KEY`:
     ```bash
     node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
     ```
   - Generate a strong `JWT_SECRET` the same way.
   - Sign up at https://msg91.com for `MSG91_AUTH_KEY`, an approved DLT SMS template, and a WhatsApp Business API template (required for `MSG91_SMS_TEMPLATE_ID` / `MSG91_WHATSAPP_*` variables).
4. Create tables:
   ```bash
   npm run db:sync
   ```
5. Create the first super admin (edit `SEED_SUPER_ADMIN_*` in `.env` first):
   ```bash
   npm run db:seed
   ```
6. Start the API:
   ```bash
   npm run dev   # or: npm start
   ```

The API listens on `http://localhost:4000` by default. Use `POST /api/auth/login` to get a JWT, then pass it as `Authorization: Bearer <token>` on subsequent requests.

## Notes on production hardening
- Put this behind HTTPS (e.g. Nginx/Caddy reverse proxy with a TLS cert) — tokens and Aadhaar photos must never travel over plain HTTP.
- Back up the PostgreSQL database and the `uploads/` directory regularly (Aadhaar data lives in both).
- Rotate `JWT_SECRET` and `AADHAAR_ENCRYPTION_KEY` only with a proper migration plan — rotating the encryption key requires re-encrypting existing `aadhaar_encrypted` values.
