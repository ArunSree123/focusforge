import { Object3D } from '../components/Objects3D'
import { INTERVIEW_LABELS, TopicBoard, TopicProgressBar } from '../components/TopicBoard'
import { Card, ErrorNote, PageHeader, Spinner } from '../components/ui'
import { useApi } from '../lib/hooks'
import type { TopicList } from '../lib/types'

export function InterviewPage() {
  const { data, loading, error, reload } = useApi<TopicList>('/api/interview/topics')
  return (
    <div>
      <PageHeader title="Interview Preparation" subtitle="Java, SQL, DBMS, Spring Boot, REST APIs, AWS, AI/ML, projects and HR."
        art={<Object3D kind="briefcase" size={80} depth={10} />} />
      {loading && !data && <Spinner />}
      {error && !data && <ErrorNote message={error} onRetry={reload} />}
      {data && (
        <div className="space-y-5">
          <Card><TopicProgressBar data={{ ...data, completionPct: data.readinessPct }} label="Interview readiness" /></Card>
          <Card><TopicBoard area="interview" data={data} labels={INTERVIEW_LABELS} onChanged={reload} /></Card>
        </div>
      )}
    </div>
  )
}
