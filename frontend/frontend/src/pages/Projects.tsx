import { useState } from 'react'
import { Check, Plus, Trash2 } from 'lucide-react'
import { Object3D } from '../components/Objects3D'
import { Badge, Bar, Button, Card, ConfirmButton, EmptyState, ErrorNote, Modal, PageHeader, Spinner, TextArea, TextField } from '../components/ui'
import { api } from '../lib/api'
import { useApi, useForm } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { Project } from '../lib/types'

type FormState = Record<'name' | 'problemStatement' | 'features' | 'techStack' | 'architecture' | 'databaseDesign' | 'apiFlow' | 'challenges' | 'solutions' | 'deployment' | 'futureImprovements', string>
const FIELDS: [keyof FormState, string, boolean?][] = [
  ['name', 'Project name'], ['techStack', 'Tech stack'], ['problemStatement', 'Problem statement', true],
  ['features', 'Features', true], ['architecture', 'Architecture', true], ['databaseDesign', 'Database design', true],
  ['apiFlow', 'API flow', true], ['challenges', 'Challenges', true], ['solutions', 'Solutions', true],
  ['deployment', 'Deployment', true], ['futureImprovements', 'Future improvements', true],
]
const blank = (): FormState => ({ name: '', problemStatement: '', features: '', techStack: '', architecture: '', databaseDesign: '', apiFlow: '', challenges: '', solutions: '', deployment: '', futureImprovements: '' })
const fromProject = (p: Project): FormState => ({
  name: p.name, problemStatement: p.problemStatement ?? '', features: p.features ?? '', techStack: p.techStack ?? '',
  architecture: p.architecture ?? '', databaseDesign: p.databaseDesign ?? '', apiFlow: p.apiFlow ?? '',
  challenges: p.challenges ?? '', solutions: p.solutions ?? '', deployment: p.deployment ?? '', futureImprovements: p.futureImprovements ?? '',
})

function ProjectForm({ editing, onClose, onSaved }: { editing: Project | null; onClose: () => void; onSaved: () => void }) {
  const toast = useToast()
  const { v, set } = useForm<FormState>(editing ? fromProject(editing) : blank())
  const [busy, setBusy] = useState(false)
  async function save() {
    setBusy(true)
    try {
      if (editing) await api.put(`/api/projects/${editing.id}`, v); else await api.post('/api/projects', v)
      toast.ok(editing ? 'Project updated' : 'Project added'); onSaved(); onClose()
    } catch (e) { toast.err(e) } finally { setBusy(false) }
  }
  return (
    <div className="space-y-3">
      {FIELDS.map(([key, label, big]) =>
        big ? <TextArea key={key} label={label} value={v[key]} onChange={(e) => set(key, e.target.value)} rows={3} />
             : <TextField key={key} label={label} value={v[key]} onChange={(e) => set(key, e.target.value)} />)}
      <div className="flex justify-end gap-2 pt-1"><Button variant="ghost" onClick={onClose}>Cancel</Button><Button variant="primary" busy={busy} disabled={!v.name.trim()} onClick={save}>{editing ? 'Save changes' : 'Add project'}</Button></div>
    </div>
  )
}

export function ProjectsPage() {
  const { data, loading, error, reload } = useApi<Project[]>('/api/projects')
  const toast = useToast()
  const [modal, setModal] = useState<Project | 'new' | null>(null)
  const [open, setOpen] = useState<number | null>(null)

  async function remove(p: Project) { try { await api.del(`/api/projects/${p.id}`); void reload() } catch (e) { toast.err(e) } }

  return (
    <div>
      <PageHeader title="Projects" subtitle="Prepare every project to be explained confidently in an interview." art={<Object3D kind="server" size={80} depth={10} />}
        actions={<Button variant="primary" onClick={() => setModal('new')}><Plus size={14} /> Add project</Button>} />

      {loading && !data && <Spinner />}
      {error && !data && <ErrorNote message={error} onRetry={reload} />}
      {data && (data.length === 0 ? (
        <Card><EmptyState title="No projects yet" body="Add a project to build your interview explanation checklist." action={<Button variant="primary" onClick={() => setModal('new')}>Add project</Button>} /></Card>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2">
          {data.map((p) => (
            <Card key={p.id}>
              <div className="mb-2 flex items-start justify-between gap-2">
                <div><div className="flex items-center gap-2"><h2 className="text-lg">{p.name}</h2>{p.demo && <Badge>sample</Badge>}</div>
                  {p.techStack && <p className="text-xs text-ink-500">{p.techStack}</p>}</div>
                <ConfirmButton label={`Delete ${p.name}`} onConfirm={() => remove(p)}><Trash2 size={15} /></ConfirmButton>
              </div>
              <div className="mb-3 flex items-center justify-between text-xs text-ink-500"><span>Interview readiness</span><span className="font-medium text-ink-900">{p.readinessPct}%</span></div>
              <Bar value={p.readinessPct} className="mb-4" />
              <ul className="mb-3 grid grid-cols-2 gap-1.5 text-xs">
                {p.checklist.map((c) => (
                  <li key={c.field} className="flex items-center gap-1.5">
                    {c.done ? <Check size={13} className="text-moss-500" /> : <span className="ml-0.5 h-3 w-3 rounded-sm border border-oat-400" />}
                    <span className={c.done ? 'text-ink-700' : 'text-ink-400'}>{c.label}</span>
                  </li>
                ))}
              </ul>
              <div className="flex gap-2">
                <Button size="sm" onClick={() => setModal(p)}>Edit</Button>
                <Button size="sm" onClick={() => setOpen(open === p.id ? null : p.id)}>{open === p.id ? 'Hide details' : 'View details'}</Button>
              </div>
              {open === p.id && (
                <div className="mt-3 space-y-2 rounded-2xl bg-oat-100 p-3 text-sm text-ink-700">
                  {FIELDS.filter(([k]) => k !== 'name' && k !== 'techStack').map(([k, label]) => {
                    const val = (p as unknown as Record<string, string | null>)[k]
                    return val ? <div key={k}><p className="text-xs font-medium text-ink-500">{label}</p><p className="whitespace-pre-wrap">{val}</p></div> : null
                  })}
                </div>
              )}
            </Card>
          ))}
        </div>
      ))}
      <Modal open={modal !== null} title={modal === 'new' ? 'Add project' : 'Edit project'} onClose={() => setModal(null)} wide>
        {modal !== null && <ProjectForm key={modal === 'new' ? 'new' : modal.id} editing={modal === 'new' ? null : modal} onClose={() => setModal(null)} onSaved={reload} />}
      </Modal>
    </div>
  )
}
