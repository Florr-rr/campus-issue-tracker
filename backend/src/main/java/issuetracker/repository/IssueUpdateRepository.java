package issuetracker.repository;

import issuetracker.model.IssueUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueUpdateRepository extends JpaRepository<IssueUpdate, Long> {
    List<IssueUpdate> findByIssue_IdOrderByCreatedAtAsc(Long issueId);
}