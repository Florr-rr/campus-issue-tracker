package issuetracker.repository;

import issuetracker.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findAllByOrderByCreatedAtDesc();
    List<Issue> findByReporter_IdOrderByCreatedAtDesc(Long reporterId);
}