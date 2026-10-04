package issuetracker.dto;

import java.time.Instant;

public record IssueResponse(
        Long id,
        String code,
        String title,
        String description,
        String status,
        String priority,
        String category,
        String location,
        String reporter,
        String assignedTo,
        Instant createdAt,
        Instant updatedAt
) {
}