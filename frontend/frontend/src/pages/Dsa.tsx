import { useSearchParams } from 'react-router-dom'
import { Object3D } from '../components/Objects3D'
import { ProblemTracker } from '../components/ProblemTracker'
import { TopicBoard } from '../components/TopicBoard'
import { Card, ErrorNote, PageHeader, Segmented, Spinner } from '../components/ui'
import { useApi } from '../lib/hooks'
import type { TopicList } from '../lib/types'

const DSA_TOPICS = ['Arrays', 'Strings', 'Hashing', 'Two Pointers', 'Sliding Window', 'Binary Search', 'Sorting',
  'Linked List', 'Stack', 'Queue', 'Trees', 'Graphs', 'Recursion', 'Backtracking', 'Heap', 'Greedy', 'Dynamic Programming']

export function DsaPage() {
  const [params, setParams] = useSearchParams()
  const tab = (params.get('tab') as 'dsa' | 'problem-solving') ?? 'dsa'
  const { data, loading, error, reload } = useApi<TopicList>('/api/dsa/topics')

  return (
    <div>
      <PageHeader title="DSA & Problems" subtitle="LeetCode-style problems, DSA learning topics, and reasoning practice."
        art={<Object3D kind="puzzle" size={80} depth={10} />}
        actions={<Segmented label="Section" value={tab} onChange={(v) => setParams(v === 'dsa' ? {} : { tab: v })}
          options={[{ value: 'dsa', label: 'DSA + LeetCode' }, { value: 'problem-solving', label: 'Problem solving' }]} />} />

      {tab === 'dsa' ? (
        <div className="space-y-5">
          <Card>
            <h2 className="mb-3 text-xl">Learning topics</h2>
            {loading && !data && <Spinner />}
            {error && !data && <ErrorNote message={error} onRetry={reload} />}
            {data && <TopicBoard area="dsa" data={data} onChanged={reload} />}
          </Card>
          <Card><h2 className="mb-3 text-xl">LeetCode log</h2><ProblemTracker area="dsa" title="DSA problems" topics={DSA_TOPICS} dsa /></Card>
        </div>
      ) : (
        <Card><h2 className="mb-3 text-xl">Problem-solving practice</h2>
          <p className="mb-4 text-sm text-ink-500">Logical reasoning, mathematical reasoning, pattern problems, coding logic, interview problems and analytical thinking — kept separate from DSA.</p>
          <ProblemTracker area="problem-solving" title="Problem-solving practice"
            topics={['Logical reasoning', 'Mathematical reasoning', 'Pattern problems', 'Coding logic', 'Interview problems', 'Analytical thinking']} />
        </Card>
      )}
    </div>
  )
}
