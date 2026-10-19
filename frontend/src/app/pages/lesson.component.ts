import { Component, OnDestroy, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Lesson } from '../core/models';

@Component({
  selector: 'app-lesson',
  standalone: true,
  imports: [RouterLink],
  template: `
    @if (lesson(); as l) {
      <a [routerLink]="['/subjects', l.subjectId]">← Retour</a>
      <div class="card">
        <h2>{{ l.title }}</h2>
        <p class="muted">⏱ {{ format(remaining()) }} restant</p>
        <div class="content">{{ l.content }}</div>
        @if (l.quizId) {
          <a class="btn" [routerLink]="['/quizzes', l.quizId]">Passer le quiz →</a>
        }
      </div>
    }
  `
})
export class LessonComponent implements OnDestroy {
  private api = inject(ApiService);
  lesson = signal<Lesson | null>(null);
  remaining = signal(0);
  private timer?: ReturnType<typeof setInterval>;

  constructor() {
    const id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
    this.api.lesson(id).subscribe(l => {
      this.lesson.set(l);
      this.remaining.set(l.durationMinutes * 60);
      this.timer = setInterval(() => this.remaining.update(v => Math.max(0, v - 1)), 1000);
      this.api.startSession(id).subscribe();   // enregistre StudySession + streak + progression
    });
  }

  format(s: number) { return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`; }
  ngOnDestroy() { clearInterval(this.timer); }
}
