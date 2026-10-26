package com.fivemins.repo;
import com.fivemins.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
public interface QuizRepository extends JpaRepository<Quiz, Long> {}
