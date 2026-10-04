package issuetracker.messaging;

public record IssueEvent(
        String type,          // CREATED, ASSIGNED, STATUS_CHANGED
        Long issueId,
        String issueCode,
        String title,
        String status,
        String recipientEmail,
        String recipientPhone,
        String message
) {
}