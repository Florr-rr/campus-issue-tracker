package issuetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 20) String phone,
        @NotBlank @Size(min = 8, max = 72) String password,
        String role
) {
}