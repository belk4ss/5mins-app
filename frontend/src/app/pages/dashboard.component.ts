import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ApiService } from '../core/api.service';
import { AttemptResult, Progress, Streak } from '../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe],
  template: `
    <h2>Mon progrès</h2>
    @if (streak(); as s) {
      <div class="streak">🔥 <b>{{ s.currentStreak }}</b> jour(s) d'affilée · record {{ s.longestStreak }}</div>
    }
    <h3>Par matière</h3>
    @for (p of progress(); track p.subjectId) {
      <div class="card">
        <div class="row"><b>{{ p.subjectTitle }}</b><span>{{ p.completedLessons }}/{{ p.totalLessons }} leçons</span></div>
        <div class="bar"><div class="fill" [style.width.%]="p.percentage"></div></div>
      </div>
    }
    <h3>Derniers quiz</h3>
    @for (a of attempts(); track a.takenAt) {
      <div class="card row">
        <span>{{ a.quizTitle }}</span>
        <span><b>{{ a.score }}/{{ a.total }}</b> · {{ a.takenAt | date:'short' }}</span>
      </div>
    } @empty { <p class="muted">Aucun quiz passé.</p> }
  `
})
export class DashboardComponent {
  private api = inject(ApiService);
  progress = signal<Progress[]>([]);
  streak = signal<Streak | null>(null);
  attempts = signal<AttemptResult[]>([]);

  constructor() {
    this.api.progress().subscribe(p => this.progress.set(p));
    this.api.streak().subscribe(s => this.streak.set(s));
    this.api.attempts().subscribe(a => this.attempts.set(a));
  }
}
