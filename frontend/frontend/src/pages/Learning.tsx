import { useMemo, useState } from 'react'
import { Newspaper, Pencil, Plus, Trash2 } from 'lucide-react'
import { Object3D } from '../components/Objects3D'
import { SessionModal } from '../components/SessionModal'
import { Badge, Button, Card, ConfirmButton, ErrorNote, IconButton, PageHeader, Spinner } from '../components/ui'
import { api } from '../lib/api'
import { CATEGORY_LABEL, fmtDate, hm } from '../lib/format'
import { useApi } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { StudySession } from '../lib/types'

/** Tech news + GK log, plus a weekly-target view across every learning category. */
export function LearningPage() {
  const { data, loading, error, reload } = useApi<StudySession[]>('/api/study-sessions?' + new URLSearchParams({ from: new Date(Date.now() - 29 * 864e5).toISOString().slice(0, 10) }))
  const toast = useToast()
  const [session, setSession] = useState(false)
  const [edit, setEdit] = useState<StudySession | null>(null)

  const techGk = useMemo(() => (data ?? []).filter((s) => s.category === 'TECH_GK'), [data])
  const totals = useMemo(() => {
    const m: Record<string, number> = {}
    ;(data ?? []).forEach((s) => { m[s.category] = (m[s.category] ?? 0) + s.durationMinutes })
    return m
  }, [data])

  async function remove(id: number) { try { await api.del(`/api/study-sessions/${id}`); void reload() } catch (e) { toast.err(e) } }

  return (
    <div>
      <PageHeader title="Learning" subtitle="Tech news, general knowledge, and an overview of every learning track."
        art={<Object3D kind="laptop" size={80} depth={10} />}
        actions={<Button variant="primary" onClick={() => setSession(true)}><Plus size={14} /> Log session</Button>} />

      {loading && !data && <Spinner />}
      {error && !data && <ErrorNote message={error} onRetry={reload} />}
      {data && (
        <div className="space-y-5">
          <Card>
            <h2 className="mb-3 text-xl">Last 30 days by category</h2>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              {Object.entries(CATEGORY_LABEL).map(([k, label]) => (
                <div key={k} className="rounded-2xl bg-oat-100 px-3 py-2.5">
                  <p className="text-xs text-ink-500">{label}</p>
                  <p className="font-display text-xl text-ink-900">{hm(totals[k] ?? 0)}</p>
                </div>
              ))}
            </div>
          </Card>

          <Card>
            <div className="mb-3 flex items-center gap-2"><Newspaper size={18} className="text-saffron-600" /><h2 className="text-xl">Tech news + GK log</h2></div>
            {techGk.length === 0 ? (
              <p className="py-6 text-center text-sm text-ink-500">No sessions logged yet. Aim for 30 minutes a day of tech news, AI news, cloud news or general knowledge.</p>
            ) : (
              <ul className="divide-y divide-oat-300/50">
                {techGk.map((s) => (
                  <li key={s.id} className="flex flex-wrap items-center justify-between gap-3 py-3">
                    <div>
                      <div className="flex flex-wrap items-center gap-2"><span className="font-medium text-ink-900">{s.topic || 'Tech + GK'}</span>{s.demo && <Badge>sample</Badge>}</div>
                      <p className="text-xs text-ink-500">{fmtDate(s.date)} · {hm(s.durationMinutes)}{s.source ? ` · ${s.source}` : ''}</p>
                    </div>
                    <div className="flex items-center gap-1">
                      <IconButton label="Edit" onClick={() => setEdit(s)}><Pencil size={15} /></IconButton>
                      <ConfirmButton label="Delete session" onConfirm={() => remove(s.id)}><Trash2 size={15} /></ConfirmButton>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>
      )}
      <SessionModal open={session} onClose={() => setSession(false)} category="TECH_GK" onSaved={reload} />
      {edit && <EditSessionInline session={edit} onClose={() => setEdit(null)} onSaved={reload} />}
    </div>
  )
}

function EditSessionInline({ session, onClose, onSaved }: { session: StudySession; onClose: () => void; onSaved: () => void }) {
  const toast = useToast()
  const [minutes, setMinutes] = useState(String(session.durationMinutes))
  const [topic, setTopic] = useState(session.topic ?? '')
  async function save() {
    try {
      await api.put(`/api/study-sessions/${session.id}`, { category: session.category, date: session.date, durationMinutes: Number(minutes), topic, source: session.source, notes: session.notes })
      toast.ok('Updated'); onSaved(); onClose()
    } catch (e) { toast.err(e) }
  }
  return (
    <div role="dialog" aria-modal className="fixed inset-0 z-50 flex items-center justify-center bg-ink-900/40 p-4" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <div className="w-full max-w-sm rounded-3xl bg-oat-50 p-5 shadow-lift">
        <h2 className="mb-3 text-lg">Edit session</h2>
        <label className="label">Topic</label><input className="input mb-3" value={topic} onChange={(e) => setTopic(e.target.value)} />
        <label className="label">Minutes</label><input type="number" className="input mb-4" value={minutes} onChange={(e) => setMinutes(e.target.value)} />
        <div className="flex justify-end gap-2"><Button variant="ghost" onClick={onClose}>Cancel</Button><Button variant="primary" onClick={save}>Save</Button></div>
      </div>
    </div>
  )
}
