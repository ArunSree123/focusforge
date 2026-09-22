import { useEffect, useState } from 'react'
import { animate, useReducedMotion } from 'framer-motion'

/** Animated ring. The number counts up once; with reduced motion it simply appears. */
export function ProgressRing({ value, size = 168, stroke = 12, label = "Today's Progress" }: { value: number; size?: number; stroke?: number; label?: string }) {
  const reduce = useReducedMotion()
  const [shown, setShown] = useState(reduce ? value : 0)
  useEffect(() => {
    if (reduce) { setShown(value); return }
    const c = animate(shown, value, { duration: 1.1, ease: [0.22, 1, 0.36, 1], onUpdate: (v) => setShown(v) })
    return () => c.stop()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value, reduce])
  const r = (size - stroke) / 2
  const circ = 2 * Math.PI * r
  return (
    <div className="relative" style={{ width: size, height: size }} role="img" aria-label={`${label}: ${Math.round(value)} percent`}>
      <svg width={size} height={size} className="-rotate-90">
        <defs>
          <linearGradient id="ring" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#6FA08A" /><stop offset="100%" stopColor="#3F6754" />
          </linearGradient>
        </defs>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#EDE6D8" strokeWidth={stroke} />
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="url(#ring)" strokeWidth={stroke} strokeLinecap="round"
          strokeDasharray={circ} strokeDashoffset={circ * (1 - shown / 100)} />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="font-display text-4xl text-ink-900">{Math.round(shown)}%</span>
        <span className="mt-0.5 text-xs text-ink-500">{label}</span>
      </div>
    </div>
  )
}
