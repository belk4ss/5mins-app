package com.fivemins.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"}))
@Getter @Setter @NoArgsConstructor
public class Progress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private User user;
    @ManyToOne(optional = false) private Subject subject;
    private int completedLessons;
    private double percentage;
}
