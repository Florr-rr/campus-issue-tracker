package issuetracker.repository;

import issuetracker.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findByIssue_IdAndCurrentTrue(Long issueId);
    List<Assignment> findByAssignedTo_IdAndCurrentTrue(Long userId);
}