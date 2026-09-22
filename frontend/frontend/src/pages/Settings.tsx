import { useEffect, useState } from 'react'
import { LogOut, Sparkles, Trash2 } from 'lucide-react'
import { Object3D } from '../components/Objects3D'
import { Button, Card, ConfirmButton, ErrorNote, PageHeader, Spinner, TextField } from '../components/ui'
import { api } from '../lib/api'
import { useApi, useForm } from '../lib/hooks'
import { useAuth } from '../lib/auth'
import { useToast } from '../lib/toast'
import type { User } from '../lib/types'

export function SettingsPage() {
  const { user, logout, setUser } = useAuth()
  const toast = useToast()
  const settings = useApi<User>('/api/settings')
  const { v, set, reset } = useForm({ name: '', wakeTargetTime: '06:00' })
  const [busy, setBusy] = useState(false)
  const [demoBusy, setDemoBusy] = useState<'load' | 'clear' | null>(null)
  const demoStatus = useApi<{ active: boolean }>('/api/demo')

  useEffect(() => { if (settings.data) reset({ name: settings.data.name, wakeTargetTime: settings.data.wakeTargetTime.slice(0, 5) }) }, [settings.data, reset])

  async function save() {
    setBusy(true)
    try {
      const updated = await api.put<User>('/api/settings', { name: v.name, wakeTargetTime: v.wakeTargetTime })
      setUser(updated); toast.ok('Settings saved')
    } catch (e) { toast.err(e) } finally { setBusy(false) }
  }
  async function loadDemo() {
    setDemoBusy('load')
    try { await api.post('/api/demo'); toast.ok('Sample data loaded'); void demoStatus.reload() } catch (e) { toast.err(e) } finally { setDemoBusy(null) }
  }
  async function clearDemo() {
    setDemoBusy('clear')
    try { await api.del('/api/demo'); toast.ok('Sample data removed'); void demoStatus.reload() } catch (e) { toast.err(e) } finally { setDemoBusy(null) }
  }

  return (
    <div>
      <PageHeader title="Settings" subtitle="Your account and sample data." art={<Object3D kind="laptop" size={72} depth={8} />} />
      <div className="max-w-lg space-y-5">
        <Card>
          <h2 className="mb-3 text-xl">Profile</h2>
          {settings.loading && !settings.data && <Spinner />}
          {settings.error && !settings.data && <ErrorNote message={settings.error} onRetry={settings.reload} />}
          {settings.data && (
            <div className="space-y-3">
              <TextField label="Name" value={v.name} onChange={(e) => set('name', e.target.value)} />
              <TextField label="Email" value={user?.email ?? ''} disabled hint="Email cannot be changed here." />
              <TextField label="Wake-up target" type="time" value={v.wakeTargetTime} onChange={(e) => set('wakeTargetTime', e.target.value)} />
              <Button variant="primary" busy={busy} onClick={save}>Save changes</Button>
            </div>
          )}
        </Card>

        <Card>
          <div className="mb-2 flex items-center gap-2"><Sparkles size={18} className="text-saffron-600" /><h2 className="text-xl">Sample data</h2></div>
          <p className="mb-3 text-sm text-ink-500">Load three weeks of realistic sample activity to explore FocusForge, or remove it without touching anything you entered yourself.</p>
          <div className="flex flex-wrap gap-2">
            <Button busy={demoBusy === 'load'} onClick={loadDemo}>Load sample data</Button>
            {demoStatus.data?.active && <Button variant="danger" busy={demoBusy === 'clear'} onClick={clearDemo}><Trash2 size={14} /> Remove sample data</Button>}
          </div>
        </Card>

        <Card>
          <h2 className="mb-2 text-xl">Session</h2>
          <ConfirmButton label="Sign out" onConfirm={logout} className="!w-auto !rounded-xl !px-4 !py-2 text-sm font-medium hover:bg-rose-100 hover:text-rose-700">
            <span className="flex items-center gap-1.5"><LogOut size={15} /> Sign out</span>
          </ConfirmButton>
        </Card>
      </div>
    </div>
  )
}
