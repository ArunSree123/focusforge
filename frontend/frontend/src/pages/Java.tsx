import { Object3D } from '../components/Objects3D'
import { ProblemTracker } from '../components/ProblemTracker'
import { TopicBoard } from '../components/TopicBoard'
import { Card, ErrorNote, PageHeader, Spinner } from '../components/ui'
import { useApi } from '../lib/hooks'
import type { TopicList } from '../lib/types'

const JAVA_TOPICS = ['OOP', 'Classes & Objects', 'Inheritance', 'Polymorphism', 'Abstraction', 'Interfaces',
  'Collections', 'Generics', 'Exception Handling', 'Streams', 'Lambda', 'Multithreading', 'JDBC', 'JVM', 'Memory management']

export function JavaPage() {
  const { data, loading, error, reload } = useApi<TopicList>('/api/java/topics')
  return (
    <div>
      <PageHeader title="Java" subtitle="Language fundamentals and hands-on problem practice." art={<Object3D kind="laptop" size={80} depth={10} />} />
      <div className="space-y-5">
        <Card><h2 className="mb-3 text-xl">Java learning</h2>
          {loading && !data && <Spinner />}{error && !data && <ErrorNote message={error} onRetry={reload} />}
          {data && <TopicBoard area="java" data={data} onChanged={reload} />}
        </Card>
        <Card><h2 className="mb-3 text-xl">Java problems</h2><ProblemTracker area="java" title="Java problems" topics={JAVA_TOPICS} /></Card>
      </div>
    </div>
  )
}
