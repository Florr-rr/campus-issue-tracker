package issuetracker.dto;

import jakarta.validation.constraints.NotNull;

public record AssignIssueRequest(@NotNull Long assigneeId) {
}