import { useMemo, useState } from 'react'
import { ExternalLink, Pencil, Plus, Trash2 } from 'lucide-react'
import { Badge, Button, CheckField, ConfirmButton, EmptyState, ErrorNote, IconButton, Modal, Segmented, SelectField, Spinner, Stat, TextArea, TextField } from './ui'
import { api } from '../lib/api'
import { todayISO, hm, fmtDate } from '../lib/format'
import { useApi, useForm } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { Difficulty, Problem, ProblemList } from '../lib/types'

const DIFF_TONE: Record<Difficulty, 'moss' | 'saffron' | 'rose'> = { EASY: 'moss', MEDIUM: 'saffron', HARD: 'rose' }
const DIFF_LABEL: Record<Difficulty, string> = { EASY: 'Easy', MEDIUM: 'Medium', HARD: 'Hard' }

interface FormState {
  title: string; number: string; url: string; difficulty: Difficulty; topic: string
  attempts: string; time: string; solved: boolean; date: string; notes: string; solution: string
}
const blank = (): FormState => ({ title: '', number: '', url: '', difficulty: 'MEDIUM', topic: '', attempts: '1', time: '', solved: false, date: todayISO(), notes: '', solution: '' })
const fromProblem = (p: Problem): FormState => ({
  title: p.title, number: p.problemNumber?.toString() ?? '', url: p.url ?? '', difficulty: p.difficulty, topic: p.topic ?? '',
  attempts: String(p.attempts), time: String(p.timeMinutes), solved: p.solved, date: p.date, notes: p.notes ?? '', solution: p.solution ?? '',
})

function ProblemForm({ area, editing, topics, dsa, onClose, onSaved }: {
  area: string; editing: Problem | null; topics: string[]; dsa: boolean; onClose: () => void; onSaved: () => void
}) {
  const toast = useToast()
  const { v, set } = useForm<FormState>(editing ? fromProblem(editing) : blank())
  const [busy, setBusy] = useState(false)
  async function save() {
    setBusy(true)
    const body = {
      title: v.title, problemNumber: v.number ? Number(v.number) : null, url: v.url, difficulty: v.difficulty, topic: v.topic,
      attempts: Number(v.attempts) || 1, timeMinutes: Number(v.time) || 0, solved: v.solved, date: v.date, notes: v.notes, solution: v.solution,
    }
    try {
      if (editing) await api.put(`/api/${area}/problems/${editing.id}`, body); else await api.post(`/api/${area}/problems`, body)
      toast.ok(editing ? 'Problem updated' : 'Problem added'); onSaved(); onClose()
    } catch (e) { toast.err(e) } finally { setBusy(false) }
  }
  return (
    <div className="space-y-3">
      <div className="grid gap-3 sm:grid-cols-[1fr_120px]">
        <TextField label="Problem" value={v.title} onChange={(e) => set('title', e.target.value)} />
        {dsa && <TextField label="Number" type="number" value={v.number} onChange={(e) => set('number', e.target.value)} />}
      </div>
      {dsa && <TextField label="URL" type="url" value={v.url} onChange={(e) => set('url', e.target.value)} placeholder="https://leetcode.com/problems/..." />}
      <div className="grid gap-3 sm:grid-cols-2">
        <SelectField label="Difficulty" value={v.difficulty} onChange={(e) => set('difficulty', e.target.value as Difficulty)}
          options={(['EASY', 'MEDIUM', 'HARD'] as Difficulty[]).map((d) => ({ value: d, label: DIFF_LABEL[d] }))} />
        <div>
          <TextField label="Topic" list={`topics-${area}`} value={v.topic} onChange={(e) => set('topic', e.target.value)} />
          <datalist id={`topics-${area}`}>{topics.map((t) => <option key={t} value={t} />)}</datalist>
        </div>
      </div>
      <div className="grid grid-cols-3 gap-3">
        <TextField label="Attempts" type="number" min={1} value={v.attempts} onChange={(e) => set('attempts', e.target.value)} />
        <TextField label="Minutes" type="number" min={0} value={v.time} onChange={(e) => set('time', e.target.value)} />
        <TextField label="Date" type="date" value={v.date} onChange={(e) => set('date', e.target.value)} />
      </div>
      <CheckField label="Solved" checked={v.solved} onChange={(c) => set('solved', c)} />
      <TextArea label="Notes" value={v.notes} onChange={(e) => set('notes', e.target.value)} />
      <TextArea label="Solution" value={v.solution} onChange={(e) => set('solution', e.target.value)} className="font-mono" />
      <div className="flex justify-end gap-2 pt-1"><Button variant="ghost" onClick={onClose}>Cancel</Button><Button variant="primary" busy={busy} disabled={!v.title.trim()} onClick={save}>{editing ? 'Save changes' : 'Add problem'}</Button></div>
    </div>
  )
}

/** Problem log + live stats for one area (dsa | sql | java | problem-solving). */
export function ProblemTracker({ area, title, topics, dsa = false, onChanged }: {
  area: string; title: string; topics: string[]; dsa?: boolean; onChanged?: () => void
}) {
  const toast = useToast()
  const { data, loading, error, reload } = useApi<ProblemList>(`/api/${area}/problems`)
  const [modal, setModal] = useState<Problem | 'new' | null>(null)
  const [filter, setFilter] = useState<'all' | 'solved' | 'open'>('all')
  const changed = () => { void reload(); onChanged?.() }

  const shown = useMemo(() => (data?.problems ?? []).filter((p) => filter === 'all' || (filter === 'solved' ? p.solved : !p.solved)), [data, filter])

  async function toggleSolved(p: Problem) {
    try {
      await api.put(`/api/${area}/problems/${p.id}`, {
        title: p.title, problemNumber: p.problemNumber, url: p.url, difficulty: p.difficulty, topic: p.topic, attempts: p.attempts,
        timeMinutes: p.timeMinutes, solved: !p.solved, date: p.date, notes: p.notes, solution: p.solution,
      })
      changed()
    } catch (e) { toast.err(e) }
  }
  async function remove(p: Problem) { try { await api.del(`/api/${area}/problems/${p.id}`); changed() } catch (e) { toast.err(e) } }

  if (loading && !data) return <Spinner />
  if (error && !data) return <ErrorNote message={error} onRetry={reload} />
  const s = data!.stats

  return (
    <div>
      <div className="mb-5 flex flex-wrap items-start justify-between gap-4">
        <div className="grid grid-cols-2 gap-x-8 gap-y-3 sm:grid-cols-4">
          <Stat label="Attempted" value={s.attempted} />
          <Stat label="Solved" value={s.solved} />
          <Stat label="Success rate" value={`${s.successRate}%`} />
          <Stat label="Avg. solve time" value={s.avgSolveMinutes ? hm(s.avgSolveMinutes) : '-'} />
        </div>
        <Button variant="primary" onClick={() => setModal('new')}><Plus size={16} /> Add problem</Button>
      </div>
      {dsa && (
        <div className="mb-5 rounded-2xl bg-oat-100 px-4 py-3 text-sm">
          <span className="font-medium text-ink-900">Today</span>: solved {s.solvedToday}
          <span className="text-ink-500"> · Easy {s.easyToday} · Medium {s.mediumToday} · Hard {s.hardToday}</span>
          <span className="float-right text-ink-500">Lifetime solved: <b className="text-ink-900">{s.lifetimeSolved}</b></span>
        </div>
      )}
      <div className="mb-3"><Segmented label={`Filter ${title}`} value={filter} onChange={setFilter}
        options={[{ value: 'all', label: 'All' }, { value: 'solved', label: 'Solved' }, { value: 'open', label: 'Not solved' }]} /></div>

      {shown.length === 0 ? (
        <EmptyState title={data!.problems.length ? 'Nothing matches this filter' : `No ${title.toLowerCase()} yet`}
          body={data!.problems.length ? 'Try another filter.' : 'Add your first problem to start building your success rate.'}
          action={!data!.problems.length && <Button variant="primary" onClick={() => setModal('new')}>Add a problem</Button>} />
      ) : (
        <ul className="space-y-2">
          {shown.map((p) => (
            <li key={p.id} className="flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-white/60 px-4 py-3">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="truncate font-medium text-ink-900">{p.problemNumber ? `${p.problemNumber}. ` : ''}{p.title}</span>
                  {p.url && <a href={p.url} target="_blank" rel="noreferrer" aria-label={`Open ${p.title}`} className="text-dusk-500 hover:text-dusk-700"><ExternalLink size={14} /></a>}
                  {p.demo && <Badge>sample</Badge>}
                </div>
                <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-ink-500">
                  <Badge tone={DIFF_TONE[p.difficulty]}>{DIFF_LABEL[p.difficulty]}</Badge>
                  {p.topic && <span>{p.topic}</span>}<span>· {fmtDate(p.date)}</span>
                  <span>· {p.attempts} {p.attempts === 1 ? 'attempt' : 'attempts'}</span>{p.timeMinutes > 0 && <span>· {hm(p.timeMinutes)}</span>}
                </div>
              </div>
              <div className="flex items-center gap-1">
                <button onClick={() => toggleSolved(p)} aria-pressed={p.solved}
                  className={`rounded-full px-3 py-1 text-xs font-medium ${p.solved ? 'bg-moss-100 text-moss-700' : 'bg-oat-200 text-ink-500'}`}>{p.solved ? 'Solved' : 'Not solved'}</button>
                <IconButton label={`Edit ${p.title}`} onClick={() => setModal(p)}><Pencil size={15} /></IconButton>
                <ConfirmButton label={`Delete ${p.title}`} onConfirm={() => remove(p)}><Trash2 size={15} /></ConfirmButton>
              </div>
            </li>
          ))}
        </ul>
      )}
      <Modal open={modal !== null} title={modal === 'new' ? 'Add a problem' : 'Edit problem'} onClose={() => setModal(null)} wide>
        {modal !== null && <ProblemForm key={modal === 'new' ? 'new' : modal.id} area={area} editing={modal === 'new' ? null : modal} topics={topics} dsa={dsa} onClose={() => setModal(null)} onSaved={changed} />}
      </Modal>
    </div>
  )
}
