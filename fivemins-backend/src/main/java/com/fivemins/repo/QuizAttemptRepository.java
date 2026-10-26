package com.fivemins.repo;
import com.fivemins.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserOrderByTakenAtDesc(User user);
    List<QuizAttempt> findByUser(User user);
    @Modifying @Query("delete from QuizAttempt a where a.quiz.id = :quizId")
    void deleteAllByQuizId(@Param("quizId") Long quizId);
}
