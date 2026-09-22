import { useCallback, useEffect, useState } from 'react'
import { api, errMsg } from './api'

/** GET a path and keep the previous data on screen while reloading. Pass null to skip. */
export function useApi<T>(path: string | null) {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(path !== null)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    if (path === null) return

    try {
      const d = await api.get<T>(path)
      setData(d)
      setError(null)
    } catch (e) {
      setError(errMsg(e))
    } finally {
      setLoading(false)
    }
  }, [path])

  useEffect(() => {
    setLoading(true)
    void load()
  }, [load])

  return { data, loading, error, reload: load }
}

export function useForm<T extends object>(initial: T) {
  const [v, setV] = useState<T>(initial)

  const set = useCallback(
    <K extends keyof T>(k: K, val: T[K]) =>
      setV((p) => ({ ...p, [k]: val })),
    []
  )

  return { v, set, reset: setV }
}

export function useMedia(query: string): boolean {
  const [m, setM] = useState(() => window.matchMedia(query).matches)

  useEffect(() => {
    const mq = window.matchMedia(query)
    const fn = () => setM(mq.matches)

    mq.addEventListener('change', fn)
    return () => mq.removeEventListener('change', fn)
  }, [query])

  return m
}
