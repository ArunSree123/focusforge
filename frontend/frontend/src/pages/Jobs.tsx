import { useState } from 'react'
import { ExternalLink, Filter, Pencil, Plus, Trash2 } from 'lucide-react'
import { Object3D } from '../components/Objects3D'
import { Badge, Bar, Button, Card, ConfirmButton, EmptyState, ErrorNote, IconButton, Modal, PageHeader, SelectField, Spinner, Stat, TextArea, TextField } from '../components/ui'
import { api, qs } from '../lib/api'
import { fmtDate, JOB_STATUS_LABEL, todayISO } from '../lib/format'
import { useApi, useForm } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { Job, JobList, JobStatus } from '../lib/types'

const STATUSES = Object.keys(JOB_STATUS_LABEL) as JobStatus[]
const STATUS_TONE: Record<JobStatus, 'neutral' | 'moss' | 'saffron' | 'dusk' | 'rose'> = {
  SAVED: 'neutral', APPLIED: 'dusk', ASSESSMENT: 'saffron', INTERVIEW: 'saffron', HR_ROUND: 'saffron',
  OFFER: 'moss', REJECTED: 'rose', ON_HOLD: 'neutral',
}

interface FormState {
  company: string; title: string; location: string; jobUrl: string; dateApplied: string
  resumeVersion: string; referral: string; status: JobStatus; followUpDate: string; notes: string
}
const blank = (): FormState => ({ company: '', title: '', location: '', jobUrl: '', dateApplied: todayISO(), resumeVersion: '', referral: '', status: 'APPLIED', followUpDate: '', notes: '' })
const fromJob = (j: Job): FormState => ({
  company: j.company, title: j.title, location: j.location ?? '', jobUrl: j.jobUrl ?? '', dateApplied: j.dateApplied,
  resumeVersion: j.resumeVersion ?? '', referral: j.referral ?? '', status: j.status, followUpDate: j.followUpDate ?? '', notes: j.notes ?? '',
})

function JobForm({ editing, onClose, onSaved }: { editing: Job | null; onClose: () => void; onSaved: () => void }) {
  const toast = useToast()
  const { v, set } = useForm<FormState>(editing ? fromJob(editing) : blank())
  const [busy, setBusy] = useState(false)
  async function save() {
    setBusy(true)
    const body = { ...v, location: v.location || null, jobUrl: v.jobUrl || null, resumeVersion: v.resumeVersion || null, referral: v.referral || null, followUpDate: v.followUpDate || null, notes: v.notes || null }
    try {
      if (editing) await api.put(`/api/jobs/${editing.id}`, body); else await api.post('/api/jobs', body)
      toast.ok(editing ? 'Application updated' : 'Application added'); onSaved(); onClose()
    } catch (e) { toast.err(e) } finally { setBusy(false) }
  }
  return (
    <div className="space-y-3">
      <div className="grid gap-3 sm:grid-cols-2">
        <TextField label="Company" value={v.company} onChange={(e) => set('company', e.target.value)} />
        <TextField label="Job title" value={v.title} onChange={(e) => set('title', e.target.value)} />
      </div>
      <div className="grid gap-3 sm:grid-cols-2">
        <TextField label="Location" value={v.location} onChange={(e) => set('location', e.target.value)} />
        <TextField label="Job URL" type="url" value={v.jobUrl} onChange={(e) => set('jobUrl', e.target.value)} />
      </div>
      <div className="grid gap-3 sm:grid-cols-3">
        <TextField label="Date applied" type="date" value={v.dateApplied} onChange={(e) => set('dateApplied', e.target.value)} />
        <SelectField label="Status" value={v.status} onChange={(e) => set('status', e.target.value as JobStatus)} options={STATUSES.map((s) => ({ value: s, label: JOB_STATUS_LABEL[s] }))} />
        <TextField label="Follow-up date" type="date" value={v.followUpDate} onChange={(e) => set('followUpDate', e.target.value)} />
      </div>
      <div className="grid gap-3 sm:grid-cols-2">
        <TextField label="Resume version" value={v.resumeVersion} onChange={(e) => set('resumeVersion', e.target.value)} />
        <TextField label="Referral" value={v.referral} onChange={(e) => set('referral', e.target.value)} />
      </div>
      <TextArea label="Notes" value={v.notes} onChange={(e) => set('notes', e.target.value)} />
      <div className="flex justify-end gap-2 pt-1"><Button variant="ghost" onClick={onClose}>Cancel</Button><Button variant="primary" busy={busy} disabled={!v.company.trim() || !v.title.trim()} onClick={save}>{editing ? 'Save changes' : 'Add application'}</Button></div>
    </div>
  )
}

export function JobsPage() {
  const toast = useToast()
  const [filters, setFilters] = useState({ company: '', role: '', status: '', location: '' })
  const [showFilters, setShowFilters] = useState(false)
  const query = qs({ ...filters, status: filters.status || undefined })
  const { data, loading, error, reload } = useApi<JobList>(`/api/jobs${query}`)
  const [modal, setModal] = useState<Job | 'new' | null>(null)

  async function remove(j: Job) { try { await api.del(`/api/jobs/${j.id}`); void reload() } catch (e) { toast.err(e) } }

  return (
    <div>
      <PageHeader title="Job Applications" subtitle="Track every application from saved to offer." art={<Object3D kind="briefcase" size={80} depth={10} />}
        actions={<><Button size="sm" onClick={() => setShowFilters((s) => !s)}><Filter size={14} /> Filters</Button>
          <Button variant="primary" onClick={() => setModal('new')}><Plus size={14} /> Add application</Button></>} />

      {loading && !data && <Spinner />}
      {error && !data && <ErrorNote message={error} onRetry={reload} />}
      {data && (
        <div className="space-y-5">
          <Card>
            <div className="mb-4 grid grid-cols-2 gap-4 sm:grid-cols-4">
              <Stat label="Today" value={`${data.stats.appliedToday} / ${data.stats.dailyTarget}`} />
              <Stat label="This week" value={`${data.stats.appliedThisWeek} / ${data.stats.weeklyTarget}`} />
              <Stat label="Responses" value={`${data.stats.responses} (${data.stats.responseRate}%)`} />
              <Stat label="Follow-ups due" value={data.stats.followUpsDue} />
            </div>
            <div className="mb-1 flex items-center justify-between text-xs text-ink-500"><span>Today's progress</span><span>{data.stats.appliedToday} / {data.stats.dailyTarget}</span></div>
            <Bar value={(data.stats.appliedToday / Math.max(1, data.stats.dailyTarget)) * 100} />
          </Card>

          {showFilters && (
            <Card className="grid gap-3 sm:grid-cols-5">
              <TextField label="Company" value={filters.company} onChange={(e) => setFilters({ ...filters, company: e.target.value })} />
              <TextField label="Role" value={filters.role} onChange={(e) => setFilters({ ...filters, role: e.target.value })} />
              <TextField label="Location" value={filters.location} onChange={(e) => setFilters({ ...filters, location: e.target.value })} />
              <SelectField label="Status" value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                options={[{ value: '', label: 'Any' }, ...STATUSES.map((s) => ({ value: s, label: JOB_STATUS_LABEL[s] }))]} />
              <div className="flex items-end"><Button variant="ghost" onClick={() => setFilters({ company: '', role: '', status: '', location: '' })}>Clear</Button></div>
            </Card>
          )}

          <Card>
            {data.jobs.length === 0 ? (
              <EmptyState title="No applications yet" body="Add your first application to start tracking your job search." action={<Button variant="primary" onClick={() => setModal('new')}>Add application</Button>} />
            ) : (
              <ul className="divide-y divide-oat-300/50">
                {data.jobs.map((j) => (
                  <li key={j.id} className="flex flex-wrap items-center justify-between gap-3 py-3">
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="font-medium text-ink-900">{j.company}</span><span className="text-ink-500">· {j.title}</span>
                        {j.jobUrl && <a href={j.jobUrl} target="_blank" rel="noreferrer" aria-label={`Open ${j.company} listing`} className="text-dusk-500"><ExternalLink size={13} /></a>}
                        {j.demo && <Badge>sample</Badge>}
                      </div>
                      <p className="text-xs text-ink-500">{j.location ? `${j.location} · ` : ''}{fmtDate(j.dateApplied)}{j.followUpDate ? ` · follow up ${fmtDate(j.followUpDate)}` : ''}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge tone={STATUS_TONE[j.status]}>{JOB_STATUS_LABEL[j.status]}</Badge>
                      <IconButton label={`Edit ${j.company}`} onClick={() => setModal(j)}><Pencil size={15} /></IconButton>
                      <ConfirmButton label={`Delete ${j.company} application`} onConfirm={() => remove(j)}><Trash2 size={15} /></ConfirmButton>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>
      )}
      <Modal open={modal !== null} title={modal === 'new' ? 'Add application' : 'Edit application'} onClose={() => setModal(null)} wide>
        {modal !== null && <JobForm key={modal === 'new' ? 'new' : modal.id} editing={modal === 'new' ? null : modal} onClose={() => setModal(null)} onSaved={reload} />}
      </Modal>
    </div>
  )
}
