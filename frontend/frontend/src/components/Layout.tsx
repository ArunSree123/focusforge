import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import {
  BarChart3, Briefcase, CalendarClock, Cloud, Code2, Coffee, Database, Dumbbell, FileText, FolderGit2,
  GraduationCap, LayoutDashboard, MessagesSquare, MoreHorizontal, Settings, type LucideIcon,
} from 'lucide-react'
import { Kural } from './Kural'
import { Modal, cx } from './ui'

interface Item { to: string; label: string; icon: LucideIcon }
const ITEMS: Item[] = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/routine', label: 'Daily Routine', icon: CalendarClock },
  { to: '/learning', label: 'Learning', icon: GraduationCap },
  { to: '/dsa', label: 'DSA & Problems', icon: Code2 },
  { to: '/sql', label: 'SQL & MySQL', icon: Database },
  { to: '/java', label: 'Java', icon: Coffee },
  { to: '/aws', label: 'AWS', icon: Cloud },
  { to: '/jobs', label: 'Job Applications', icon: Briefcase },
  { to: '/projects', label: 'Projects', icon: FolderGit2 },
  { to: '/interview', label: 'Interview Prep', icon: MessagesSquare },
  { to: '/fitness', label: 'Fitness', icon: Dumbbell },
  { to: '/analytics', label: 'Analytics', icon: BarChart3 },
  { to: '/reports', label: 'Weekly Reports', icon: FileText },
  { to: '/settings', label: 'Settings', icon: Settings },
]
const PRIMARY = ['/', '/routine', '/jobs', '/fitness']

export function Layout() {
  const [more, setMore] = useState(false)
  const { pathname } = useLocation()
  const inMore = !PRIMARY.includes(pathname)

  return (
    <div className="min-h-screen lg:flex">
      <a href="#main" className="sr-only focus:not-sr-only focus:fixed focus:left-3 focus:top-3 focus:z-[70] focus:rounded-lg focus:bg-moss-600 focus:px-3 focus:py-2 focus:text-oat-50">Skip to content</a>

      <aside className="sticky top-0 hidden h-screen w-64 shrink-0 flex-col border-r border-oat-300/60 bg-oat-50/60 px-4 py-6 backdrop-blur-sm lg:flex" aria-label="Primary">
        <div className="mb-8 flex items-center gap-2.5 px-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-moss-600 font-display text-lg text-oat-50">F</span>
          <span className="font-display text-xl text-ink-900">FocusForge</span>
        </div>
        <nav className="flex-1 space-y-0.5 overflow-y-auto pr-1">
          {ITEMS.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} end={to === '/'}
              className={({ isActive }) => cx('flex items-center gap-3 rounded-xl px-3 py-2 text-sm transition-colors',
                isActive ? 'bg-moss-100 font-medium text-moss-700' : 'text-ink-500 hover:bg-oat-200/70 hover:text-ink-900')}>
              <Icon size={18} aria-hidden /> {label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="min-w-0 flex-1">
        <main id="main" className="mx-auto w-full max-w-6xl px-4 pb-32 pt-6 sm:px-8 sm:pt-10 lg:pb-16">
          <Outlet />
          <Kural />
        </main>
      </div>

      <nav aria-label="Primary" className="fixed inset-x-0 bottom-0 z-40 border-t border-oat-300/60 bg-oat-50/90 pb-[env(safe-area-inset-bottom)] backdrop-blur-md lg:hidden">
        <ul className="mx-auto flex max-w-md items-stretch justify-around px-2 py-1.5">
          {ITEMS.filter((i) => PRIMARY.includes(i.to)).map(({ to, label, icon: Icon }) => (
            <li key={to}>
              <NavLink to={to} end={to === '/'} className={({ isActive }) => cx('flex min-w-[64px] flex-col items-center gap-0.5 rounded-xl px-3 py-1.5 text-[11px]', isActive ? 'text-moss-700' : 'text-ink-500')}>
                <Icon size={20} aria-hidden /> {label.replace('Daily ', '').replace('Job Applications', 'Jobs')}
              </NavLink>
            </li>
          ))}
          <li>
            <button onClick={() => setMore(true)} aria-haspopup="dialog"
              className={cx('flex min-w-[64px] flex-col items-center gap-0.5 rounded-xl px-3 py-1.5 text-[11px]', inMore ? 'text-moss-700' : 'text-ink-500')}>
              <MoreHorizontal size={20} aria-hidden /> More
            </button>
          </li>
        </ul>
      </nav>

      <Modal open={more} title="All sections" onClose={() => setMore(false)}>
        <div className="grid grid-cols-2 gap-2">
          {ITEMS.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} end={to === '/'} onClick={() => setMore(false)}
              className={({ isActive }) => cx('flex items-center gap-2.5 rounded-xl px-3 py-3 text-sm', isActive ? 'bg-moss-100 text-moss-700' : 'bg-oat-100 text-ink-700')}>
              <Icon size={18} aria-hidden /> {label}
            </NavLink>
          ))}
        </div>
      </Modal>
    </div>
  )
}
