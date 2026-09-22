import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Object3D } from '../components/Objects3D'
import { Button, Card, CheckField, ErrorNote, TextField } from '../components/ui'
import { errMsg } from '../lib/api'
import { useAuth } from '../lib/auth'

export function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const { user, ready, login, register } = useAuth()
  const nav = useNavigate()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loadDemo, setLoadDemo] = useState(mode === 'register')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (ready && user) return <Navigate to="/" replace />

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true); setError(null)
    try {
      if (mode === 'login') await login(email, password); else await register(name, email, password, loadDemo)
      nav('/', { replace: true })
    } catch (e) { setError(errMsg(e)) } finally { setBusy(false) }
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-12">
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }} className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <Object3D kind="rocket" size={72} depth={8} />
          <h1 className="mt-3 text-3xl">FocusForge</h1>
          <p className="mt-1 text-sm text-ink-500">Small progress every day becomes a strong career.</p>
        </div>
        <Card>
          <h2 className="mb-4 text-xl">{mode === 'login' ? 'Welcome back' : 'Create your account'}</h2>
          <form onSubmit={submit} className="space-y-3">
            {mode === 'register' && <TextField label="Name" required value={name} onChange={(e) => setName(e.target.value)} autoComplete="name" />}
            <TextField label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
            <TextField label="Password" type="password" required minLength={8} value={password} onChange={(e) => setPassword(e.target.value)}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'} hint={mode === 'register' ? 'At least 8 characters' : undefined} />
            {mode === 'register' && <CheckField label="Load three weeks of sample data so I can explore right away" checked={loadDemo} onChange={setLoadDemo} />}
            {error && <ErrorNote message={error} />}
            <Button type="submit" variant="primary" busy={busy} className="w-full">{mode === 'login' ? 'Sign in' : 'Create account'}</Button>
          </form>
        </Card>
        <p className="mt-4 text-center text-sm text-ink-500">
          {mode === 'login' ? <>New here? <a href="/register" className="font-medium text-moss-700">Create an account</a></>
            : <>Already have an account? <a href="/login" className="font-medium text-moss-700">Sign in</a></>}
        </p>
      </motion.div>
    </div>
  )
}
