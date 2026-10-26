export interface AuthResponse { token: string; name: string; email: string; role: string; }
export interface Subject { id: number; title: string; description: string; lessonCount: number; }
export interface LessonSummary { id: number; title: string; durationMinutes: number; hasQuiz: boolean; }
export interface SubjectDetail { id: number; title: string; description: string; lessons: LessonSummary[]; }
export interface Lesson { id: number; title: string; content: string; durationMinutes: number; quizId: number | null; subjectId: number; }
export interface Answer { id: number; text: string; }
export interface AdminAnswer extends Answer { correct: boolean; explanation: string | null; }
export interface AdminQuestion { id: number; text: string; answers: AdminAnswer[]; }
export interface AdminQuiz { id: number; title: string; questions: AdminQuestion[]; }
export interface QuestionCorrection { questionId: number; question: string; selectedAnswer: string; correctAnswer: string; explanation: string; correct: boolean; }
export interface DashboardData { globalPercentage: number; nextLesson: Lesson | null; lastAttempt: AttemptResult | null; }
export interface Question { id: number; text: string; answers: Answer[]; }
export interface Quiz { id: number; title: string; questions: Question[]; }
export interface AttemptResult { quizId: number; quizTitle: string; score: number; total: number; takenAt: string; corrections?: QuestionCorrection[]; }
export interface Progress { subjectId: number; subjectTitle: string; completedLessons: number; totalLessons: number; percentage: number; }
export interface Streak { currentStreak: number; longestStreak: number; lastActiveDate: string | null; }
