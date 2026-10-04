package issuetracker.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignRoleRequest(@NotBlank String role) {
}