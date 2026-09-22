import { Navigate, Route, HashRouter as Router, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './lib/auth'
import { ToastProvider } from './lib/toast'
import { Layout } from './components/Layout'
import { Spinner } from './components/ui'
import { AuthPage } from './pages/Auth'
import { Dashboard } from './pages/Dashboard'
import { RoutinePage } from './pages/Routine'
import { LearningPage } from './pages/Learning'
import { DsaPage } from './pages/Dsa'
import { SqlPage } from './pages/Sql'
import { JavaPage } from './pages/Java'
import { AwsPage } from './pages/Aws'
import { JobsPage } from './pages/Jobs'
import { ProjectsPage } from './pages/Projects'
import { InterviewPage } from './pages/Interview'
import { FitnessPage } from './pages/Fitness'
import { AnalyticsPage } from './pages/Analytics'
import { ReportsPage } from './pages/Reports'
import { SettingsPage } from './pages/Settings'

function Protected({ children }: { children: React.ReactNode }) {
  const { user, ready } = useAuth()
  if (!ready) return <Spinner label="Loading FocusForge" />
  if (!user) return <Navigate to="/login" replace />
  return <>{children}</>
}

export default function App() {
  return (
    <Router>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            <Route path="/login" element={<AuthPage mode="login" />} />
            <Route path="/register" element={<AuthPage mode="register" />} />
            <Route element={<Protected><Layout /></Protected>}>
              <Route index element={<Dashboard />} />
              <Route path="routine" element={<RoutinePage />} />
              <Route path="learning" element={<LearningPage />} />
              <Route path="dsa" element={<DsaPage />} />
              <Route path="sql" element={<SqlPage />} />
              <Route path="java" element={<JavaPage />} />
              <Route path="aws" element={<AwsPage />} />
              <Route path="jobs" element={<JobsPage />} />
              <Route path="projects" element={<ProjectsPage />} />
              <Route path="interview" element={<InterviewPage />} />
              <Route path="fitness" element={<FitnessPage />} />
              <Route path="analytics" element={<AnalyticsPage />} />
              <Route path="reports" element={<ReportsPage />} />
              <Route path="settings" element={<SettingsPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </Router>
  )
}
