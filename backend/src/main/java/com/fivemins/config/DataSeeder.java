package com.fivemins.config;

import com.fivemins.dto.Dtos.*;
import com.fivemins.model.*;
import com.fivemins.repo.UserRepository;
import com.fivemins.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

/** Données de démo : admin@5mins.app / admin123 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final LearningService service;

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@5mins.app");
        admin.setPasswordHash(encoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        users.save(admin);

        var s = service.createSubject(new CreateSubject("Java en 5 minutes", "Les bases de Java, une leçon à la fois."));
        var l1 = service.createLesson(s.id(), new CreateLesson("Variables et types",
                "En Java, chaque variable a un type : int, double, boolean, String...\nExemple : int age = 20;", 5));
        service.createLesson(s.id(), new CreateLesson("Les boucles",
                "for, while et do-while permettent de répéter des instructions.\nExemple : for (int i = 0; i < 5; i++) { ... }", 5));

        service.createQuiz(l1.id(), new CreateQuiz("Quiz : variables", List.of(
            new CreateQuestion("Quel type stocke un nombre entier ?", List.of(
                new CreateAnswer("int", true), new CreateAnswer("String", false), new CreateAnswer("boolean", false))),
            new CreateQuestion("Quel type stocke vrai/faux ?", List.of(
                new CreateAnswer("double", false), new CreateAnswer("boolean", true))),
            new CreateQuestion("String est un type pour :", List.of(
                new CreateAnswer("du texte", true), new CreateAnswer("des décimaux", false))))));
    }
}
