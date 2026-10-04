package issuetracker.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "issue_activity")
public class ActivityEvent {

    @Id
    private String id;
    private Long issueId;
    private String type;          // CREATED, ASSIGNED, STATUS_CHANGED, COMMENT
    private Long actorId;
    private String actorEmail;
    private Instant timestamp;
    private Map<String, Object> details;

    public ActivityEvent() {
    }

    public ActivityEvent(Long issueId, String type, Long actorId, String actorEmail,
                         Map<String, Object> details) {
        this.issueId = issueId;
        this.type = type;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.timestamp = Instant.now();
        this.details = details;
    }

    public String getId() { return id; }
    public Long getIssueId() { return issueId; }
    public String getType() { return type; }
    public Long getActorId() { return actorId; }
    public String getActorEmail() { return actorEmail; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, Object> getDetails() { return details; }
}