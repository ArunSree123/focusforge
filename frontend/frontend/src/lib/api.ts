const BASE = import.meta.env.VITE_API_URL ?? ''
const TOKEN_KEY = 'focusforge.token'

export class ApiError extends Error {
  constructor(public status: number, message: string, public fieldErrors?: Record<string, string>) {
    super(message)
  }
}

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t: string) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

/** Fired when the server says the session is no longer valid, so the app can return to sign-in. */
export const AUTH_EXPIRED = 'focusforge:auth-expired'

async function request(method: string, path: string, body?: unknown): Promise<Response> {
  const headers: Record<string, string> = {}
  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  let res: Response
  try {
    res = await fetch(BASE + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) })
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Check that the backend is running and try again.')
  }
  if (!res.ok) {
    let message = 'Something went wrong. Please try again.'
    let fieldErrors: Record<string, string> | undefined
    try {
      const err = await res.json()
      if (err?.message) message = err.message
      fieldErrors = err?.fieldErrors
    } catch { /* non-JSON error body */ }
    if (res.status === 401 && !path.startsWith('/api/auth/')) window.dispatchEvent(new Event(AUTH_EXPIRED))
    throw new ApiError(res.status, message, fieldErrors)
  }
  return res
}

async function json<T>(method: string, path: string, body?: unknown): Promise<T> {
  const res = await request(method, path, body)
  if (res.status === 204) return undefined as T
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export const api = {
  get: <T>(path: string) => json<T>('GET', path),
  post: <T>(path: string, body?: unknown) => json<T>('POST', path, body ?? {}),
  put: <T>(path: string, body?: unknown) => json<T>('PUT', path, body ?? {}),
  del: (path: string) => json<void>('DELETE', path),
  /** Downloads a binary response (used for the weekly PDF). */
  async download(path: string, filename: string) {
    const res = await request('GET', path)
    const url = URL.createObjectURL(await res.blob())
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  },
}

export function errMsg(e: unknown): string {
  return e instanceof ApiError ? e.message : 'Something went wrong. Please try again.'
}

export function qs(params: Record<string, string | number | undefined | null>): string {
  const p = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') p.set(k, String(v)) })
  const s = p.toString()
  return s ? `?${s}` : ''
}
