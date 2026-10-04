package issuetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStatusRequest(
        @NotBlank String status,
        @Size(max = 2000) String comment
) {
}