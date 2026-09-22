import { useState } from 'react'
import { ArrowDown, ArrowRight, ArrowUp, Download, Sparkles } from 'lucide-react'
import { Object3D } from '../components/Objects3D'
import { Badge, Button, Card, ErrorNote, PageHeader, Segmented, Spinner, Stat, TextField } from '../components/ui'
import { api } from '../lib/api'
import { addDays, fmtDate, hm, mondayOf, todayISO } from '../lib/format'
import { useApi } from '../lib/hooks'
import { useToast } from '../lib/toast'
import type { Insights, NextWeekPlan, PlanRow, WeeklyReport } from '../lib/types'

function ComparisonRow({ c }: { c: WeeklyReport['comparison'][number] }) {
  const Icon = c.changePct == null || c.changePct === 0 ? ArrowRight : c.changePct > 0 ? ArrowUp : ArrowDown
  const tone = c.changePct == null || c.changePct === 0 ? 'text-ink-400' : c.changePct > 0 ? 'text-moss-600' : 'text-saffron-600'
  const fmt = (v: number) => (c.unit === 'minutes' ? hm(v) : v)
  return (
    <li className="flex items-center justify-between gap-3 py-2 text-sm">
      <span className="text-ink-700">{c.label}</span>
      <span className="flex items-center gap-2 tabular-nums">
        <span className="text-ink-400">{fmt(c.lastWeek)} →</span><span className="font-medium text-ink-900">{fmt(c.thisWeek)}</span>
        <Icon size={14} className={tone} aria-hidden />
      </span>
    </li>
  )
}

function InsightBlock({ title, lines }: { title: string; lines: string[] }) {
  if (!lines.length) return null
  return (<div><h3 className="mb-1.5 text-sm font-medium text-ink-900">{title}</h3><ul className="space-y-1 text-sm text-ink-700">{lines.map((l, i) => <li key={i}>• {l}</li>)}</ul></div>)
}

function PlanEditor({ plan, weekStart, onSaved }: { plan: NextWeekPlan; weekStart: string; onSaved: () => void }) {
  const toast = useToast()
  const [rows, setRows] = useState<PlanRow[]>(plan.rows)
  const [busy, setBusy] = useState(false)
  async function save() {
    setBusy(true)
    try { await api.put(`/api/reports/plan?weekStart=${weekStart}`, { rows }); toast.ok('Plan saved'); onSaved() } catch (e) { toast.err(e) } finally { setBusy(false) }
  }
  return (
    <div>
      <p className="mb-3 text-sm text-ink-500">{plan.basis}</p>
      <ul className="space-y-2">
        {rows.map((r, i) => (
          <li key={r.key} className="flex items-center justify-between gap-3">
            <span className="text-sm text-ink-700">{r.label}</span>
            {r.key === 'JOBS' ? (
              <TextField label="" aria-label={`${r.label} target`} type="number" value={r.targetCount}
                onChange={(e) => setRows(rows.map((x, idx) => (idx === i ? { ...x, targetCount: Number(e.target.value) } : x)))} className="w-24" />
            ) : (
              <div className="flex items-center gap-1.5">
                <TextField label="" aria-label={`${r.label} target minutes`} type="number" value={r.targetMinutes}
                  onChange={(e) => setRows(rows.map((x, idx) => (idx === i ? { ...x, targetMinutes: Number(e.target.value) } : x)))} className="w-24" />
                <span className="text-xs text-ink-400">min/week</span>
              </div>
            )}
          </li>
        ))}
      </ul>
      <Button variant="primary" className="mt-4" busy={busy} onClick={save}>{plan.saved ? 'Update plan' : 'Save plan'}</Button>
    </div>
  )
}

export function ReportsPage() {
  const toast = useToast()
  const [weekStart, setWeekStart] = useState(mondayOf(todayISO()))
  const report = useApi<WeeklyReport>(`/api/reports/weekly?weekStart=${weekStart}`)
  const insights = useApi<Insights>(`/api/reports/insights?weekStart=${weekStart}`)
  const nextWeekStart = addDays(weekStart, 7)
  const plan = useApi<NextWeekPlan>(`/api/reports/plan?weekStart=${nextWeekStart}`)
  const [pdfBusy, setPdfBusy] = useState(false)

  async function downloadPdf() {
    setPdfBusy(true)
    try { await api.download(`/api/reports/weekly/pdf?weekStart=${weekStart}`, `focusforge-weekly-${weekStart}.pdf`) } catch (e) { toast.err(e) } finally { setPdfBusy(false) }
  }

  const w = report.data

  return (
    <div>
      <PageHeader title="Weekly Reports" subtitle="A full picture of your week, an AI-style analysis, and next week's plan." art={<Object3D kind="rocket" size={80} depth={10} />}
        actions={<>
          <TextField label="" aria-label="Week starting" type="date" value={weekStart} onChange={(e) => setWeekStart(mondayOf(e.target.value))} className="w-auto" />
          <Segmented label="Move week" value="x" onChange={() => {}} options={[]} />
          <Button size="sm" onClick={() => setWeekStart(addDays(weekStart, -7))}>← Prev</Button>
          <Button size="sm" onClick={() => setWeekStart(addDays(weekStart, 7))}>Next →</Button>
          <Button variant="primary" busy={pdfBusy} onClick={downloadPdf}><Download size={14} /> Download PDF</Button>
        </>} />
      <p className="-mt-4 mb-5 text-sm text-ink-500">{fmtDate(weekStart)} – {fmtDate(addDays(weekStart, 6))}</p>

      {report.loading && !w && <Spinner />}
      {report.error && !w && <ErrorNote message={report.error} onRetry={report.reload} />}
      {w && !w.hasData && (
        <Card><p className="py-6 text-center text-sm text-ink-500">Nothing recorded for this week yet.</p></Card>
      )}
      {w && w.hasData && (
        <div className="space-y-5">
          <Card>
            <h2 className="mb-3 text-xl">Overview</h2>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
              <Stat label="Completion" value={`${w.overall.completionPct}%`} />
              <Stat label="Total study" value={hm(w.overall.totalStudyMinutes)} />
              <Stat label="Avg / day" value={hm(w.overall.avgStudyMinutesPerDay)} />
              <Stat label="Streak" value={`${w.overall.streak} days`} />
            </div>
          </Card>

          <Card>
            <h2 className="mb-3 text-xl">Learning breakdown</h2>
            <ul className="space-y-2">
              {w.learning.map((c) => (
                <li key={c.key} className="flex items-center justify-between text-sm">
                  <span className="text-ink-700">{c.label}</span>
                  <span className="tabular-nums text-ink-900">{hm(c.actualMinutes)} / {hm(c.targetMinutes)} <span className="text-ink-400">({c.completionPct}%)</span></span>
                </li>
              ))}
            </ul>
          </Card>

          <div className="grid gap-5 sm:grid-cols-2">
            <Card><h2 className="mb-2 text-xl">Career</h2>
              <p className="text-sm text-ink-700">Applications: <b>{w.career.applications} / {w.career.applicationTarget}</b></p>
              <p className="text-sm text-ink-700">Responses: <b>{w.career.responses}</b> ({w.career.responseRate}%)</p>
              <p className="text-sm text-ink-700">Assessments {w.career.assessments} · Interviews {w.career.interviews} · Offers {w.career.offers}</p>
            </Card>
            <Card><h2 className="mb-2 text-xl">Fitness</h2>
              <p className="text-sm text-ink-700">Gym: <b>{w.fitness.gymSessions} / {w.fitness.gymTarget}</b> ({hm(w.fitness.gymMinutes)})</p>
              <p className="text-sm text-ink-700">Walking {w.fitness.walkDays}/7 · Basketball {w.fitness.basketballDays}/7 · Active mornings {w.fitness.activeMornings}/7</p>
            </Card>
            <Card><h2 className="mb-2 text-xl">DSA</h2>
              <p className="text-sm text-ink-700">Solved: <b>{w.dsa.solved}</b> (E {w.dsa.easy} · M {w.dsa.medium} · H {w.dsa.hard})</p>
              <p className="text-sm text-ink-700">Topics learned {w.dsa.topicsLearned} · Lifetime {w.dsa.lifetimeSolved}</p>
            </Card>
            <Card><h2 className="mb-2 text-xl">AWS</h2>
              <p className="text-sm text-ink-700">Topics completed: <b>{w.aws.topicsCompleted}</b> this week · <b>{w.aws.totalCompleted} / {w.aws.totalTopics}</b> overall</p>
              <p className="text-sm text-ink-700">Hands-on tasks this week: {w.aws.handsOnCompleted}</p>
            </Card>
          </div>

          <Card>
            <h2 className="mb-1 text-xl">Week-over-week comparison</h2>
            <ul className="divide-y divide-oat-300/50">{w.comparison.map((c) => <ComparisonRow key={c.key} c={c} />)}</ul>
          </Card>

          <Card>
            <div className="mb-3 flex items-center gap-2"><Sparkles size={18} className="text-saffron-600" /><h2 className="text-xl">✨ Analyze my week</h2></div>
            {insights.loading && !insights.data && <Spinner />}
            {insights.data && !insights.data.hasEnoughData && <p className="text-sm text-ink-500">{insights.data.message}</p>}
            {insights.data?.hasEnoughData && (
              <div className="space-y-4">
                <p className="text-sm text-ink-900">{insights.data.summary}</p>
                <InsightBlock title="What went well" lines={insights.data.wentWell} />
                <InsightBlock title="What needs attention" lines={insights.data.needsAttention} />
                <InsightBlock title="Learning pattern" lines={insights.data.learningPattern} />
                <InsightBlock title="Career progress" lines={insights.data.careerProgress} />
                <InsightBlock title="Fitness consistency" lines={insights.data.fitnessConsistency} />
                <InsightBlock title="Suggested focus for next week" lines={insights.data.suggestedFocus} />
              </div>
            )}
          </Card>

          <Card>
            <div className="mb-1 flex items-center gap-2"><h2 className="text-xl">Next week's plan</h2>{plan.data?.saved && <Badge tone="moss">saved</Badge>}</div>
            {plan.loading && !plan.data && <Spinner />}
            {plan.data && <PlanEditor plan={plan.data} weekStart={nextWeekStart} onSaved={plan.reload} />}
          </Card>
        </div>
      )}
    </div>
  )
}
