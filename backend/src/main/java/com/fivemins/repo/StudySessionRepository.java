package com.fivemins.repo;
import com.fivemins.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    @Query("select count(distinct s.lesson.id) from StudySession s where s.user = :user and s.lesson.subject = :subject")
    long countDistinctLessons(@Param("user") User user, @Param("subject") Subject subject);
}
