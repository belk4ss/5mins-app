import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AttemptResult, DashboardData, Progress, Streak } from '../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe, DecimalPipe, RouterLink],
  template: `
    <h2>Mon progrès</h2>
    @if (dashboard(); as d) {
      <div class="card">
        <h3>Progression globale</h3><div class="row"><strong>{{ d.globalPercentage | number:'1.0-0' }} %</strong><span>leçons terminées selon les quiz soumis</span></div>
        <div class="bar"><div class="fill" [style.width.%]="d.globalPercentage"></div></div>
        @if (d.nextLesson; as next) {
          <h3>Prochaine leçon recommandée</h3><p>{{ next.title }} · {{ next.durationMinutes }} min</p>
          <a class="btn" [routerLink]="['/lessons', next.id]">Continuer l’apprentissage</a>
        } @else { <p>Félicitations, toutes les leçons sont terminées.</p> }
        @if (d.lastAttempt; as last) { <p>Dernier score : <strong>{{ last.score }}/{{ last.total }}</strong> — {{ last.quizTitle }}</p> }
      </div>
    }
    @if (streak(); as s) {
      <div class="streak"><b>{{ s.currentStreak }}</b> jour(s) d'affilée · record {{ s.longestStreak }}</div>
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
  dashboard = signal<DashboardData | null>(null);
  progress = signal<Progress[]>([]);
  streak = signal<Streak | null>(null);
  attempts = signal<AttemptResult[]>([]);

  constructor() {
    this.api.dashboard().subscribe(d => this.dashboard.set(d));
    this.api.progress().subscribe(p => this.progress.set(p));
    this.api.streak().subscribe(s => this.streak.set(s));
    this.api.attempts().subscribe(a => this.attempts.set(a));
  }
}
