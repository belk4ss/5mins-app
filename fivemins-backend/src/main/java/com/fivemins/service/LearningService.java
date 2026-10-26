package com.fivemins.service;

import com.fivemins.dto.Dtos.*;
import com.fivemins.model.*;
import com.fivemins.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LearningService {
    private final UserRepository users;
    private final SubjectRepository subjects;
    private final LessonRepository lessons;
    private final QuizRepository quizzes;
    private final QuizAttemptRepository attempts;
    private final ProgressRepository progresses;
    private final StreakRepository streaks;
    private final StudySessionRepository sessions;

    private ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND); }
    private ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    // ---------- Catalogue ----------
    @Transactional(readOnly = true)
    public List<SubjectDto> listSubjects() {
        return subjects.findAll().stream()
                .map(s -> new SubjectDto(s.getId(), s.getTitle(), s.getDescription(), s.getLessons().size())).toList();
    }

    @Transactional(readOnly = true)
    public SubjectDetail getSubject(Long id) {
        Subject s = subjects.findById(id).orElseThrow(this::notFound);
        return new SubjectDetail(s.getId(), s.getTitle(), s.getDescription(),
                s.getLessons().stream().map(l -> new LessonSummary(l.getId(), l.getTitle(), l.getDurationMinutes(), l.getQuiz() != null)).toList());
    }

    @Transactional(readOnly = true)
    public LessonDto getLesson(Long id) {
        Lesson l = lessons.findById(id).orElseThrow(this::notFound);
        return new LessonDto(l.getId(), l.getTitle(), l.getContent(), l.getDurationMinutes(),
                l.getQuiz() == null ? null : l.getQuiz().getId(), l.getSubject().getId());
    }

    @Transactional(readOnly = true)
    public QuizDto getQuiz(Long id) {
        return toDto(quizzes.findById(id).orElseThrow(this::notFound));
    }

    private QuizDto toDto(Quiz q) {
        return new QuizDto(q.getId(), q.getTitle(), q.getQuestions().stream().map(qu ->
                new QuestionDto(qu.getId(), qu.getText(),
                        qu.getAnswers().stream().map(a -> new AnswerDto(a.getId(), a.getText())).toList())).toList());
    }

    @Transactional(readOnly = true)
    public AdminQuizDto getAdminQuizForLesson(Long lessonId) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(this::notFound);
        Quiz q = lesson.getQuiz();
        if (q == null) throw notFound();
        return new AdminQuizDto(q.getId(), q.getTitle(), q.getQuestions().stream().map(qu ->
                new AdminQuestionDto(qu.getId(), qu.getText(), qu.getAnswers().stream().map(a ->
                        new AdminAnswerDto(a.getId(), a.getText(), a.isCorrect(), a.getExplanation())).toList())).toList());
    }

    public LessonDto updateLesson(Long lessonId, CreateLesson r) {
        Lesson l = lessons.findById(lessonId).orElseThrow(this::notFound);
        l.setTitle(r.title()); l.setContent(r.content()); l.setDurationMinutes(r.durationMinutes());
        return new LessonDto(l.getId(), l.getTitle(), l.getContent(), l.getDurationMinutes(),
                l.getQuiz() == null ? null : l.getQuiz().getId(), l.getSubject().getId());
    }

    public QuizDto updateQuizForLesson(Long lessonId, CreateQuiz r) {
        Lesson l = lessons.findById(lessonId).orElseThrow(this::notFound);
        Quiz q = l.getQuiz();
        if (q == null) throw notFound();
        validateQuiz(r);
        q.setTitle(r.title()); q.getQuestions().clear();
        for (CreateQuestion cq : r.questions()) q.getQuestions().add(buildQuestion(cq));
        quizzes.save(q);
        return toDto(q);
    }

    private void validateQuiz(CreateQuiz r) {
        if (r.questions() == null || r.questions().size() < 3 || r.questions().size() > 5)
            throw bad("Un quiz doit contenir entre 3 et 5 questions");
        for (CreateQuestion cq : r.questions()) {
            if (cq.answers() == null || cq.answers().size() < 2) throw bad("Chaque question doit avoir au moins 2 réponses");
            if (cq.answers().stream().filter(CreateAnswer::correct).count() != 1) throw bad("Chaque question doit avoir exactement une bonne réponse");
        }
    }
    private Question buildQuestion(CreateQuestion cq) {
        Question qu = new Question(); qu.setText(cq.text());
        for (CreateAnswer ca : cq.answers()) { Answer a = new Answer(); a.setText(ca.text()); a.setCorrect(ca.correct()); a.setExplanation(ca.explanation()); qu.getAnswers().add(a); }
        return qu;
    }

    // ---------- Admin ----------
    public SubjectDto createSubject(CreateSubject r) {
        Subject s = new Subject();
        s.setTitle(r.title());
        s.setDescription(r.description());
        s = subjects.save(s);
        return new SubjectDto(s.getId(), s.getTitle(), s.getDescription(), 0);
    }

    public LessonDto createLesson(Long subjectId, CreateLesson r) {
        Subject s = subjects.findById(subjectId).orElseThrow(this::notFound);
        Lesson l = new Lesson();
        l.setTitle(r.title());
        l.setContent(r.content());
        l.setDurationMinutes(r.durationMinutes());
        l.setSubject(s);
        l = lessons.save(l);
        return new LessonDto(l.getId(), l.getTitle(), l.getContent(), l.getDurationMinutes(), null, s.getId());
    }

    public QuizDto createQuiz(Long lessonId, CreateQuiz r) {
        Lesson l = lessons.findById(lessonId).orElseThrow(this::notFound);
        validateQuiz(r);
        if (l.getQuiz() != null) return updateQuizForLesson(lessonId, r);
        Quiz q = new Quiz();
        q.setTitle(r.title());
        for (CreateQuestion cq : r.questions()) q.getQuestions().add(buildQuestion(cq));
        l.setQuiz(q);
        lessons.save(l);
        return toDto(l.getQuiz());
    }

    public void deleteQuizForLesson(Long lessonId) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(this::notFound);
        Quiz quiz = lesson.getQuiz();
        if (quiz == null) throw notFound();
        attempts.deleteAllByQuizId(quiz.getId());
        lesson.setQuiz(null);
        lessons.save(lesson);
    }

    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessons.findById(lessonId).orElseThrow(this::notFound);
        if (lesson.getQuiz() != null) attempts.deleteAllByQuizId(lesson.getQuiz().getId());
        sessions.deleteAllByLessonId(lessonId);
        lessons.delete(lesson);
    }

    // ---------- Étudiant ----------
    public void startSession(String email, Long lessonId) {
        User u = user(email);
        Lesson l = lessons.findById(lessonId).orElseThrow(this::notFound);
        StudySession s = new StudySession();
        s.setUser(u);
        s.setLesson(l);
        s.setStartedAt(LocalDateTime.now());
        sessions.save(s);
        touchStreak(u);
        updateProgress(u, l.getSubject());
    }

    public AttemptResult submit(String email, Long quizId, AttemptRequest req) {
        User u = user(email);
        Quiz q = quizzes.findById(quizId).orElseThrow(this::notFound);
        int score = 0;
        List<QuestionCorrection> corrections = new java.util.ArrayList<>();
        for (Question qu : q.getQuestions()) {
            Long chosen = req.answers() == null ? null : req.answers().get(qu.getId());
            Answer selected = chosen == null ? null : qu.getAnswers().stream().filter(a -> a.getId().equals(chosen)).findFirst().orElse(null);
            Answer correct = qu.getAnswers().stream().filter(Answer::isCorrect).findFirst().orElse(null);
            boolean isCorrect = selected != null && selected.isCorrect();
            if (isCorrect) score++;
            String explanation = correct == null ? "" : (correct.getExplanation() == null || correct.getExplanation().isBlank() ? "La bonne réponse est « " + correct.getText() + " »." : correct.getExplanation());
            corrections.add(new QuestionCorrection(qu.getId(), qu.getText(), selected == null ? "Aucune réponse" : selected.getText(), correct == null ? "Réponse non définie" : correct.getText(), explanation, isCorrect));
        }
        QuizAttempt at = new QuizAttempt(); at.setUser(u); at.setQuiz(q); at.setScore(score); at.setTotal(q.getQuestions().size()); at.setTakenAt(LocalDateTime.now()); attempts.save(at);
        touchStreak(u);
        updateProgress(u, lessons.findAll().stream().filter(l -> l.getQuiz() != null && l.getQuiz().getId().equals(q.getId())).map(Lesson::getSubject).findFirst().orElse(null));
        return new AttemptResult(q.getId(), q.getTitle(), at.getScore(), at.getTotal(), at.getTakenAt(), corrections);
    }

    @Transactional(readOnly = true)
    public List<ProgressDto> progress(String email) {
        User u = user(email);
        return subjects.findAll().stream().map(s -> {
            int done = completedLessonIds(u, s).size();
            int total = (int) s.getLessons().stream().filter(l -> l.getQuiz() != null).count();
            double pct = total == 0 ? 0 : done * 100.0 / total;
            Progress p = progresses.findByUserAndSubject(u, s).orElse(null);
            return new ProgressDto(s.getId(), s.getTitle(), done, total, pct);
        }).toList();
    }

    @Transactional(readOnly = true)
    public StreakDto streak(String email) {
        return streaks.findByUser(user(email))
                .map(s -> new StreakDto(s.getCurrentStreak(), s.getLongestStreak(), s.getLastActiveDate()))
                .orElse(new StreakDto(0, 0, null));
    }

    @Transactional(readOnly = true)
    public List<AttemptResult> history(String email) {
        return attempts.findByUserOrderByTakenAtDesc(user(email)).stream()
                .map(a -> new AttemptResult(a.getQuiz().getId(), a.getQuiz().getTitle(), a.getScore(), a.getTotal(), a.getTakenAt(), List.of())).toList();
    }

    // ---------- Règles internes ----------
    private java.util.Set<Long> completedLessonIds(User u, Subject subject) {
        java.util.Set<Long> started = sessions.findByUser(u).stream().map(x -> x.getLesson()).filter(l -> l.getSubject().getId().equals(subject.getId())).map(Lesson::getId).collect(java.util.stream.Collectors.toSet());
        java.util.Set<Long> passedQuizIds = attempts.findByUser(u).stream().map(QuizAttempt::getQuiz).map(Quiz::getId).collect(java.util.stream.Collectors.toSet());
        return subject.getLessons().stream().filter(l -> l.getQuiz() != null && started.contains(l.getId()) && passedQuizIds.contains(l.getQuiz().getId())).map(Lesson::getId).collect(java.util.stream.Collectors.toSet());
    }

    private void updateProgress(User u, Subject subject) {
        if (subject == null) return;
        int done = completedLessonIds(u, subject).size();
        int total = (int) subject.getLessons().stream().filter(l -> l.getQuiz() != null).count();
        Progress p = progresses.findByUserAndSubject(u, subject).orElseGet(() -> { Progress n = new Progress(); n.setUser(u); n.setSubject(subject); return n; });
        p.setCompletedLessons(done); p.setPercentage(total == 0 ? 0 : done * 100.0 / total); progresses.save(p);
    }

    @Transactional(readOnly = true)
    public DashboardDto dashboard(String email) {
        User u = user(email);
        List<Subject> all = subjects.findAll();
        int total = all.stream().mapToInt(s -> (int) s.getLessons().stream().filter(l -> l.getQuiz() != null).count()).sum();
        int done = all.stream().mapToInt(s -> completedLessonIds(u, s).size()).sum();
        java.util.Set<Long> completed = all.stream().flatMap(s -> completedLessonIds(u, s).stream()).collect(java.util.stream.Collectors.toSet());
        Lesson next = all.stream().flatMap(s -> s.getLessons().stream()).filter(l -> l.getQuiz() != null && !completed.contains(l.getId())).findFirst().orElse(null);
        QuizAttempt last = attempts.findByUserOrderByTakenAtDesc(u).stream().findFirst().orElse(null);
        AttemptResult lastDto = last == null ? null : new AttemptResult(last.getQuiz().getId(), last.getQuiz().getTitle(), last.getScore(), last.getTotal(), last.getTakenAt(), List.of());
        LessonDto nextDto = next == null ? null : new LessonDto(next.getId(), next.getTitle(), next.getContent(), next.getDurationMinutes(), next.getQuiz() == null ? null : next.getQuiz().getId(), next.getSubject().getId());
        return new DashboardDto(total == 0 ? 0 : done * 100.0 / total, nextDto, lastDto);
    }

    private void touchStreak(User u) {
        Streak st = streaks.findByUser(u).orElseGet(() -> {
            Streak n = new Streak();
            n.setUser(u);
            return n;
        });
        LocalDate today = LocalDate.now();
        if (today.equals(st.getLastActiveDate())) return;
        if (st.getLastActiveDate() != null && st.getLastActiveDate().plusDays(1).equals(today))
            st.setCurrentStreak(st.getCurrentStreak() + 1);
        else
            st.setCurrentStreak(1);
        st.setLongestStreak(Math.max(st.getLongestStreak(), st.getCurrentStreak()));
        st.setLastActiveDate(today);
        streaks.save(st);
    }
}
