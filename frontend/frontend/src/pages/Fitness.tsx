import { useState } from 'react'
import { Bike, Dumbbell, Pencil, Plus, Trash2 } from 'lucide-react'
import { Object3D } from '../components/Objects3D'
import { Badge, Button, Card, ConfirmButton, ErrorNote, IconButton, Modal, PageHeader, Segmented, SelectField, Spinner, Stat, TextArea, TextField } from '../components/ui'
import { api } from '../lib/api'
import { fmtDate, hhmm, hm, todayISO } from '../lib/format'
import { useApi, useForm } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { ActivityType, Exercise, Fitness, FitnessSummary, WorkoutType } from '../lib/types'

const WORKOUTS: WorkoutType[] = ['PUSH', 'PULL', 'LEGS', 'FULL_BODY', 'CARDIO', 'OTHER']
const WORKOUT_LABEL: Record<WorkoutType, string> = { PUSH: 'Push', PULL: 'Pull', LEGS: 'Legs', FULL_BODY: 'Full body', CARDIO: 'Cardio', OTHER: 'Other' }

interface FormState {
  type: ActivityType; date: string; startTime: string; endTime: string; minutes: string
  workoutType: WorkoutType; distance: string; steps: string; notes: string; exercises: Exercise[]
}
const blank = (type: ActivityType): FormState => ({ type, date: todayISO(), startTime: '', endTime: '', minutes: type === 'GYM' ? '120' : '30', workoutType: 'PUSH', distance: '', steps: '', notes: '', exercises: [] })

function ActivityForm({ type, onClose, onSaved }: { type: ActivityType; onClose: () => void; onSaved: () => void }) {
  const toast = useToast()
  const { v, set } = useForm<FormState>(blank(type))
  const [busy, setBusy] = useState(false)

  function updateExercise(i: number, patch: Partial<Exercise>) {
    set('exercises', v.exercises.map((e, idx) => (idx === i ? { ...e, ...patch } : e)))
  }
  async function save() {
    setBusy(true)
    try {
      await api.post('/api/fitness', {
        type: v.type, date: v.date, startTime: v.startTime || null, endTime: v.endTime || null,
        durationMinutes: v.startTime && v.endTime ? 0 : Number(v.minutes) || 0,
        workoutType: v.type === 'GYM' ? v.workoutType : null,
        distanceKm: v.distance ? Number(v.distance) : null, steps: v.steps ? Number(v.steps) : null,
        notes: v.notes, exercises: v.type === 'GYM' ? v.exercises.filter((e) => e.name.trim()) : [],
      })
      toast.ok('Logged'); onSaved(); onClose()
    } catch (e) { toast.err(e) } finally { setBusy(false) }
  }
  return (
    <div className="space-y-3">
      <div className="grid grid-cols-2 gap-3"><TextField label="Date" type="date" value={v.date} onChange={(e) => set('date', e.target.value)} />
        {v.type === 'GYM' && <SelectField label="Workout type" value={v.workoutType} onChange={(e) => set('workoutType', e.target.value as WorkoutType)} options={WORKOUTS.map((w) => ({ value: w, label: WORKOUT_LABEL[w] }))} />}
      </div>
      <div className="grid grid-cols-3 gap-3">
        <TextField label="Start" type="time" value={v.startTime} onChange={(e) => set('startTime', e.target.value)} />
        <TextField label="End" type="time" value={v.endTime} onChange={(e) => set('endTime', e.target.value)} />
        <TextField label="Or minutes" type="number" value={v.minutes} onChange={(e) => set('minutes', e.target.value)} />
      </div>
      {v.type === 'WALK' && <div className="grid grid-cols-2 gap-3">
        <TextField label="Distance (km, optional)" type="number" step="0.1" value={v.distance} onChange={(e) => set('distance', e.target.value)} />
        <TextField label="Steps (optional)" type="number" value={v.steps} onChange={(e) => set('steps', e.target.value)} />
      </div>}
      {v.type === 'GYM' && (
        <div>
          <div className="label">Exercises</div>
          <div className="space-y-2">
            {v.exercises.map((e, i) => (
              <div key={i} className="grid grid-cols-[1fr_60px_60px_70px_auto] items-end gap-2">
                <TextField label="" aria-label="Exercise name" placeholder="Exercise" value={e.name} onChange={(ev) => updateExercise(i, { name: ev.target.value })} />
                <TextField label="" aria-label="Sets" type="number" placeholder="Sets" value={e.sets || ''} onChange={(ev) => updateExercise(i, { sets: Number(ev.target.value) })} />
                <TextField label="" aria-label="Reps" type="number" placeholder="Reps" value={e.reps || ''} onChange={(ev) => updateExercise(i, { reps: Number(ev.target.value) })} />
                <TextField label="" aria-label="Weight kg" type="number" placeholder="kg" value={e.weightKg ?? ''} onChange={(ev) => updateExercise(i, { weightKg: ev.target.value ? Number(ev.target.value) : null })} />
                <IconButton label="Remove exercise" onClick={() => set('exercises', v.exercises.filter((_, idx) => idx !== i))}><Trash2 size={14} /></IconButton>
              </div>
            ))}
          </div>
          <Button size="sm" className="mt-2" onClick={() => set('exercises', [...v.exercises, { name: '', sets: 3, reps: 10, weightKg: null }])}><Plus size={13} /> Add exercise</Button>
        </div>
      )}
      <TextArea label="Notes" value={v.notes} onChange={(e) => set('notes', e.target.value)} />
      <p className="text-xs text-ink-400">This is an activity tracker only, not training or medical advice.</p>
      <div className="flex justify-end gap-2 pt-1"><Button variant="ghost" onClick={onClose}>Cancel</Button><Button variant="primary" busy={busy} onClick={save}>Log activity</Button></div>
    </div>
  )
}

export function FitnessPage() {
  const toast = useToast()
  const { data: summary, reload: reloadSummary } = useApi<FitnessSummary>('/api/fitness/summary')
  const { data, loading, error, reload } = useApi<Fitness[]>('/api/fitness')
  const [modalType, setModalType] = useState<ActivityType | null>(null)

  const reloadAll = () => { void reload(); void reloadSummary() }
  async function remove(id: number) { try { await api.del(`/api/fitness/${id}`); reloadAll() } catch (e) { toast.err(e) } }

  return (
    <div>
      <PageHeader title="Fitness" subtitle="Gym, walking and basketball, tracked as activity only." art={<Object3D kind="dumbbell" size={80} depth={10} />}
        actions={<>
          <Button variant="primary" onClick={() => setModalType('GYM')}><Dumbbell size={14} /> Log gym</Button>
          <Button onClick={() => setModalType('WALK')}><Bike size={14} /> Log walk</Button>
          <Button onClick={() => setModalType('BASKETBALL')}>Log basketball</Button>
        </>} />

      {summary && (
        <Card className="mb-5 grid grid-cols-2 gap-4 sm:grid-cols-4">
          <Stat label="Gym" value={`${summary.gymSessions} / ${summary.gymTarget}`} sub="sessions this week" />
          <Stat label="Walking" value={`${summary.walkDays} / 7`} sub="days" />
          <Stat label="Basketball" value={`${summary.basketballDays} / 7`} sub="days" />
          <Stat label="Active morning" value={`${summary.activeMornings} / 7`} sub="days" />
        </Card>
      )}

      <Card>
        <h2 className="mb-3 text-xl">Recent activity</h2>
        {loading && !data && <Spinner />}
        {error && !data && <ErrorNote message={error} onRetry={reload} />}
        {data && (data.length === 0 ? <p className="py-6 text-center text-sm text-ink-500">No activity logged yet.</p> : (
          <ul className="divide-y divide-oat-300/50">
            {data.map((f) => (
              <li key={f.id} className="flex flex-wrap items-center justify-between gap-3 py-3">
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-medium text-ink-900">{f.type === 'GYM' ? `Gym${f.workoutType ? ` · ${WORKOUT_LABEL[f.workoutType]}` : ''}` : f.type === 'WALK' ? 'Walk' : 'Basketball'}</span>
                    {f.demo && <Badge>sample</Badge>}
                  </div>
                  <p className="text-xs text-ink-500">{fmtDate(f.date)} · {hm(f.durationMinutes)}
                    {f.startTime && ` · ${hhmm(f.startTime)}\u2013${hhmm(f.endTime)}`}
                    {f.distanceKm != null && ` · ${f.distanceKm} km`}{f.steps != null && ` · ${f.steps} steps`}</p>
                  {f.exercises.length > 0 && <p className="mt-0.5 text-xs text-ink-400">{f.exercises.map((e) => `${e.name} ${e.sets}×${e.reps}${e.weightKg ? `@${e.weightKg}kg` : ''}`).join(', ')}</p>}
                </div>
                <ConfirmButton label="Delete activity" onConfirm={() => remove(f.id)}><Trash2 size={15} /></ConfirmButton>
              </li>
            ))}
          </ul>
        ))}
      </Card>
      <Modal open={modalType !== null} title={`Log ${modalType === 'GYM' ? 'gym session' : modalType === 'WALK' ? 'walk' : 'basketball'}`} onClose={() => setModalType(null)} wide>
        {modalType && <ActivityForm key={modalType} type={modalType} onClose={() => setModalType(null)} onSaved={reloadAll} />}
      </Modal>
    </div>
  )
}
