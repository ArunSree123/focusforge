import { useState } from 'react'
import { Check, Clock, Pencil, Plus, Sunrise, Trash2 } from 'lucide-react'
import { Object3D } from '../components/Objects3D'
import { SessionModal } from '../components/SessionModal'
import { Badge, Button, Card, ConfirmButton, ErrorNote, IconButton, Modal, PageHeader, Spinner, TextField } from '../components/ui'
import { api } from '../lib/api'
import { hm, hhmm, to12h, fmtLong, todayISO } from '../lib/format'
import { useApi, useForm } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { RoutineDay, RoutineItem } from '../lib/types'

function WakeCard({ day, onSaved }: { day: RoutineDay; onSaved: () => void }) {
  const toast = useToast()
  const [time, setTime] = useState(hhmm(day.wake.actual) || new Date().toTimeString().slice(0, 5))
  const [busy, setBusy] = useState(false)
  async function save() {
    setBusy(true)
    try { await api.put('/api/routines/wake', { date: day.date, actual: time }); toast.ok('Wake-up logged'); onSaved() } catch (e) { toast.err(e) } finally { setBusy(false) }
  }
  return (
    <Card>
      <div className="mb-3 flex items-center gap-2"><Sunrise size={18} className="text-saffron-600" /><h2 className="text-xl">Wake up</h2></div>
      <p className="text-sm text-ink-500">Target: <b className="text-ink-900">{to12h(day.wake.target)}</b></p>
      {day.wake.actual && (
        <p className="mt-1 text-sm text-ink-500">Actual: <b className="text-ink-900">{to12h(day.wake.actual)}</b>{' '}
          <Badge tone={day.wake.completed && (day.wake.diffMinutes ?? 0) <= 15 ? 'moss' : 'saffron'}>
            {(day.wake.diffMinutes ?? 0) === 0 ? 'On time' : (day.wake.diffMinutes ?? 0) > 0 ? `${day.wake.diffMinutes}m late` : `${Math.abs(day.wake.diffMinutes ?? 0)}m early`}
          </Badge>
        </p>
      )}
      <div className="mt-3 flex items-end gap-2">
        <TextField label="Log actual wake time" type="time" value={time} onChange={(e) => setTime(e.target.value)} className="max-w-[160px]" />
        <Button variant="primary" busy={busy} onClick={save}>Save</Button>
      </div>
    </Card>
  )
}

function MorningChecklist({ day, onSaved }: { day: RoutineDay; onSaved: () => void }) {
  const toast = useToast()
  const [adding, setAdding] = useState(false)
  const [title, setTitle] = useState('')
  async function toggle(id: number, done: boolean) {
    try { await api.put('/api/routines/morning-checks', { date: day.date, itemId: id, done: !done }); onSaved() } catch (e) { toast.err(e) }
  }
  async function add() {
    try { await api.post('/api/routines/morning-items', { title }); setTitle(''); setAdding(false); onSaved() } catch (e) { toast.err(e) }
  }
  async function remove(id: number) { try { await api.del(`/api/routines/morning-items/${id}`); onSaved() } catch (e) { toast.err(e) } }
  return (
    <Card>
      <h2 className="mb-3 text-xl">Active morning</h2>
      <ul className="space-y-1.5">
        {day.morning.map((m) => (
          <li key={m.id} className="flex items-center justify-between gap-2 rounded-xl px-2 py-1.5 hover:bg-oat-100">
            <button onClick={() => toggle(m.id, m.done)} className="flex items-center gap-2.5 text-sm">
              <span className={`flex h-5 w-5 items-center justify-center rounded-full border ${m.done ? 'border-moss-500 bg-moss-500 text-oat-50' : 'border-oat-400'}`}>{m.done && <Check size={12} />}</span>
              <span className={m.done ? 'text-ink-500 line-through decoration-oat-400' : 'text-ink-900'}>{m.title}</span>
            </button>
            <ConfirmButton label={`Remove ${m.title}`} onConfirm={() => remove(m.id)} className="!h-7 !w-7"><Trash2 size={13} /></ConfirmButton>
          </li>
        ))}
      </ul>
      {adding ? (
        <div className="mt-3 flex items-end gap-2"><TextField label="New item" value={title} onChange={(e) => setTitle(e.target.value)} />
          <Button variant="primary" size="sm" onClick={add} disabled={!title.trim()}>Add</Button><Button size="sm" variant="ghost" onClick={() => setAdding(false)}>Cancel</Button></div>
      ) : <Button size="sm" className="mt-3" onClick={() => setAdding(true)}><Plus size={14} /> Add item</Button>}
    </Card>
  )
}

function ItemEditor({ item, onClose, onSaved }: { item: RoutineItem | 'new'; onClose: () => void; onSaved: () => void }) {
  const toast = useToast()
  const editing = item !== 'new'
  const { v, set } = useForm({
    title: editing ? item.title : '', minutes: editing ? String(item.targetMinutes) : '30',
    count: editing ? String(item.targetCount) : '0', time: editing ? hhmm(item.scheduledTime) : '',
  })
  const [busy, setBusy] = useState(false)
  async function save() {
    setBusy(true)
    const body = { title: v.title, targetMinutes: Number(v.minutes) || 0, targetCount: Number(v.count) || 0, scheduledTime: v.time || null }
    try {
      if (editing) await api.put(`/api/routines/${item.id}`, body); else await api.post('/api/routines', body)
      toast.ok('Saved'); onSaved(); onClose()
    } catch (e) { toast.err(e) } finally { setBusy(false) }
  }
  return (
    <div className="space-y-3">
      <TextField label="Title" value={v.title} onChange={(e) => set('title', e.target.value)} disabled={editing && item.key !== 'CUSTOM'}
        hint={editing && item.key !== 'CUSTOM' ? 'Built-in block names cannot be changed' : undefined} />
      <div className="grid grid-cols-2 gap-3">
        <TextField label="Target minutes" type="number" min={0} max={1440} value={v.minutes} onChange={(e) => set('minutes', e.target.value)} />
        <TextField label="Target count (jobs only)" type="number" min={0} value={v.count} onChange={(e) => set('count', e.target.value)} />
      </div>
      <TextField label="Scheduled time" type="time" value={v.time} onChange={(e) => set('time', e.target.value)} />
      <div className="flex justify-end gap-2 pt-1"><Button variant="ghost" onClick={onClose}>Cancel</Button><Button variant="primary" busy={busy} disabled={!v.title.trim()} onClick={save}>Save</Button></div>
    </div>
  )
}

export function RoutinePage() {
  const [date, setDate] = useState(todayISO())
  const { data, loading, error, reload } = useApi<RoutineDay>(`/api/routines?date=${date}`)
  const toast = useToast()
  const [editItem, setEditItem] = useState<RoutineItem | 'new' | null>(null)
  const [session, setSession] = useState(false)

  async function toggleComplete(item: RoutineItem, done: boolean) {
    try { await api[done ? 'del' : 'post'](`/api/routines/${item.id}/complete?date=${date}`); void reload() } catch (e) { toast.err(e) }
  }
  async function remove(item: RoutineItem) { try { await api.del(`/api/routines/${item.id}`); void reload() } catch (e) { toast.err(e) } }

  return (
    <div>
      <PageHeader title="Daily Routine" subtitle="Your day, planned and tracked block by block."
        art={<Object3D kind="brackets" size={72} depth={12} />}
        actions={<><TextField label="" aria-label="Date" type="date" value={date} onChange={(e) => setDate(e.target.value)} className="w-auto" />
          <Button variant="primary" onClick={() => setSession(true)}><Plus size={14} /> Log session</Button></>} />

      {loading && !data && <Spinner />}
      {error && !data && <ErrorNote message={error} onRetry={reload} />}
      {data && (
        <div className="space-y-5">
          <p className="text-sm text-ink-500">{fmtLong(data.date)}</p>
          <div className="grid gap-5 lg:grid-cols-2"><WakeCard day={data} onSaved={reload} /><MorningChecklist day={data} onSaved={reload} /></div>

          <Card>
            <div className="mb-3 flex items-center justify-between"><h2 className="text-xl">Schedule</h2>
              <Button size="sm" onClick={() => setEditItem('new')}><Plus size={14} /> Add custom block</Button></div>
            <ul className="divide-y divide-oat-300/50">
              {data.items.map((item) => {
                const s = data.score.items.find((i) => i.routineItemId === item.id)
                const manual = s?.manual
                return (
                  <li key={item.id} className="flex flex-wrap items-center gap-3 py-3">
                    <button onClick={() => toggleComplete(item, !!s?.completed)} aria-label={s?.completed ? `Mark ${item.title} not done` : `Mark ${item.title} done`}
                      className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full border ${s?.completed ? 'border-moss-500 bg-moss-500 text-oat-50' : 'border-oat-400'}`}>
                      {s?.completed && <Check size={13} />}
                    </button>
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className={`font-medium ${s?.completed ? 'text-ink-500 line-through decoration-oat-400' : 'text-ink-900'}`}>{item.title}</span>
                        {manual && <Badge tone="moss">marked done</Badge>}
                        {!item.active && <Badge>off</Badge>}
                      </div>
                      <div className="mt-0.5 flex flex-wrap items-center gap-2 text-xs text-ink-500">
                        {item.scheduledTime && <span className="inline-flex items-center gap-1"><Clock size={12} /> {to12h(item.scheduledTime)}</span>}
                        {item.key === 'JOBS' ? <span>{s?.actualCount ?? 0} / {item.targetCount} applications</span>
                          : <span>{hm(s?.actualMinutes ?? 0)} / {hm(item.targetMinutes)}</span>}
                      </div>
                    </div>
                    <div className="flex items-center gap-1">
                      <IconButton label={`Edit ${item.title}`} onClick={() => setEditItem(item)}><Pencil size={15} /></IconButton>
                      {item.key === 'CUSTOM' && <ConfirmButton label={`Remove ${item.title}`} onConfirm={() => remove(item)}><Trash2 size={15} /></ConfirmButton>}
                    </div>
                  </li>
                )
              })}
            </ul>
          </Card>
        </div>
      )}

      <Modal open={editItem !== null} title={editItem === 'new' ? 'Add custom block' : `Edit ${editItem ? editItem.title : ''}`} onClose={() => setEditItem(null)}>
        {editItem && <ItemEditor item={editItem} onClose={() => setEditItem(null)} onSaved={reload} />}
      </Modal>
      <SessionModal open={session} onClose={() => setSession(false)} onSaved={reload} />
    </div>
  )
}
