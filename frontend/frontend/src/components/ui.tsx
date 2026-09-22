import { useEffect, useId, useRef, type ButtonHTMLAttributes, type ReactNode } from 'react'
import { AnimatePresence, motion, useReducedMotion } from 'framer-motion'
import { Loader2, X } from 'lucide-react'

export function cx(...c: (string | false | null | undefined)[]) { return c.filter(Boolean).join(' ') }

export function Card({ children, className, as: Tag = 'section', ...rest }: {
  children: ReactNode; className?: string; as?: 'section' | 'div' | 'article'; 'aria-label'?: string
}) {
  return <Tag className={cx('card p-5 sm:p-6', className)} {...rest}>{children}</Tag>
}

type Variant = 'primary' | 'soft' | 'ghost' | 'danger'
const VARIANT: Record<Variant, string> = {
  primary: 'bg-moss-600 text-oat-50 hover:bg-moss-700 shadow-soft',
  soft: 'bg-oat-200 text-ink-900 hover:bg-oat-300',
  ghost: 'text-ink-700 hover:bg-oat-200/70',
  danger: 'bg-rose-100 text-rose-700 hover:bg-rose-500 hover:text-oat-50',
}
export function Button({ variant = 'soft', size = 'md', busy, className, children, disabled, ...rest }:
  ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant; size?: 'sm' | 'md'; busy?: boolean }) {
  return (
    <button
      className={cx('inline-flex items-center justify-center gap-1.5 rounded-xl font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50',
        size === 'sm' ? 'px-3 py-1.5 text-xs' : 'px-4 py-2 text-sm', VARIANT[variant], className)}
      disabled={disabled || busy} {...rest}>
      {busy && <Loader2 size={14} className="animate-spin" aria-hidden />}
      {children}
    </button>
  )
}

export function IconButton({ label, children, className, ...rest }: ButtonHTMLAttributes<HTMLButtonElement> & { label: string }) {
  return (
    <button aria-label={label} title={label}
      className={cx('inline-flex h-8 w-8 items-center justify-center rounded-lg text-ink-500 transition-colors hover:bg-oat-200 hover:text-ink-900', className)} {...rest}>
      {children}
    </button>
  )
}

export function Modal({ open, title, onClose, children, wide }: { open: boolean; title: string; onClose: () => void; children: ReactNode; wide?: boolean }) {
  const ref = useRef<HTMLDivElement>(null)
  const titleId = useId()
  const reduce = useReducedMotion()
  useEffect(() => {
    if (!open) return
    const prev = document.activeElement as HTMLElement | null
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', onKey)
    document.body.style.overflow = 'hidden'
    ref.current?.querySelector<HTMLElement>('input,select,textarea,button')?.focus()
    return () => { document.removeEventListener('keydown', onKey); document.body.style.overflow = ''; prev?.focus() }
  }, [open, onClose])
  return (
    <AnimatePresence>
      {open && (
        <motion.div className="fixed inset-0 z-50 flex items-end justify-center bg-ink-900/40 p-0 backdrop-blur-[2px] sm:items-center sm:p-4"
          initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
          onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}>
          <motion.div ref={ref} role="dialog" aria-modal="true" aria-labelledby={titleId}
            initial={reduce ? false : { y: 24, opacity: 0 }} animate={{ y: 0, opacity: 1 }} exit={{ opacity: 0 }}
            className={cx('max-h-[92vh] w-full overflow-y-auto rounded-t-3xl bg-oat-50 p-5 shadow-lift sm:rounded-3xl sm:p-6', wide ? 'sm:max-w-2xl' : 'sm:max-w-md')}>
            <div className="mb-4 flex items-start justify-between gap-4">
              <h2 id={titleId} className="text-xl">{title}</h2>
              <IconButton label="Close" onClick={onClose}><X size={18} /></IconButton>
            </div>
            {children}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}

interface FieldBase { label: string; hint?: string; className?: string }
export function TextField({ label, hint, className, ...rest }: FieldBase & React.InputHTMLAttributes<HTMLInputElement>) {
  const id = useId()
  return (
    <div className={className}>
      <label htmlFor={id} className="label">{label}</label>
      <input id={id} className="input" {...rest} />
      {hint && <p className="mt-1 text-xs text-ink-400">{hint}</p>}
    </div>
  )
}
export function TextArea({ label, hint, className, ...rest }: FieldBase & React.TextareaHTMLAttributes<HTMLTextAreaElement>) {
  const id = useId()
  return (
    <div className={className}>
      <label htmlFor={id} className="label">{label}</label>
      <textarea id={id} rows={3} className="input resize-y" {...rest} />
      {hint && <p className="mt-1 text-xs text-ink-400">{hint}</p>}
    </div>
  )
}
export function SelectField({ label, className, options, ...rest }:
  FieldBase & React.SelectHTMLAttributes<HTMLSelectElement> & { options: { value: string; label: string }[] }) {
  const id = useId()
  return (
    <div className={className}>
      <label htmlFor={id} className="label">{label}</label>
      <select id={id} className="input" {...rest}>
        {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
    </div>
  )
}
export function CheckField({ label, checked, onChange }: { label: string; checked: boolean; onChange: (v: boolean) => void }) {
  return (
    <label className="flex cursor-pointer items-center gap-2 text-sm text-ink-700">
      <input type="checkbox" checked={checked} onChange={(e) => onChange(e.target.checked)} className="h-4 w-4 rounded border-oat-400 accent-moss-600" />
      {label}
    </label>
  )
}

export function Segmented<T extends string>({ value, onChange, options, label }: {
  value: T; onChange: (v: T) => void; options: { value: T; label: string }[]; label: string
}) {
  return (
    <div role="group" aria-label={label} className="inline-flex flex-wrap gap-1 rounded-xl bg-oat-200/70 p-1">
      {options.map((o) => (
        <button key={o.value} type="button" aria-pressed={o.value === value} onClick={() => onChange(o.value)}
          className={cx('rounded-lg px-3 py-1.5 text-xs font-medium transition-colors',
            o.value === value ? 'bg-oat-50 text-ink-900 shadow-soft' : 'text-ink-500 hover:text-ink-900')}>
          {o.label}
        </button>
      ))}
    </div>
  )
}

export function Bar({ value, tone = 'moss', label, className }: { value: number; tone?: 'moss' | 'saffron' | 'dusk'; label?: string; className?: string }) {
  const v = Math.max(0, Math.min(100, value))
  const color = { moss: 'bg-moss-500', saffron: 'bg-saffron-500', dusk: 'bg-dusk-500' }[tone]
  return (
    <div role="progressbar" aria-valuenow={Math.round(v)} aria-valuemin={0} aria-valuemax={100} aria-label={label}
      className={cx('h-2 w-full overflow-hidden rounded-full bg-oat-200', className)}>
      <div className={cx('h-full rounded-full transition-[width] duration-700', color)} style={{ width: `${v}%` }} />
    </div>
  )
}

export function Badge({ children, tone = 'neutral' }: { children: ReactNode; tone?: 'neutral' | 'moss' | 'saffron' | 'dusk' | 'rose' }) {
  const t = {
    neutral: 'bg-oat-200 text-ink-700', moss: 'bg-moss-100 text-moss-700', saffron: 'bg-saffron-100 text-saffron-700',
    dusk: 'bg-dusk-100 text-dusk-700', rose: 'bg-rose-100 text-rose-700',
  }[tone]
  return <span className={cx('inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium', t)}>{children}</span>
}

export function Stat({ label, value, sub }: { label: string; value: ReactNode; sub?: ReactNode }) {
  return (
    <div>
      <div className="text-xs text-ink-500">{label}</div>
      <div className="font-display text-2xl leading-tight text-ink-900">{value}</div>
      {sub && <div className="text-xs text-ink-400">{sub}</div>}
    </div>
  )
}

export function Spinner({ label = 'Loading' }: { label?: string }) {
  return <div className="flex items-center justify-center gap-2 py-16 text-sm text-ink-500" role="status"><Loader2 className="animate-spin" size={18} /> {label}</div>
}

export function ErrorNote({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div role="alert" className="flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-rose-100 px-4 py-3 text-sm text-rose-700">
      <span>{message}</span>
      {onRetry && <Button size="sm" onClick={onRetry}>Try again</Button>}
    </div>
  )
}

/** Small drawn illustration + a next step. Empty screens are invitations to act. */
export function EmptyState({ title, body, action }: { title: string; body: string; action?: ReactNode }) {
  return (
    <div className="flex flex-col items-center px-4 py-10 text-center">
      <svg width="88" height="72" viewBox="0 0 88 72" aria-hidden className="mb-4">
        <ellipse cx="44" cy="64" rx="30" ry="5" fill="#EDE6D8" />
        <rect x="16" y="14" width="56" height="42" rx="10" fill="#FBF8F2" stroke="#DED4C1" strokeWidth="2" />
        <rect x="26" y="26" width="24" height="4" rx="2" fill="#93B8A4" />
        <rect x="26" y="35" width="36" height="4" rx="2" fill="#E9C476" />
        <rect x="26" y="44" width="16" height="4" rx="2" fill="#DED4C1" />
        <circle cx="66" cy="16" r="9" fill="#4E7B66" /><path d="M62 16l3 3 5-6" stroke="#FBF8F2" strokeWidth="2.2" fill="none" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
      <p className="font-display text-lg text-ink-900">{title}</p>
      <p className="mt-1 max-w-xs text-sm text-ink-500">{body}</p>
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}

export function PageHeader({ title, subtitle, actions, art }: { title: string; subtitle?: string; actions?: ReactNode; art?: ReactNode }) {
  return (
    <header className="mb-6 flex items-start justify-between gap-4">
      <div>
        <h1 className="text-3xl sm:text-4xl">{title}</h1>
        {subtitle && <p className="mt-1 max-w-xl text-sm text-ink-500">{subtitle}</p>}
        {actions && <div className="mt-4 flex flex-wrap gap-2">{actions}</div>}
      </div>
      {art && <div className="hidden shrink-0 sm:block">{art}</div>}
    </header>
  )
}

export function ConfirmButton({ label, onConfirm, children, className }: { label: string; onConfirm: () => void; children: ReactNode; className?: string }) {
  return (
    <IconButton label={label} className={className} onClick={() => { if (window.confirm(`${label}?`)) onConfirm() }}>{children}</IconButton>
  )
}
