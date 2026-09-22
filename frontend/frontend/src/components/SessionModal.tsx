import { useEffect, useState } from 'react'
import { Button, Modal, SelectField, TextArea, TextField } from './ui'
import { api } from '../lib/api'
import { CATEGORY_LABEL, todayISO } from '../lib/format'
import { useForm } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { Category } from '../lib/types'

const CATS = Object.entries(CATEGORY_LABEL).map(([value, label]) => ({ value, label }))

/** Logs one study session. Category can be fixed by the caller (e.g. on the Java page). */
export function SessionModal({ open, onClose, category, onSaved }: {
  open: boolean; onClose: () => void; category?: Category; onSaved: () => void
}) {
  const toast = useToast()
  const [busy, setBusy] = useState(false)
  const { v, set, reset } = useForm({ category: (category ?? 'JAVA') as Category, minutes: '60', date: todayISO(), topic: '', source: '', notes: '' })
  useEffect(() => { if (open) reset({ category: category ?? 'JAVA', minutes: '60', date: todayISO(), topic: '', source: '', notes: '' }) }, [open, category, reset])

  async function save() {
    setBusy(true)
    try {
      await api.post('/api/study-sessions', {
        category: v.category, date: v.date, durationMinutes: Number(v.minutes),
        topic: v.topic, source: v.source, notes: v.notes,
      })
      toast.ok('Session logged')
      onSaved()
      onClose()
    } catch (e) { toast.err(e) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} title="Log a study session" onClose={onClose}>
      <div className="space-y-3">
        {!category && <SelectField label="Category" value={v.category} onChange={(e) => set('category', e.target.value as Category)} options={CATS} />}
        <div className="grid grid-cols-2 gap-3">
          <TextField label="Minutes" type="number" min={1} max={720} value={v.minutes} onChange={(e) => set('minutes', e.target.value)} />
          <TextField label="Date" type="date" value={v.date} onChange={(e) => set('date', e.target.value)} />
        </div>
        <TextField label="Topic" value={v.topic} onChange={(e) => set('topic', e.target.value)} placeholder="e.g. Streams, Joins, IAM policies" />
        {(v.category === 'TECH_GK' || v.source) && (
          <TextField label="Source" value={v.source} onChange={(e) => set('source', e.target.value)} placeholder="e.g. Hacker News, The Hindu" />
        )}
        <TextArea label="Notes" value={v.notes} onChange={(e) => set('notes', e.target.value)} />
        <div className="flex justify-end gap-2 pt-1">
          <Button variant="ghost" onClick={onClose}>Cancel</Button>
          <Button variant="primary" busy={busy} onClick={save}>Save session</Button>
        </div>
      </div>
    </Modal>
  )
}
