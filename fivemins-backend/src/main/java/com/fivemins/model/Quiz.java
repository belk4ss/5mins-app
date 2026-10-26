package com.fivemins.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
public class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)   // composition 3..5
    @JoinColumn(name = "quiz_id")
    private List<Question> questions = new ArrayList<>();
}
