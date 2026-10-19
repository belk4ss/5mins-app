package com.fivemins.repo;
import com.fivemins.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserOrderByTakenAtDesc(User user);
}
