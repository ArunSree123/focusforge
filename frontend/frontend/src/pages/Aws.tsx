import { Object3D } from '../components/Objects3D'
import { TopicBoard, TopicProgressBar } from '../components/TopicBoard'
import { Card, ErrorNote, PageHeader, Spinner } from '../components/ui'
import { useApi } from '../lib/hooks'
import type { TopicList } from '../lib/types'

export function AwsPage() {
  const { data, loading, error, reload } = useApi<TopicList>('/api/aws/topics')
  return (
    <div>
      <PageHeader title="AWS" subtitle="Fundamentals, IAM, compute, storage, database, API and deployment." art={<Object3D kind="cloud" size={80} depth={10} />} />
      {loading && !data && <Spinner />}
      {error && !data && <ErrorNote message={error} onRetry={reload} />}
      {data && (
        <div className="space-y-5">
          <Card><TopicProgressBar data={data} label="AWS progress" /></Card>
          <Card><h2 className="mb-3 text-xl">Topics</h2><TopicBoard area="aws" data={data} hideGroup="Hands-on" onChanged={reload} /></Card>
          <Card><h2 className="mb-1 text-xl">Hands-on activities</h2>
            <p className="mb-3 text-sm text-ink-500">Things you actually built, not just read about.</p>
            <TopicBoard area="aws" data={{ ...data, topics: data.topics.filter((t) => t.group === 'Hands-on') }} checklistGroup="Hands-on" onChanged={reload} />
          </Card>
        </div>
      )}
    </div>
  )
}
