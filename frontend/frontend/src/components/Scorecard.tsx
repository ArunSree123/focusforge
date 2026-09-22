import { Check, Circle } from 'lucide-react'
import { Bar, Card } from './ui'
import { hm, KEY_LABEL } from '../lib/format'
import type { DayScore } from '../lib/types'

/** End-of-day scorecard, computed by the backend; this component only presents it. */
export function Scorecard({ score }: { score: DayScore }) {
  return (
    <Card aria-label="Daily scorecard">
      <h2 className="mb-4 text-xl">Today's scorecard</h2>
      <ul className="space-y-2.5">
        {score.items.map((i) => {
          const label = i.type === 'COUNT' ? i.title : (KEY_LABEL[i.key] ?? i.title)
          return (
            <li key={`${i.key}-${i.routineItemId}`} className="flex items-center justify-between gap-3 text-sm">
              <span className="text-ink-700">{label}</span>
              {i.type === 'COUNT' && !i.completed
                ? <span className="tabular-nums text-ink-500">{i.actualCount} / {i.targetCount}</span>
                : i.completed
                  ? <Check size={18} className="text-moss-500" aria-label="Complete" />
                  : <Circle size={16} className="text-oat-400" aria-label="Not complete" />}
            </li>
          )
        })}
      </ul>
      <div className="mt-5 border-t border-oat-300/60 pt-4">
        <div className="mb-2 flex items-baseline justify-between"><span className="text-xs text-ink-500">Overall completion</span><span className="font-display text-2xl text-ink-900">{score.overallPercent}%</span></div>
        <Bar value={score.overallPercent} label="Overall completion" />
      </div>
      <dl className="mt-5 grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
        {[['Study time', hm(score.studyMinutes)], ['Problems solved', score.problemsSolved], ['Jobs applied', score.jobsApplied],
          ['AWS topics completed', score.awsTopicsCompleted], ['Gym', hm(score.gymMinutes)]].map(([k, v]) => (
          <div key={k as string} className="flex justify-between gap-2"><dt className="text-ink-500">{k}</dt><dd className="font-medium text-ink-900">{v}</dd></div>
        ))}
      </dl>
    </Card>
  )
}
