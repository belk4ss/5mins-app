package com.fivemins.repo;
import com.fivemins.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StreakRepository extends JpaRepository<Streak, Long> {
    Optional<Streak> findByUser(User user);
}
