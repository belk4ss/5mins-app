import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './auth.service';
import { AttemptResult, Lesson, Progress, Quiz, Streak, Subject, SubjectDetail } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

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
}
