package com.fivemins.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class Dtos {
    private Dtos() {}

    // Auth
    public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email, @Size(min = 6) String password) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, String name, String email, String role) {}

    // Lecture
    public record SubjectDto(Long id, String title, String description, int lessonCount) {}
    public record LessonSummary(Long id, String title, int durationMinutes, boolean hasQuiz) {}
    public record SubjectDetail(Long id, String title, String description, List<LessonSummary> lessons) {}
    public record LessonDto(Long id, String title, String content, int durationMinutes, Long quizId, Long subjectId) {}
    public record AnswerDto(Long id, String text) {}                       // pas de "correct" côté client
    public record QuestionDto(Long id, String text, List<AnswerDto> answers) {}
    public record QuizDto(Long id, String title, List<QuestionDto> questions) {}
    public record AttemptRequest(Map<Long, Long> answers) {}               // questionId -> answerId
    public record AttemptResult(Long quizId, String quizTitle, int score, int total, LocalDateTime takenAt) {}
    public record ProgressDto(Long subjectId, String subjectTitle, int completedLessons, int totalLessons, double percentage) {}
    public record StreakDto(int currentStreak, int longestStreak, LocalDate lastActiveDate) {}

    // Admin
    public record CreateSubject(@NotBlank String title, String description) {}
    public record CreateLesson(@NotBlank String title, String content, @Min(1) int durationMinutes) {}
    public record CreateAnswer(@NotBlank String text, boolean correct) {}
    public record CreateQuestion(@NotBlank String text, List<CreateAnswer> answers) {}
    public record CreateQuiz(@NotBlank String title, List<CreateQuestion> questions) {}
}
