import { useId, type ReactNode } from 'react'
import { motion, motionValue, useReducedMotion, useTransform } from 'framer-motion'

export type ObjectKind = 'laptop' | 'rocket' | 'brackets' | 'database' | 'puzzle' | 'cloud' | 'dumbbell' | 'basketball' | 'briefcase' | 'server'

// One shared pointer position drives every object's parallax, so there is a single listener.
const px = motionValue(0)
const py = motionValue(0)
if (typeof window !== 'undefined') {
  window.addEventListener('pointermove', (e) => {
    px.set(e.clientX / window.innerWidth - 0.5)
    py.set(e.clientY / window.innerHeight - 0.5)
  }, { passive: true })
}

/** Two-stop gradient definitions per object: [light, base, shadow]. */
const TONES: Record<ObjectKind, [string, string, string]> = {
  laptop: ['#8FB5A2', '#4E7B66', '#2F5343'], rocket: ['#F4D58E', '#C98F1F', '#8A5F0F'],
  brackets: ['#9DB7CE', '#5F7C9A', '#3F5873'], database: ['#F4D58E', '#D9A441', '#9A6E14'],
  puzzle: ['#D8B4C8', '#A9748F', '#744A62'], cloud: ['#BFE0DD', '#6BA3A0', '#456F6C'],
  dumbbell: ['#B9A48F', '#8A6F5A', '#5A4636'], basketball: ['#F2B36E', '#D9822B', '#9A5514'],
  briefcase: ['#C3A57A', '#8A6A3F', '#5B4426'], server: ['#9DB7CE', '#5F7C9A', '#3F5873'],
}

function Shape({ kind, p }: { kind: ObjectKind; p: string }) {
  const g = `url(#${p}g)`, s = `url(#${p}s)`
  switch (kind) {
    case 'laptop':
      return (<>
        <rect x="26" y="22" width="68" height="46" rx="6" fill={s} />
        <rect x="30" y="26" width="60" height="38" rx="3" fill="#24302B" />
        <rect x="36" y="33" width="22" height="3" rx="1.5" fill="#93B8A4" /><rect x="40" y="40" width="30" height="3" rx="1.5" fill="#E9C476" />
        <rect x="36" y="47" width="16" height="3" rx="1.5" fill="#9DB7CE" /><rect x="56" y="47" width="20" height="3" rx="1.5" fill="#93B8A4" />
        <path d="M16 74h88l-8 12H24z" fill={g} /><rect x="46" y="76" width="28" height="3" rx="1.5" fill="#00000022" />
      </>)
    case 'rocket':
      return (<>
        <path d="M60 14c14 10 20 30 16 56H44c-4-26 2-46 16-56z" fill={g} />
        <path d="M60 14c14 10 20 30 16 56H60z" fill="#00000018" />
        <circle cx="60" cy="42" r="8" fill="#F6F1E8" /><circle cx="60" cy="42" r="5" fill="#5F7C9A" />
        <path d="M44 56L30 76l16-4zM76 56l14 20-16-4z" fill={s} />
        <path d="M52 72h16l-8 24z" fill="#F4C36A" /><path d="M56 72h8l-4 14z" fill="#FBE6B0" />
      </>)
    case 'brackets':
      return (<g fill="none" stroke={g} strokeWidth="11" strokeLinecap="round" strokeLinejoin="round">
        <path d="M40 34L18 60l22 26" /><path d="M80 34l22 26-22 26" />
        <path d="M67 28L53 92" strokeWidth="8" stroke={s} />
      </g>)
    case 'database':
      return (<>
        {[0, 22, 44].map((o) => (<g key={o}>
          <path d={`M24 ${34 + o}v14c0 7 16 12 36 12s36-5 36-12V${34 + o}`} fill={g} />
          <ellipse cx="60" cy={34 + o} rx="36" ry="12" fill={s} />
          <ellipse cx="60" cy={32 + o} rx="30" ry="8" fill="#FFFFFF33" />
        </g>))}
      </>)
    case 'puzzle':
      return (<>
        <path d="M26 34h22a8 8 0 1 1 16 0h22v22a8 8 0 1 1 0 16v22H64a8 8 0 1 0-16 0H26V72a8 8 0 1 0 0-16z" fill={g} />
        <path d="M60 34h22v22a8 8 0 1 1 0 16v22H64z" fill="#00000012" />
        <circle cx="48" cy="60" r="5" fill="#FFFFFF44" />
      </>)
    case 'cloud':
      return (<>
        <path d="M34 82a20 20 0 0 1-2-40 26 26 0 0 1 50-6 22 22 0 0 1 8 44z" fill={g} />
        <path d="M60 36a26 26 0 0 1 22 0 22 22 0 0 1 8 46H60z" fill="#00000012" />
        <path d="M40 48a16 16 0 0 1 14-10" stroke="#FFFFFF66" strokeWidth="4" strokeLinecap="round" fill="none" />
      </>)
    case 'dumbbell':
      return (<>
        <rect x="34" y="55" width="52" height="10" rx="5" fill={s} />
        <rect x="14" y="38" width="14" height="44" rx="6" fill={g} /><rect x="26" y="44" width="10" height="32" rx="4" fill={s} />
        <rect x="92" y="38" width="14" height="44" rx="6" fill={g} /><rect x="84" y="44" width="10" height="32" rx="4" fill={s} />
      </>)
    case 'basketball':
      return (<>
        <circle cx="60" cy="58" r="34" fill={g} />
        <path d="M26 58h68M60 24v68M34 34c14 14 14 38 0 48M86 34c-14 14-14 38 0 48" stroke="#5A2F0C" strokeWidth="3" fill="none" strokeLinecap="round" />
        <path d="M60 24a34 34 0 0 1 0 68z" fill="#00000012" />
      </>)
    case 'briefcase':
      return (<>
        <path d="M46 34v-6a6 6 0 0 1 6-6h16a6 6 0 0 1 6 6v6" stroke={s} strokeWidth="6" fill="none" />
        <rect x="18" y="34" width="84" height="54" rx="10" fill={g} />
        <path d="M18 58h84" stroke="#00000022" strokeWidth="3" /><rect x="52" y="52" width="16" height="14" rx="3" fill="#F4D58E" />
        <path d="M60 34h42a0 0 0 0 1 0 0v44a10 10 0 0 1-10 10H60z" fill="#00000012" />
      </>)
    case 'server':
      return (<>
        {[0, 26, 52].map((o) => (<g key={o}>
          <rect x="24" y={20 + o} width="72" height="22" rx="7" fill={g} />
          <circle cx="38" cy={31 + o} r="3" fill="#93E0B0" /><circle cx="48" cy={31 + o} r="3" fill="#F4D58E" />
          <rect x="66" y={28 + o} width="22" height="6" rx="3" fill="#00000030" />
        </g>))}
      </>)
  }
}

/**
 * A softly floating SVG object. `depth` sets how far it drifts with the pointer.
 * Decorative only: aria-hidden, and it never covers content (callers place it in a corner).
 */
export function Object3D({ kind, size = 96, depth = 14, delay = 0, className }: {
  kind: ObjectKind; size?: number; depth?: number; delay?: number; className?: string
}) {
  const reduce = useReducedMotion()
  const id = useId().replace(/[^a-zA-Z0-9]/g, '')
  const [l, b, d] = TONES[kind]
  const x = useTransform(px, (v) => v * depth)
  const y = useTransform(py, (v) => v * depth)
  const inner: ReactNode = (
    <svg width={size} height={size} viewBox="0 0 120 120" aria-hidden focusable="false" className="overflow-visible drop-shadow-[0_10px_10px_rgba(74,68,61,0.18)]">
      <defs>
        <linearGradient id={`${id}g`} x1="0" y1="0" x2="1" y2="1"><stop offset="0" stopColor={l} /><stop offset="0.55" stopColor={b} /><stop offset="1" stopColor={d} /></linearGradient>
        <linearGradient id={`${id}s`} x1="0" y1="0" x2="0" y2="1"><stop offset="0" stopColor={b} /><stop offset="1" stopColor={d} /></linearGradient>
      </defs>
      <ellipse cx="60" cy="108" rx="30" ry="5" fill="#4A443D" opacity="0.10" />
      <Shape kind={kind} p={id} />
    </svg>
  )
  if (reduce) return <div className={className} aria-hidden>{inner}</div>
  return (
    <motion.div aria-hidden style={{ x, y }} className={className}>
      <motion.div
        animate={{ y: [0, -7, 0], rotate: [-2, 2, -2] }}
        transition={{ duration: 6 + (delay % 3), repeat: Infinity, ease: 'easeInOut', delay }}
        whileHover={{ scale: 1.08, rotate: 6 }}>
        {inner}
      </motion.div>
    </motion.div>
  )
}
