import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, AUTH_EXPIRED, tokenStore } from './api'
import type { AuthResponse, User } from './types'

interface AuthCtx {
  user: User | null
  ready: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string, loadDemo: boolean) => Promise<void>
  logout: () => void
  setUser: (u: User) => void
}
const Ctx = createContext<AuthCtx | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [ready, setReady] = useState(false)

  const logout = useCallback(() => { tokenStore.clear(); setUser(null) }, [])

  useEffect(() => {
    if (!tokenStore.get()) { setReady(true); return }
    api.get<User>('/api/auth/me').then(setUser).catch(() => tokenStore.clear()).finally(() => setReady(true))
  }, [])

  useEffect(() => {
    window.addEventListener(AUTH_EXPIRED, logout)
    return () => window.removeEventListener(AUTH_EXPIRED, logout)
  }, [logout])

  const accept = (r: AuthResponse) => { tokenStore.set(r.token); setUser(r.user) }
  const value = useMemo<AuthCtx>(() => ({
    user, ready, logout, setUser,
    login: async (email, password) => accept(await api.post<AuthResponse>('/api/auth/login', { email, password })),
    register: async (name, email, password, loadDemo) =>
      accept(await api.post<AuthResponse>('/api/auth/register', { name, email, password, loadDemo })),
  }), [user, ready, logout])

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>
}

export function useAuth(): AuthCtx {
  const c = useContext(Ctx)
  if (!c) throw new Error('useAuth must be used inside AuthProvider')
  return c
}
