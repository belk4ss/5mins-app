package com.fivemins.repo;
import com.fivemins.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    java.util.List<StudySession> findByUser(User user);
    @Query("select count(distinct s.lesson.id) from StudySession s where s.user = :user and s.lesson.subject = :subject")
    long countDistinctLessons(@Param("user") User user, @Param("subject") Subject subject);
    @Modifying @Query("delete from StudySession s where s.lesson.id = :lessonId")
    void deleteAllByLessonId(@Param("lessonId") Long lessonId);
}
