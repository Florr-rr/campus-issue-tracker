package issuetracker.controller;

import issuetracker.dto.*;
import issuetracker.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService service;

    public IssueController(IssueService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponse create(@AuthenticationPrincipal Jwt jwt,
                                @Valid @RequestBody CreateIssueRequest request) {
        return service.create(userId(jwt), request);
    }

    @GetMapping
    public List<IssueResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(userId(jwt), role(jwt));
    }

    @GetMapping("/{id}")
    public IssueResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.get(userId(jwt), role(jwt), id);
    }

    @PutMapping("/{id}/status")
    public IssueResponse status(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                @Valid @RequestBody UpdateStatusRequest request) {
        return service.changeStatus(userId(jwt), role(jwt), id, request);
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public IssueResponse assign(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                @Valid @RequestBody AssignIssueRequest request) {
        return service.assign(userId(jwt), id, request.assigneeId());
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private String role(Jwt jwt) {
        return jwt.getClaimAsString("role");
    }
}