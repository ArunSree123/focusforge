import { useState } from 'react'
import { BarChart, Bar as RBar, CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Object3D } from '../components/Objects3D'
import { Card, ErrorNote, PageHeader, Segmented, Spinner, Stat } from '../components/ui'
import { CATEGORY_COLOR, CATEGORY_LABEL, fmtDate, hm, hours } from '../lib/format'
import { useApi } from '../lib/hooks'
import type { MonthlyAnalytics, WeeklyReport } from '../lib/types'

const TRACKS = ['JAVA', 'DSA', 'SQL', 'AWS', 'PROBLEM_SOLVING'] as const

export function AnalyticsPage() {
  const [range, setRange] = useState<'weekly' | 'monthly'>('weekly')
  const weekly = useApi<WeeklyReport>(range === 'weekly' ? '/api/analytics/weekly' : null)
  const monthly = useApi<MonthlyAnalytics>(range === 'monthly' ? '/api/analytics/monthly' : null)
  const data = range === 'weekly' ? weekly : monthly
  const w = weekly.data, m = monthly.data

  const dayData = range === 'weekly'
    ? (w?.days ?? []).map((d) => ({ label: fmtDate(d.date, { weekday: 'short' }), ...Object.fromEntries(TRACKS.map((t) => [t, hours(d.minutesByCategory[t] ?? 0)])), study: hours(d.studyMinutes) }))
    : (m?.days ?? []).map((d) => ({ label: fmtDate(d.date), study: hours(d.studyMinutes), gym: hours(d.gymMinutes), completion: d.completionPct }))

  return (
    <div>
      <PageHeader title="Analytics" subtitle="Your numbers, charted." art={<Object3D kind="laptop" size={80} depth={10} />}
        actions={<Segmented label="Range" value={range} onChange={setRange} options={[{ value: 'weekly', label: 'This week' }, { value: 'monthly', label: 'This month' }]} />} />

      {data.loading && !data.data && <Spinner />}
      {data.error && !data.data && <ErrorNote message={data.error} onRetry={data.reload} />}

      {range === 'weekly' && w && (
        <div className="space-y-5">
          <Card className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <Stat label="Study hours" value={hm(w.overall.totalStudyMinutes)} />
            <Stat label="Problems solved" value={w.dsa.solved + w.sql.solved + w.java.solved + w.problemSolving.solved} />
            <Stat label="Job applications" value={w.career.applications} />
            <Stat label="Gym hours" value={hm(w.fitness.gymMinutes)} />
          </Card>
          <Card>
            <h2 className="mb-3 text-xl">Target vs actual</h2>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={w.learning.map((c) => ({ label: c.label, target: hours(c.targetMinutes), actual: hours(c.actualMinutes) }))}>
                <CartesianGrid strokeDasharray="3 3" stroke="#EDE6D8" /><XAxis dataKey="label" tick={{ fontSize: 11 }} /><YAxis tick={{ fontSize: 11 }} unit="h" />
                <Tooltip contentStyle={{ borderRadius: 12, border: '1px solid #DED4C1' }} /><Legend />
                <RBar dataKey="target" name="Target (h)" fill="#DED4C1" radius={[6, 6, 0, 0]} />
                <RBar dataKey="actual" name="Actual (h)" fill="#4E7B66" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
          <Card>
            <h2 className="mb-3 text-xl">Study hours by day</h2>
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={dayData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#EDE6D8" /><XAxis dataKey="label" tick={{ fontSize: 11 }} /><YAxis tick={{ fontSize: 11 }} unit="h" />
                <Tooltip contentStyle={{ borderRadius: 12, border: '1px solid #DED4C1' }} /><Legend />
                {TRACKS.map((t) => <Line key={t} type="monotone" dataKey={t} name={CATEGORY_LABEL[t]} stroke={CATEGORY_COLOR[t]} strokeWidth={2} dot={false} />)}
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </div>
      )}

      {range === 'monthly' && m && (
        <div className="space-y-5">
          <Card className="grid grid-cols-2 gap-4 sm:grid-cols-5">
            <Stat label="Study time" value={hm(m.totalStudyMinutes)} />
            <Stat label="Gym time" value={hm(m.totalGymMinutes)} />
            <Stat label="Applications" value={m.totalApplications} />
            <Stat label="Problems solved" value={m.totalProblemsSolved} />
            <Stat label="Active days" value={m.activeDays} />
          </Card>
          <Card>
            <h2 className="mb-3 text-xl">{m.month} trend</h2>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={dayData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#EDE6D8" /><XAxis dataKey="label" tick={{ fontSize: 10 }} interval={2} /><YAxis tick={{ fontSize: 11 }} />
                <Tooltip contentStyle={{ borderRadius: 12, border: '1px solid #DED4C1' }} /><Legend />
                <Line type="monotone" dataKey="study" name="Study (h)" stroke="#4E7B66" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="gym" name="Gym (h)" stroke="#8A6F5A" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="completion" name="Completion %" stroke="#C98F1F" strokeWidth={2} dot={false} yAxisId={0} />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </div>
      )}
    </div>
  )
}
