import { useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Award, Briefcase, Check, Cloud, Code2, Dumbbell, Flame, MessagesSquare, Plus, Sparkles } from 'lucide-react'
import { Object3D } from '../components/Objects3D'
import { ProgressRing } from '../components/ProgressRing'
import { Scorecard } from '../components/Scorecard'
import { SessionModal } from '../components/SessionModal'
import { ReflectionModal } from '../components/ReflectionModal'
import { Badge, Bar, Button, Card, ErrorNote, Spinner, Stat } from '../components/ui'
import { api } from '../lib/api'
import { fmtLong, greeting, hm } from '../lib/format'
import { useApi } from '../lib/hooks'
import { KEY_ROUTE } from '../lib/nav'
import { useToast } from '../lib/toast'
import { useAuth } from '../lib/auth'
import type { Today } from '../lib/types'

export function Dashboard() {
  const { user } = useAuth()
  const toast = useToast()
  const { data, loading, error, reload } = useApi<Today>('/api/dashboard/today')
  const [session, setSession] = useState(false)
  const [reflect, setReflect] = useState(false)
  const [demoBusy, setDemoBusy] = useState(false)

  async function toggle(id: number | null, done: boolean, key: string) {
    if (id === null) { toast.err('This item is tracked automatically, not marked by hand.'); return }
    try { await api[done ? 'del' : 'post'](`/api/routines/${id}/complete`); void reload() } catch (e) { toast.err(e) }
    void key
  }
  async function loadDemo() {
    setDemoBusy(true)
    try { await api.post('/api/demo'); toast.ok('Sample data loaded'); void reload() } catch (e) { toast.err(e) } finally { setDemoBusy(false) }
  }

  if (loading && !data) return <Spinner label="Loading your dashboard" />
  if (error && !data) return <ErrorNote message={error} onRetry={reload} />
  const t = data as Today
  const s = t.score

  return (
    <div>
      <header className="mb-6 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl sm:text-4xl">{greeting()}, {t.name.split(' ')[0]} 👋</h1>
          <p className="mt-1 text-sm text-ink-500">{fmtLong(t.date)}</p>
          <p className="mt-2 max-w-md font-display text-base text-ink-700">Small progress every day becomes a strong career.</p>
        </div>
        <div className="hidden gap-3 sm:flex">
          <Object3D kind="laptop" size={84} depth={10} delay={0} className="opacity-90" />
          <Object3D kind="brackets" size={64} depth={16} delay={1.2} className="mt-4 opacity-90" />
          <Object3D kind="rocket" size={70} depth={22} delay={0.6} className="opacity-90" />
        </div>
      </header>

      {!t.hasDemoData && s.completedTasks === 0 && s.jobsApplied === 0 && (
        <Card className="mb-6 flex flex-wrap items-center justify-between gap-3 bg-saffron-100/60">
          <div className="flex items-center gap-3"><Sparkles className="text-saffron-700" size={20} />
            <p className="text-sm text-ink-700">New here? Load three weeks of sample data to see FocusForge in action.</p></div>
          <Button variant="primary" busy={demoBusy} onClick={loadDemo}>Load sample data</Button>
        </Card>
      )}

      <div className="grid gap-5 lg:grid-cols-3">
        <Card className="flex flex-col items-center justify-center gap-4 lg:col-span-1">
          <ProgressRing value={s.overallPercent} />
          <div className="grid w-full grid-cols-3 gap-2 text-center text-xs">
            <Stat label="Planned" value={hm(s.plannedMinutes)} />
            <Stat label="Completed" value={hm(s.completedMinutes)} />
            <Stat label="Remaining" value={hm(Math.max(0, s.plannedMinutes - s.completedMinutes))} />
          </div>
          <div className="flex w-full items-center justify-between rounded-2xl bg-oat-100 px-4 py-2.5 text-sm">
            <span className="flex items-center gap-1.5 text-ink-700"><Flame size={16} className="text-saffron-500" /> Streak</span>
            <span className="font-display text-lg text-ink-900">{t.streak} {t.streak === 1 ? 'day' : 'days'}</span>
          </div>
        </Card>

        <Card className="lg:col-span-2">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-xl">Today's routine</h2>
            <Button size="sm" onClick={() => setSession(true)}><Plus size={14} /> Log session</Button>
          </div>
          <ul className="space-y-1">
            {s.items.map((i) => (
              <li key={`${i.key}-${i.routineItemId}`}>
                <Link to={KEY_ROUTE[i.key] ?? '/routine'} className="flex items-center justify-between gap-3 rounded-xl px-2 py-2 text-sm transition-colors hover:bg-oat-100">
                  <span className="flex items-center gap-2.5">
                    <button onClick={(e) => { e.preventDefault(); void toggle(i.routineItemId, i.completed, i.key) }} aria-label={i.completed ? `Mark ${i.title} not done` : `Mark ${i.title} done`}
                      className={`flex h-5 w-5 items-center justify-center rounded-full border ${i.completed ? 'border-moss-500 bg-moss-500 text-oat-50' : i.ratio > 0 ? 'border-saffron-500' : 'border-oat-400'}`}>
                      {i.completed && <Check size={12} />}
                    </button>
                    <span className={i.completed ? 'text-ink-500 line-through decoration-oat-400' : 'text-ink-900'}>{i.title}</span>
                  </span>
                  <span className="tabular-nums text-xs text-ink-500">
                    {i.type === 'COUNT' ? `${i.actualCount} / ${i.targetCount}` : i.type === 'TIME' ? `${hm(i.actualMinutes)} / ${hm(i.targetMinutes)}` : i.completed ? 'Done' : ''}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
          <div className="mt-3 flex items-center justify-between rounded-xl bg-dusk-100/60 px-3 py-2 text-sm">
            <span className="text-ink-700">Job applications</span>
            <Link to="/jobs" className="font-medium text-dusk-700">{t.jobs.today} / {t.jobs.target}</Link>
          </div>
          <button onClick={() => setReflect(true)} className="mt-4 text-sm font-medium text-moss-700 hover:underline">
            {t.reflectionLogged ? 'Edit today\u2019s reflection' : 'How was your day? (optional)'}
          </button>
        </Card>
      </div>

      <div className="mt-5 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        <Link to="/dsa"><Card as="div" className="h-full transition-transform hover:-translate-y-0.5">
          <div className="mb-2 flex items-center justify-between"><Code2 className="text-moss-600" /><Object3D kind="puzzle" size={44} depth={6} /></div>
          <p className="text-sm text-ink-500">DSA solved today</p>
          <p className="font-display text-2xl text-ink-900">{t.dsa.solvedToday}</p>
          <p className="mt-0.5 text-xs text-ink-400">E {t.dsa.easy} · M {t.dsa.medium} · H {t.dsa.hard} · lifetime {t.dsa.lifetimeSolved}</p>
        </Card></Link>
        <Link to="/aws"><Card as="div" className="h-full transition-transform hover:-translate-y-0.5">
          <div className="mb-2 flex items-center justify-between"><Cloud className="text-dusk-600" /><Object3D kind="cloud" size={44} depth={6} /></div>
          <p className="text-sm text-ink-500">AWS progress</p>
          <p className="font-display text-2xl text-ink-900">{t.aws.completed} / {t.aws.total}</p>
          <Bar value={t.aws.percent} tone="dusk" className="mt-2" />
        </Card></Link>
        <Link to="/interview"><Card as="div" className="h-full transition-transform hover:-translate-y-0.5">
          <div className="mb-2 flex items-center justify-between"><MessagesSquare className="text-saffron-600" /></div>
          <p className="text-sm text-ink-500">Interview readiness</p>
          <p className="font-display text-2xl text-ink-900">{t.interview.percent}%</p>
          <Bar value={t.interview.percent} tone="saffron" className="mt-2" />
        </Card></Link>
        <Link to="/fitness"><Card as="div" className="h-full transition-transform hover:-translate-y-0.5">
          <div className="mb-2 flex items-center justify-between"><Dumbbell className="text-ink-700" /><Object3D kind="dumbbell" size={44} depth={6} /></div>
          <p className="text-sm text-ink-500">Gym this week</p>
          <p className="font-display text-2xl text-ink-900">{t.fitness.gymSessions} / {t.fitness.gymTarget}</p>
          <p className="mt-0.5 text-xs text-ink-400">Walk {t.fitness.walkDays}/7 · Ball {t.fitness.basketballDays}/7</p>
        </Card></Link>
      </div>

      <div className="mt-5 grid gap-5 lg:grid-cols-3">
        <div className="lg:col-span-2 space-y-5">
          <Card>
            <div className="mb-3 flex items-center gap-2"><Award size={18} className="text-saffron-600" /><h2 className="text-xl">Milestones</h2></div>
            <ul className="grid gap-2 sm:grid-cols-2">
              {t.milestones.map((m) => (
                <li key={m.title} className={`rounded-2xl px-4 py-3 ${m.achieved ? 'bg-moss-100' : 'bg-oat-100'}`}>
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-medium text-ink-900">{m.title}</span>
                    {m.achieved ? <Badge tone="moss">achieved</Badge> : <span className="text-xs text-ink-500">{m.progress} / {m.goal}</span>}
                  </div>
                  <p className="mt-0.5 text-xs text-ink-500">{m.description}</p>
                  {!m.achieved && <Bar value={(m.progress / m.goal) * 100} className="mt-2" />}
                </li>
              ))}
            </ul>
          </Card>
          <Card>
            <div className="mb-1 flex items-center gap-2"><Briefcase size={18} className="text-dusk-600" /><h2 className="text-xl">Job search</h2></div>
            <p className="text-sm text-ink-500">This week: <b className="text-ink-900">{t.jobs.week} / {t.jobs.weekTarget}</b></p>
            <Link to="/jobs" className="mt-3 inline-block text-sm font-medium text-moss-700 hover:underline">Open the job tracker →</Link>
          </Card>
        </div>
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.2 }}><Scorecard score={s} /></motion.div>
      </div>

      <SessionModal open={session} onClose={() => setSession(false)} onSaved={reload} />
      <ReflectionModal open={reflect} onClose={() => setReflect(false)} onSaved={reload} />
    </div>
  )
}
