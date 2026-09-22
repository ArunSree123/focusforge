import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { CheckCircle2, AlertCircle } from 'lucide-react'
import { errMsg } from './api'

interface ToastItem { id: number; kind: 'ok' | 'err'; text: string }
interface ToastCtx { ok: (t: string) => void; err: (e: unknown) => void }
const Ctx = createContext<ToastCtx | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([])
  const push = useCallback((kind: 'ok' | 'err', text: string) => {
    const id = Date.now() + Math.random()
    setItems((p) => [...p, { id, kind, text }])
    setTimeout(() => setItems((p) => p.filter((i) => i.id !== id)), kind === 'err' ? 6000 : 3000)
  }, [])
  const value = useMemo<ToastCtx>(() => ({
    ok: (t) => push('ok', t),
    err: (e) => push('err', typeof e === 'string' ? e : errMsg(e)),
  }), [push])

  return (
    <Ctx.Provider value={value}>
      {children}
      <div className="pointer-events-none fixed inset-x-0 bottom-24 z-[60] flex flex-col items-center gap-2 px-4 lg:bottom-6" role="status" aria-live="polite">
        <AnimatePresence>
          {items.map((t) => (
            <motion.div key={t.id} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }}
              className={`pointer-events-auto flex max-w-md items-start gap-2 rounded-2xl px-4 py-3 text-sm shadow-lift ${
                t.kind === 'ok' ? 'bg-moss-700 text-oat-50' : 'bg-rose-700 text-oat-50'}`}>
              {t.kind === 'ok' ? <CheckCircle2 size={18} className="mt-0.5 shrink-0" /> : <AlertCircle size={18} className="mt-0.5 shrink-0" />}
              <span>{t.text}</span>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </Ctx.Provider>
  )
}

export function useToast(): ToastCtx {
  const c = useContext(Ctx)
  if (!c) throw new Error('useToast must be used inside ToastProvider')
  return c
}
