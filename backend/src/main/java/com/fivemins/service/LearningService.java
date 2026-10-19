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
        if (r.questions() == null || r.questions().size() < 3 || r.questions().size() > 5)
            throw bad("Un quiz doit contenir entre 3 et 5 questions");
        Quiz q = new Quiz();
        q.setTitle(r.title());
        for (CreateQuestion cq : r.questions()) {
            if (cq.answers() == null || cq.answers().size() < 2) throw bad("Chaque question doit avoir au moins 2 réponses");
            if (cq.answers().stream().noneMatch(CreateAnswer::correct)) throw bad("Chaque question doit avoir une bonne réponse");
            Question qu = new Question();
            qu.setText(cq.text());
            for (CreateAnswer ca : cq.answers()) {
                Answer a = new Answer();
                a.setText(ca.text());
                a.setCorrect(ca.correct());
                qu.getAnswers().add(a);
            }
            q.getQuestions().add(qu);
        }
        l.setQuiz(q);
        lessons.save(l);
        return toDto(l.getQuiz());
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
        for (Question qu : q.getQuestions()) {
            Long chosen = req.answers() == null ? null : req.answers().get(qu.getId());
            if (chosen != null && qu.getAnswers().stream().anyMatch(a -> a.getId().equals(chosen) && a.isCorrect())) score++;
        }
        QuizAttempt at = new QuizAttempt();
        at.setUser(u);
        at.setQuiz(q);
        at.setScore(score);
        at.setTotal(q.getQuestions().size());
        at.setTakenAt(LocalDateTime.now());
        attempts.save(at);
        touchStreak(u);
        return new AttemptResult(q.getId(), q.getTitle(), at.getScore(), at.getTotal(), at.getTakenAt());
    }

    @Transactional(readOnly = true)
    public List<ProgressDto> progress(String email) {
        User u = user(email);
        return subjects.findAll().stream().map(s -> {
            Progress p = progresses.findByUserAndSubject(u, s).orElse(null);
            return new ProgressDto(s.getId(), s.getTitle(), p == null ? 0 : p.getCompletedLessons(),
                    s.getLessons().size(), p == null ? 0 : p.getPercentage());
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
                .map(a -> new AttemptResult(a.getQuiz().getId(), a.getQuiz().getTitle(), a.getScore(), a.getTotal(), a.getTakenAt())).toList();
    }

    // ---------- Règles internes ----------
    private void updateProgress(User u, Subject s) {
        long done = sessions.countDistinctLessons(u, s);
        int total = s.getLessons().size();
        Progress p = progresses.findByUserAndSubject(u, s).orElseGet(() -> {
            Progress n = new Progress();
            n.setUser(u);
            n.setSubject(s);
            return n;
        });
        p.setCompletedLessons((int) done);
        p.setPercentage(total == 0 ? 0 : done * 100.0 / total);
        progresses.save(p);
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
