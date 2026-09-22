# FocusForge frontend

React + Vite + TypeScript + Tailwind, built against the FocusForge Spring Boot API.

## Run locally
```bash
npm install
npm run dev
```
Opens on http://localhost:5173. In dev, `/api/*` is proxied to `VITE_PROXY_TARGET` (defaults to `http://localhost:8080`) by `vite.config.ts`, so you don't need `VITE_API_URL` locally unless you want to point dev at a remote backend.

## Build
```bash
npm run build     # tsc --noEmit, then vite build -> dist/
npm run preview   # serve the production build locally
```

## Production API URL
`.env.production` sets `VITE_API_URL=https://focusforge-4hqx.onrender.com`, which `src/lib/api.ts` reads via `import.meta.env.VITE_API_URL`. Change that file (or override the env var on your host) to point at a different backend.

## Deploy to Vercel
1. Push this repo to GitHub.
2. On vercel.com: New Project → import the repo → set **Root Directory** to `frontend`.
3. Framework preset: Vite (auto-detected via `vercel.json`). Build command `npm run build`, output `dist`.
4. Environment variable: `VITE_API_URL=https://focusforge-4hqx.onrender.com` (or leave it, since `.env.production` already has it baked in — set it in Vercel too if you ever change backends without a redeploy of this file).
5. Deploy. Vercel gives you a `https://<project>.vercel.app` URL.
6. **Update the backend's CORS.** On Render, set `CORS_ORIGINS=https://<project>.vercel.app` on the FocusForge backend service and redeploy it, or the browser will block requests.

## Structure
- `src/lib/` — API client (`api.ts`), TypeScript types mirroring the backend DTOs (`types.ts`), auth context, toasts, formatting helpers.
- `src/components/` — design-system primitives (`ui.tsx`), the 3D-style floating objects, layout/navigation, and shared feature widgets (problem tracker, topic board, session/reflection modals).
- `src/pages/` — one file per sidebar section, matching the backend's route table.

Routing is a `HashRouter`, so the built app works as static files on any host (Vercel, Netlify, S3) with no server-side rewrite rules needed.
