import type { Category } from './types'

/** 150 -> "2h 30m", 45 -> "45m", 0 -> "0m". */
export function hm(minutes: number): string {
  const m = Math.max(0, Math.round(minutes))
  const h = Math.floor(m / 60), r = m % 60
  if (h === 0) return `${r}m`
  return r === 0 ? `${h}h` : `${h}h ${r}m`
}
export const hours = (minutes: number, digits = 1) => Number((minutes / 60).toFixed(digits))

/** Local-date ISO string. toISOString() would shift the day for users ahead of UTC. */
export function isoDate(d: Date): string {
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}
export const todayISO = () => isoDate(new Date())
export function parseISO(s: string): Date {
  const [y, m, d] = s.split('-').map(Number)
  return new Date(y, m - 1, d)
}
export function addDays(s: string, n: number): string {
  const d = parseISO(s)
  d.setDate(d.getDate() + n)
  return isoDate(d)
}
export function mondayOf(s: string): string {
  const d = parseISO(s)
  const dow = (d.getDay() + 6) % 7
  d.setDate(d.getDate() - dow)
  return isoDate(d)
}
export const fmtDate = (s: string, opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short' }) =>
  parseISO(s).toLocaleDateString('en-IN', opts)
export const fmtLong = (s: string) => fmtDate(s, { weekday: 'long', day: 'numeric', month: 'long' })
export const hhmm = (t: string | null | undefined) => (t ? t.slice(0, 5) : '')

export function to12h(t: string | null | undefined): string {
  if (!t) return ''
  const [h, m] = t.split(':').map(Number)
  return `${((h + 11) % 12) + 1}:${String(m).padStart(2, '0')} ${h < 12 ? 'AM' : 'PM'}`
}

export function greeting(now = new Date()): string {
  const h = now.getHours()
  if (h < 12) return 'Good Morning'
  if (h < 17) return 'Good Afternoon'
  return 'Good Evening'
}

export const pct = (part: number, whole: number) => (whole <= 0 ? 0 : Math.min(100, Math.round((part * 100) / whole)))

export const CATEGORY_LABEL: Record<Category, string> = {
  TECH_GK: 'Tech + GK', SQL: 'SQL + MySQL', PROBLEM_SOLVING: 'Problem solving', JAVA: 'Java',
  AWS: 'AWS', PROJECT_INTERVIEW: 'Project + Interview', DSA: 'DSA + LeetCode',
}
export const CATEGORY_COLOR: Record<string, string> = {
  JAVA: '#4E7B66', DSA: '#5F7C9A', SQL: '#C98F1F', PROBLEM_SOLVING: '#A9748F',
  AWS: '#6BA3A0', PROJECT_INTERVIEW: '#7A8F4E', TECH_GK: '#B59B72', GYM: '#8A6F5A',
}
export const KEY_LABEL: Record<string, string> = {
  ...CATEGORY_LABEL, MORNING: 'Morning routine', GYM: 'Gym', JOBS: 'Job applications', CUSTOM: 'Custom',
}
export const JOB_STATUS_LABEL: Record<string, string> = {
  SAVED: 'Saved', APPLIED: 'Applied', ASSESSMENT: 'Assessment', INTERVIEW: 'Interview',
  HR_ROUND: 'HR Round', OFFER: 'Offer', REJECTED: 'Rejected', ON_HOLD: 'On Hold',
}
