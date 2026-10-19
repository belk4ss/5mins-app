package com.fivemins.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)   // composition 2..*
    @JoinColumn(name = "question_id")
    private List<Answer> answers = new ArrayList<>();
}
