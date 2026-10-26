import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './auth.service';
import { AdminQuiz, AttemptResult, DashboardData, Lesson, Progress, Quiz, Streak, Subject, SubjectDetail } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

  // Étudiant
  subjects() { return this.http.get<Subject[]>(`${API_URL}/subjects`); }
  subject(id: number) { return this.http.get<SubjectDetail>(`${API_URL}/subjects/${id}`); }
  lesson(id: number) { return this.http.get<Lesson>(`${API_URL}/lessons/${id}`); }
  startSession(lessonId: number) { return this.http.post<void>(`${API_URL}/lessons/${lessonId}/sessions`, {}); }
  quiz(id: number) { return this.http.get<Quiz>(`${API_URL}/quizzes/${id}`); }
  submitQuiz(id: number, answers: Record<number, number>) {
    return this.http.post<AttemptResult>(`${API_URL}/quizzes/${id}/attempts`, { answers });
  }
  progress() { return this.http.get<Progress[]>(`${API_URL}/me/progress`); }
  streak() { return this.http.get<Streak>(`${API_URL}/me/streak`); }
  attempts() { return this.http.get<AttemptResult[]>(`${API_URL}/me/attempts`); }
  dashboard() { return this.http.get<DashboardData>(`${API_URL}/me/dashboard`); }

  // Admin
  createSubject(title: string, description: string) {
    return this.http.post<Subject>(`${API_URL}/subjects`, { title, description });
  }
  createLesson(subjectId: number, body: { title: string; content: string; durationMinutes: number }) {
    return this.http.post<Lesson>(`${API_URL}/subjects/${subjectId}/lessons`, body);
  }
  createQuiz(lessonId: number, body: NewQuiz) { return this.http.post<Quiz>(`${API_URL}/lessons/${lessonId}/quiz`, body); }
  updateLesson(lessonId: number, body: { title: string; content: string; durationMinutes: number }) { return this.http.put<Lesson>(`${API_URL}/lessons/${lessonId}`, body); }
  adminQuiz(lessonId: number) { return this.http.get<AdminQuiz>(`${API_URL}/admin/lessons/${lessonId}/quiz`); }
  updateQuiz(lessonId: number, body: NewQuiz) { return this.http.put<Quiz>(`${API_URL}/lessons/${lessonId}/quiz`, body); }
  deleteLesson(lessonId: number) { return this.http.delete<void>(`${API_URL}/lessons/${lessonId}`); }
  deleteQuiz(lessonId: number) { return this.http.delete<void>(`${API_URL}/lessons/${lessonId}/quiz`); }
}

export interface NewQuiz {
  title: string;
  questions: { text: string; answers: { text: string; correct: boolean; explanation?: string }[] }[];
}
