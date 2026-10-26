package com.fivemins.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor
public class Lesson {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private int durationMinutes;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Subject subject;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)   // 0..1
    private Quiz quiz;
}
