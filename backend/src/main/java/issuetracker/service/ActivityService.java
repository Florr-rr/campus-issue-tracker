package issuetracker.service;

import issuetracker.document.ActivityEvent;
import issuetracker.dto.ActivityResponse;
import issuetracker.model.User;
import issuetracker.repository.ActivityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private final ActivityEventRepository repository;

    public ActivityService(ActivityEventRepository repository) {
        this.repository = repository;
    }

    public ActivityResponse record(Long issueId, String type, User actor, Map<String, Object> details) {
        ActivityEvent event = new ActivityEvent(issueId, type, actor.getId(), actor.getEmail(), details);
        try {
            return toResponse(repository.save(event));
        } catch (RuntimeException e) {
            log.warn("Could not record {} event for issue {}: {}", type, issueId, e.getMessage());
            return toResponse(event);
        }
    }

    public List<ActivityResponse> feed(Long issueId) {
        return repository.findByIssueIdOrderByTimestampAsc(issueId).stream()
                .map(this::toResponse).toList();
    }

    private ActivityResponse toResponse(ActivityEvent e) {
        return new ActivityResponse(e.getType(), e.getActorEmail(), e.getTimestamp(), e.getDetails());
    }
}