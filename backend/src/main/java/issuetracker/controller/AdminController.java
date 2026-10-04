package issuetracker.controller;

import issuetracker.dto.AssignRoleRequest;
import issuetracker.dto.UserResponse;
import issuetracker.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return adminService.listUsers();
    }

    @PutMapping("/users/{id}/role")
    public UserResponse assignRole(@PathVariable Long id,
                                   @Valid @RequestBody AssignRoleRequest request) {
        return adminService.assignRole(id, request.role());
    }
}