package issuetracker.repository;

import issuetracker.document.ActivityEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActivityEventRepository extends MongoRepository<ActivityEvent, String> {
    List<ActivityEvent> findByIssueIdOrderByTimestampAsc(Long issueId);
}