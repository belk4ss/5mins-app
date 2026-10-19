package com.fivemins.web;

import com.fivemins.dto.Dtos.*;
import com.fivemins.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LearningController {
    private final LearningService service;

    // Catalogue
    @GetMapping("/subjects")            public List<SubjectDto> subjects() { return service.listSubjects(); }
    @GetMapping("/subjects/{id}")       public SubjectDetail subject(@PathVariable Long id) { return service.getSubject(id); }
    @GetMapping("/lessons/{id}")        public LessonDto lesson(@PathVariable Long id) { return service.getLesson(id); }
    @GetMapping("/quizzes/{id}")        public QuizDto quiz(@PathVariable Long id) { return service.getQuiz(id); }

    // Actions étudiant
    @PostMapping("/lessons/{id}/sessions") @ResponseStatus(HttpStatus.CREATED)
    public void startSession(@PathVariable Long id, Authentication auth) { service.startSession(auth.getName(), id); }

    @PostMapping("/quizzes/{id}/attempts")
    public AttemptResult submit(@PathVariable Long id, @RequestBody AttemptRequest req, Authentication auth) {
        return service.submit(auth.getName(), id, req);
    }

    @GetMapping("/me/progress")  public List<ProgressDto> progress(Authentication a) { return service.progress(a.getName()); }
    @GetMapping("/me/streak")    public StreakDto streak(Authentication a) { return service.streak(a.getName()); }
    @GetMapping("/me/attempts")  public List<AttemptResult> history(Authentication a) { return service.history(a.getName()); }

    // Admin
    @PreAuthorize("hasRole('ADMIN')") @PostMapping("/subjects")
    public SubjectDto createSubject(@Valid @RequestBody CreateSubject r) { return service.createSubject(r); }

    @PreAuthorize("hasRole('ADMIN')") @PostMapping("/subjects/{id}/lessons")
    public LessonDto createLesson(@PathVariable Long id, @Valid @RequestBody CreateLesson r) { return service.createLesson(id, r); }

    @PreAuthorize("hasRole('ADMIN')") @PostMapping("/lessons/{id}/quiz")
    public QuizDto createQuiz(@PathVariable Long id, @Valid @RequestBody CreateQuiz r) { return service.createQuiz(id, r); }
}
