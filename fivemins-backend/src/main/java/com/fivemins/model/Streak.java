package com.fivemins.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class Streak {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false) private User user;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActiveDate;
}
