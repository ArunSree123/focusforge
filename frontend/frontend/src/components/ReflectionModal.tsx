import { useEffect, useState } from 'react'
import { Button, Modal, Segmented, TextArea } from './ui'
import { api } from '../lib/api'
import { todayISO } from '../lib/format'
import { useForm } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { Reflection } from '../lib/types'

const SCALE = [1, 2, 3, 4, 5].map((n) => ({ value: String(n), label: String(n) }))

/** Optional end-of-day note. Three 1-5 sliders and three questions; nothing is scored or diagnosed. */
export function ReflectionModal({ open, onClose, onSaved }: { open: boolean; onClose: () => void; onSaved: () => void }) {
  const toast = useToast()
  const [busy, setBusy] = useState(false)
  const { v, set, reset } = useForm({ mood: '', energy: '', focus: '', wentWell: '', distractions: '', improveTomorrow: '' })
  useEffect(() => {
    if (!open) return
    api.get<Reflection>(`/api/reflections?date=${todayISO()}`).then((r) => reset({
      mood: r.mood?.toString() ?? '', energy: r.energy?.toString() ?? '', focus: r.focus?.toString() ?? '',
      wentWell: r.wentWell ?? '', distractions: r.distractions ?? '', improveTomorrow: r.improveTomorrow ?? '',
    })).catch(() => undefined)
  }, [open, reset])

  async function save() {
    setBusy(true)
    try {
      await api.put('/api/reflections', {
        date: todayISO(), mood: v.mood ? Number(v.mood) : null, energy: v.energy ? Number(v.energy) : null,
        focus: v.focus ? Number(v.focus) : null, wentWell: v.wentWell, distractions: v.distractions, improveTomorrow: v.improveTomorrow,
      })
      toast.ok('Reflection saved'); onSaved(); onClose()
    } catch (e) { toast.err(e) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} title="How was your day?" onClose={onClose}>
      <p className="mb-4 text-sm text-ink-500">Optional. Answer whatever feels useful and skip the rest.</p>
      <div className="space-y-4">
        {(['mood', 'energy', 'focus'] as const).map((k) => (
          <div key={k}>
            <div className="label capitalize">{k} (1 low, 5 high)</div>
            <Segmented label={k} value={v[k]} onChange={(x) => set(k, v[k] === x ? '' : x)} options={SCALE} />
          </div>
        ))}
        <TextArea label="What went well?" value={v.wentWell} onChange={(e) => set('wentWell', e.target.value)} />
        <TextArea label="What distracted me?" value={v.distractions} onChange={(e) => set('distractions', e.target.value)} />
        <TextArea label="What should I improve tomorrow?" value={v.improveTomorrow} onChange={(e) => set('improveTomorrow', e.target.value)} />
        <div className="flex justify-end gap-2"><Button variant="ghost" onClick={onClose}>Not now</Button><Button variant="primary" busy={busy} onClick={save}>Save</Button></div>
      </div>
    </Modal>
  )
}
