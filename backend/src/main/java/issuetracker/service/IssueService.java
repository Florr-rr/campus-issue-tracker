package issuetracker.service;

import issuetracker.dto.*;
import issuetracker.model.*;
import issuetracker.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class IssueService {

    private static final Set<String> PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH", "URGENT");

    // status -> statuses it may move to
    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "ASSIGNED", Set.of("IN_PROGRESS"),
            "IN_PROGRESS", Set.of("RESOLVED"),
            "RESOLVED", Set.of("CLOSED", "REOPENED"),
            "REOPENED", Set.of("IN_PROGRESS"));

    private final IssueRepository issues;
    private final CategoryRepository categories;
    private final LocationRepository locations;
    private final UserRepository users;
    private final AssignmentRepository assignments;
    private final IssueUpdateRepository updates;
    private final ActivityService activity;

    public IssueService(IssueRepository issues, CategoryRepository categories,
                        LocationRepository locations, UserRepository users,
                        AssignmentRepository assignments, IssueUpdateRepository updates,
                        ActivityService activity) {
        this.issues = issues;
        this.categories = categories;
        this.locations = locations;
        this.users = users;
        this.assignments = assignments;
        this.updates = updates;
        this.activity = activity;
    }

    @Transactional
    public IssueResponse create(Long userId, CreateIssueRequest req) {
        User reporter = users.findById(userId).orElseThrow(() -> unauthorized());
        Category category = categories.findById(req.categoryId())
                .filter(Category::isActive)
                .orElseThrow(() -> bad("Unknown category"));
        Location location = locations.findById(req.locationId())
                .filter(Location::isActive)
                .orElseThrow(() -> bad("Unknown location"));

        String priority = (req.priority() == null || req.priority().isBlank())
                ? "MEDIUM" : req.priority().trim().toUpperCase();
        if (!PRIORITIES.contains(priority)) {
            throw bad("Priority must be LOW, MEDIUM, HIGH or URGENT");
        }

        Issue issue = new Issue();
        issue.setTitle(req.title().trim());
        issue.setDescription(req.description().trim());
        issue.setPriority(priority);
        issue.setReporter(reporter);
        issue.setCategory(category);
        issue.setLocation(location);
        issue = issues.save(issue);

        recordUpdate(issue, reporter, null, "SUBMITTED", null);

        Map<String, Object> details = new HashMap<>();
        details.put("title", issue.getTitle());
        details.put("priority", priority);
        details.put("category", category.getName());
        details.put("location", location.displayName());
        activity.record(issue.getId(), "CREATED", reporter, details);

        return toResponse(issue);
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> list(Long userId, String role) {
        List<Issue> result;
        if (seesAll(role)) {
            result = issues.findAllByOrderByCreatedAtDesc();
        } else if (role.equals("MAINTENANCE_STAFF")) {
            result = assignments.findByAssignedTo_IdAndCurrentTrue(userId).stream()
                    .map(Assignment::getIssue).toList();
        } else {
            result = issues.findByReporter_IdOrderByCreatedAtDesc(userId);
        }
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public IssueResponse get(Long userId, String role, Long issueId) {
        Issue issue = find(issueId);
        requireVisible(issue, userId, role);
        return toResponse(issue);
    }

    @Transactional
    public IssueResponse assign(Long adminId, Long issueId, Long assigneeId) {
        Issue issue = find(issueId);
        if (Set.of("RESOLVED", "CLOSED").contains(issue.getStatus())) {
            throw bad("Cannot assign an issue that is " + issue.getStatus());
        }
        User admin = users.findById(adminId).orElseThrow(() -> unauthorized());
        User assignee = users.findById(assigneeId)
                .filter(User::isActive)
                .orElseThrow(() -> bad("Assignee not found"));
        if (!"MAINTENANCE_STAFF".equals(assignee.getRole().getName())) {
            throw bad("Assignee must have the MAINTENANCE_STAFF role");
        }

        assignments.findByIssue_IdAndCurrentTrue(issueId).ifPresent(a -> {
            a.setCurrent(false);
            a.setUnassignedAt(Instant.now());
            assignments.saveAndFlush(a); // must be flushed before the new row (unique index)
        });

        Assignment next = new Assignment();
        next.setIssue(issue);
        next.setAssignedTo(assignee);
        next.setDepartment(issue.getCategory().getDefaultDepartment());
        next.setAssignedBy(admin);
        assignments.save(next);

        String old = issue.getStatus();
        issue.setStatus("ASSIGNED");
        issues.save(issue);
        recordUpdate(issue, admin, old, "ASSIGNED", "Assigned to " + assignee.getEmail());

        Map<String, Object> details = new HashMap<>();
        details.put("assignedTo", assignee.getEmail());
        details.put("from", old);
        details.put("to", "ASSIGNED");
        activity.record(issueId, "ASSIGNED", admin, details);

        return toResponse(issue);
    }

    @Transactional
    public IssueResponse changeStatus(Long userId, String role, Long issueId, UpdateStatusRequest req) {
        Issue issue = find(issueId);
        User actor = users.findById(userId).orElseThrow(() -> unauthorized());
        String from = issue.getStatus();
        String to = req.status().trim().toUpperCase();

        if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw bad("Cannot move an issue from " + from + " to " + to);
        }

        boolean admin = role.equals("ADMIN");
        boolean reporter = issue.getReporter().getId().equals(userId);
        boolean assignee = assignments.findByIssue_IdAndCurrentTrue(issueId)
                .map(a -> a.getAssignedTo() != null && a.getAssignedTo().getId().equals(userId))
                .orElse(false);

        boolean allowed = switch (to) {
            case "IN_PROGRESS", "RESOLVED" -> admin || assignee;
            case "CLOSED", "REOPENED" -> admin || reporter;
            default -> false;
        };
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to make this change");
        }

        Instant now = Instant.now();
        if (to.equals("IN_PROGRESS") && issue.getFirstResponseAt() == null) issue.setFirstResponseAt(now);
        if (to.equals("RESOLVED")) issue.setResolvedAt(now);
        if (to.equals("CLOSED")) issue.setClosedAt(now);
        if (to.equals("REOPENED")) {
            issue.setResolvedAt(null);
            issue.setClosedAt(null);
        }
        issue.setStatus(to);
        issues.save(issue);

        String comment = (req.comment() == null || req.comment().isBlank()) ? null : req.comment().trim();
        recordUpdate(issue, actor, from, to, comment);

        Map<String, Object> details = new HashMap<>();
        details.put("from", from);
        details.put("to", to);
        if (comment != null) details.put("comment", comment);
        activity.record(issueId, "STATUS_CHANGED", actor, details);

        return toResponse(issue);
    }

    @Transactional(readOnly = true)
    public ActivityResponse addComment(Long userId, String role, Long issueId, String text) {
        Issue issue = find(issueId);
        requireVisible(issue, userId, role);
        User author = users.findById(userId).orElseThrow(() -> unauthorized());
        Map<String, Object> details = new HashMap<>();
        details.put("text", text.trim());
        return activity.record(issue.getId(), "COMMENT", author, details);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> activity(Long userId, String role, Long issueId) {
        Issue issue = find(issueId);
        requireVisible(issue, userId, role);
        return activity.feed(issue.getId());
    }

    // ---- helpers ----

    private boolean seesAll(String role) {
        return role.equals("ADMIN") || role.equals("MANAGEMENT");
    }

    private void requireVisible(Issue issue, Long userId, String role) {
        boolean ok;
        if (seesAll(role)) {
            ok = true;
        } else if (role.equals("MAINTENANCE_STAFF")) {
            ok = assignments.findByIssue_IdAndCurrentTrue(issue.getId())
                    .map(a -> a.getAssignedTo() != null && a.getAssignedTo().getId().equals(userId))
                    .orElse(false);
        } else {
            ok = issue.getReporter().getId().equals(userId);
        }
        // Same error as "not found" so people cannot probe for other users' issue ids.
        if (!ok) throw notFound();
    }

    private Issue find(Long id) {
        return issues.findById(id).orElseThrow(() -> notFound());
    }

    private void recordUpdate(Issue issue, User user, String oldStatus, String newStatus, String comment) {
        IssueUpdate u = new IssueUpdate();
        u.setIssue(issue);
        u.setUser(user);
        u.setOldStatus(oldStatus);
        u.setNewStatus(newStatus);
        u.setComment(comment);
        updates.save(u);
    }

    private IssueResponse toResponse(Issue i) {
        String assignee = assignments.findByIssue_IdAndCurrentTrue(i.getId())
                .map(Assignment::getAssignedTo)
                .map(User::getEmail)
                .orElse(null);
        return new IssueResponse(i.getId(), i.getCode(), i.getTitle(), i.getDescription(),
                i.getStatus(), i.getPriority(), i.getCategory().getName(),
                i.getLocation().displayName(), i.getReporter().getEmail(), assignee,
                i.getCreatedAt(), i.getUpdatedAt());
    }

    private ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found");
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
    }
}