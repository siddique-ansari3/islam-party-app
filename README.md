# Islam Party Karyakarta App

A worker/volunteer ("karyakarta") management system for the Islam Party (Malegaon, Maharashtra),
built to scale from a single city to all of Maharashtra.

## Structure
- `backend/` - Node.js/Express + PostgreSQL REST API (auth, worker records, bulk SMS/WhatsApp via MSG91)
- `app/` - Native Android app (Kotlin + Jetpack Compose)

## Features
- Add/search party workers by name, mobile number or address, with photo + Aadhaar card photo
- Aadhaar numbers encrypted at rest, masked in list views
- Role-based access: `super_admin` (all cities) vs `city_admin` (their own city only)
- Send meeting notices via SMS and/or WhatsApp to selected workers or everyone in scope (via MSG91)
- Generate a printable business card (PDF/PNG) per worker, with share/print support

## Getting started

### 1. Backend
See [backend/README.md](backend/README.md). In short:
```bash
cd backend
npm install
cp .env.example .env   # fill in DB credentials, JWT secret, AES key, MSG91 keys
npm run db:sync
npm run db:seed        # creates the first super_admin
npm run dev
```

### 2. Android app
This environment has no Android SDK/Gradle installed, so the project can't be built here - open it
in Android Studio (Giraffe or newer):
1. Open the `islam-party-app` folder as a project in Android Studio.
2. Let Android Studio download/regenerate the Gradle wrapper JAR if prompted (or run
   `gradle wrapper --gradle-version 8.7` once if you have Gradle installed locally).
3. Update `API_BASE_URL` in `app/build.gradle.kts` to point at your backend (defaults to
   `http://10.0.2.2:4000/`, which reaches your machine's `localhost:4000` from the Android emulator).
4. Run on an emulator or device (minSdk 26 / Android 8.0+).
5. Log in with the super admin created via `npm run db:seed`, then call
   `POST /api/auth/users` (as that super admin) to create `city_admin` accounts for each city.

## Security notes
- Aadhaar numbers are encrypted (AES-256-GCM) in PostgreSQL and masked in list views.
- Worker/Aadhaar photos are served through an authenticated endpoint, not raw static file hosting.
- Use HTTPS in production (`network_security_config.xml` blocks cleartext to any host except the
  local dev emulator alias). Update `API_BASE_URL` to an `https://` URL before shipping.
- Bulk messaging is rate-limited server-side to reduce abuse/cost risk from a compromised account.
