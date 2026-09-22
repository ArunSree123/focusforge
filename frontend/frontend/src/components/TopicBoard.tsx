import { useState } from 'react'
import { Plus, Trash2 } from 'lucide-react'
import { Bar, Button, ConfirmButton, TextField, cx } from './ui'
import { api } from '../lib/api'
import { useToast } from '../lib/toast'
import type { Topic, TopicList, TopicStatus } from '../lib/types'

export const DEFAULT_LABELS: Record<TopicStatus, string> = {
  NOT_STARTED: 'Not Started', LEARNING: 'Learning', PRACTICING: 'Practicing', COMPLETED: 'Completed',
}
export const INTERVIEW_LABELS: Record<TopicStatus, string> = {
  NOT_STARTED: 'Not Started', LEARNING: 'Learning', PRACTICING: 'Reviewed', COMPLETED: 'Confident',
}
const ORDER: TopicStatus[] = ['NOT_STARTED', 'LEARNING', 'PRACTICING', 'COMPLETED']
const TONE: Record<TopicStatus, string> = {
  NOT_STARTED: 'bg-oat-200 text-ink-500', LEARNING: 'bg-saffron-100 text-saffron-700',
  PRACTICING: 'bg-dusk-100 text-dusk-700', COMPLETED: 'bg-moss-100 text-moss-700',
}

/** Topics grouped by theme; each has a four-step status. `checklistGroup` renders as simple done/not-done items. */
export function TopicBoard({ area, data, labels = DEFAULT_LABELS, checklistGroup, hideGroup, onChanged }: {
  area: string; data: TopicList; labels?: Record<TopicStatus, string>
  checklistGroup?: string; hideGroup?: string; onChanged: () => void
}) {
  const toast = useToast()
  const [adding, setAdding] = useState(false)
  const [group, setGroup] = useState('')
  const [name, setName] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)

  const groups: [string, Topic[]][] = []
  data.topics.forEach((t) => {
    if (t.group === hideGroup) return
    const g = groups.find(([n]) => n === t.group)
    if (g) g[1].push(t); else groups.push([t.group, [t]])
  })

  async function setStatus(t: Topic, status: TopicStatus) {
    if (t.status === status) return
    setBusyId(t.id)
    try { await api.put(`/api/${area}/topics/${t.id}`, { status }); onChanged() } catch (e) { toast.err(e) } finally { setBusyId(null) }
  }
  async function add() {
    try {
      await api.post(`/api/${area}/topics`, { group: group || groups[0]?.[0] || 'Custom', name })
      setName(''); setAdding(false); toast.ok('Topic added'); onChanged()
    } catch (e) { toast.err(e) }
  }
  async function remove(t: Topic) {
    try { await api.del(`/api/${area}/topics/${t.id}`); onChanged() } catch (e) { toast.err(e) }
  }

  return (
    <div>
      <div className="space-y-5">
        {groups.map(([g, topics]) => {
          const done = topics.filter((t) => t.status === 'COMPLETED').length
          return (
            <div key={g}>
              <div className="mb-2 flex items-center justify-between text-xs font-medium uppercase tracking-wide text-ink-500">
                <span>{g}</span><span className="tabular-nums">{done}/{topics.length}</span>
              </div>
              <ul className="space-y-1.5">
                {topics.map((t) => (
                  <li key={t.id} className="flex flex-wrap items-center justify-between gap-2 rounded-2xl bg-white/60 px-3 py-2">
                    <span className={cx('text-sm', t.status === 'COMPLETED' ? 'text-ink-500 line-through decoration-oat-400' : 'text-ink-900')}>{t.name}</span>
                    <div className="flex items-center gap-1" role="group" aria-label={`Status of ${t.name}`}>
                      {g === checklistGroup ? (
                        <button aria-pressed={t.status === 'COMPLETED'} disabled={busyId === t.id}
                          onClick={() => setStatus(t, t.status === 'COMPLETED' ? 'NOT_STARTED' : 'COMPLETED')}
                          className={cx('rounded-full px-3 py-1 text-xs font-medium', t.status === 'COMPLETED' ? TONE.COMPLETED : TONE.NOT_STARTED)}>
                          {t.status === 'COMPLETED' ? 'Done' : 'Mark done'}
                        </button>
                      ) : ORDER.map((s) => (
                        <button key={s} aria-pressed={t.status === s} disabled={busyId === t.id} onClick={() => setStatus(t, s)}
                          className={cx('rounded-full px-2.5 py-1 text-[11px] font-medium transition-colors',
                            t.status === s ? TONE[s] : 'text-ink-400 hover:bg-oat-200')}>
                          {labels[s]}
                        </button>
                      ))}
                      <ConfirmButton label={`Remove ${t.name}`} onConfirm={() => remove(t)} className="!h-7 !w-7"><Trash2 size={14} /></ConfirmButton>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )
        })}
      </div>

      {adding ? (
        <div className="mt-5 grid gap-3 rounded-2xl bg-oat-100 p-4 sm:grid-cols-[1fr_1fr_auto] sm:items-end">
          <TextField label="Group" list={`groups-${area}`} value={group} onChange={(e) => setGroup(e.target.value)} placeholder={groups[0]?.[0]} />
          <datalist id={`groups-${area}`}>{groups.map(([g]) => <option key={g} value={g} />)}</datalist>
          <TextField label="Topic name" value={name} onChange={(e) => setName(e.target.value)} />
          <div className="flex gap-2"><Button variant="primary" onClick={add} disabled={!name.trim()}>Add</Button><Button variant="ghost" onClick={() => setAdding(false)}>Cancel</Button></div>
        </div>
      ) : (
        <Button className="mt-5" size="sm" onClick={() => setAdding(true)}><Plus size={14} /> Add a topic</Button>
      )}
    </div>
  )
}

export function TopicProgressBar({ data, label }: { data: TopicList; label: string }) {
  return (
    <div>
      <div className="mb-1.5 flex items-baseline justify-between"><span className="text-sm text-ink-700">{label}</span>
        <span className="font-display text-lg text-ink-900">{data.completed} / {data.total} <span className="text-sm text-ink-500">({data.completionPct}%)</span></span></div>
      <Bar value={data.completionPct} label={label} />
    </div>
  )
}
