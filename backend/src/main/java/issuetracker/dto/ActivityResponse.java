package issuetracker.dto;

import java.time.Instant;
import java.util.Map;

public record ActivityResponse(
        String type,
        String actor,
        Instant timestamp,
        Map<String, Object> details
) {
}