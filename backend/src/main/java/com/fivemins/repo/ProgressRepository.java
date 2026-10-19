package com.fivemins.repo;
import com.fivemins.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Optional<Progress> findByUserAndSubject(User user, Subject subject);
}
