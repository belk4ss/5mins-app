package com.fivemins.repo;
import com.fivemins.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LessonRepository extends JpaRepository<Lesson, Long> {}
