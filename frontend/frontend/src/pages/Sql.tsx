import { Object3D } from '../components/Objects3D'
import { ProblemTracker } from '../components/ProblemTracker'
import { TopicBoard } from '../components/TopicBoard'
import { Card, ErrorNote, PageHeader, Spinner } from '../components/ui'
import { useApi } from '../lib/hooks'
import type { TopicList } from '../lib/types'

const SQL_TOPICS = ['SELECT', 'WHERE', 'GROUP BY', 'HAVING', 'JOIN', 'SELF JOIN', 'Subqueries', 'CTE', 'Window Functions', 'Aggregations', 'Indexes', 'Transactions', 'Normalization']

export function SqlPage() {
  const { data, loading, error, reload } = useApi<TopicList>('/api/sql/topics')
  return (
    <div>
      <PageHeader title="SQL & MySQL" subtitle="Core SQL concepts and MySQL problem practice." art={<Object3D kind="database" size={80} depth={10} />} />
      <div className="space-y-5">
        <Card><h2 className="mb-3 text-xl">SQL learning</h2>
          {loading && !data && <Spinner />}{error && !data && <ErrorNote message={error} onRetry={reload} />}
          {data && <TopicBoard area="sql" data={data} onChanged={reload} />}
        </Card>
        <Card><h2 className="mb-3 text-xl">MySQL problems</h2><ProblemTracker area="sql" title="MySQL problems" topics={SQL_TOPICS} /></Card>
      </div>
    </div>
  )
}
