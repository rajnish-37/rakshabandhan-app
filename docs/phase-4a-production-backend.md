# Phase 4A — Production Backend Preparation

## Target

Production API endpoint:

`https://api.rajnishanand.tech`

The Android Sister and Admin release builds already require `RAKSHA_BACKEND_BASE_URL` to be explicitly supplied at release-build time. Debug builds continue to use the local emulator endpoint unless overridden.

## Backend runtime

The backend is a Node.js 20+ TypeScript application using Fastify and Firebase Admin SDK.

Build:

```bash
cd backend
npm ci
npm run build
```

Start:

```bash
npm start
```

The process listens on `HOST` and `PORT`, with Hostinger expected to provide the production runtime port when using managed Node.js Web App hosting.

## Hostinger deployment plan

1. In Hostinger hPanel, create a **Node.js Web App**.
2. Connect the GitHub repository `rajnish-37/rakshabandhan-app`.
3. Select the production source branch only when the Phase 4 release branch is approved; `dev/phase-4` is the current development branch.
4. Set the application root to the `backend` directory if Hostinger asks for a project/root directory.
5. Use Node.js 20 or newer.
6. Build command: `npm run build`.
7. Start command: `npm start`.
8. Add all production environment variables in Hostinger's environment-variable settings. Never commit real values to GitHub.
9. Deploy first to Hostinger's temporary application domain and verify `/health` and `/health/firestore`.
10. Connect the custom API hostname `api.rajnishanand.tech` to the deployed Node.js application.
11. Verify HTTPS before changing Android release configuration to the production URL.

## Required production variables

```text
NODE_ENV=production
PORT=<provided/configured by Hostinger>
HOST=0.0.0.0
ADMIN_API_KEY=<long random secret>
FIREBASE_PROJECT_ID=rakhi-gift-app
FIREBASE_CLIENT_EMAIL=<service account client email>
FIREBASE_PRIVATE_KEY=<service account private key with escaped newlines as required by the environment UI>
BREVO_API_KEY=<Brevo API key>
BREVO_SENDER_EMAIL=<verified sender email>
BREVO_SENDER_NAME=Raksha Bandhan
```

## Health checks

### Basic process health

`GET /health`

Expected response:

```json
{
  "status": "ok",
  "service": "rakshabandhan-backend"
}
```

### Firebase health

`GET /health/firestore`

Expected production response:

```json
{
  "status": "ok",
  "service": "firestore"
}
```

Do not expose credentials or diagnostic stack traces through these endpoints.

## Production boundary

- Firebase Admin credentials remain server-side only.
- `ADMIN_API_KEY` remains server-side only and must not be embedded in the release Admin APK.
- The Android release build must use an HTTPS backend URL.
- The manual PhonePe/UPI payment model remains unchanged; Phase 4A does not introduce payment-gateway automation.
- Firestore remains the source of truth for persisted sister, gift, and claim state.

## Next step

After the Hostinger application exists and the environment variables are configured, perform the first production deployment and validate both health endpoints. Only then move to Phase 4B (custom domain + HTTPS verification) and Android production endpoint wiring.
