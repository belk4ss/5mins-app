import { Component, inject, signal } from '@angular/core';
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
        <div class="row"><h2>{{ l.title }}</h2><span class="muted">Durée indicative : {{ l.durationMinutes }} min</span></div>
        <div class="content">{{ l.content }}</div>
        @if (l.quizId) {
          <div class="lesson-quiz-action">
            <p class="muted">Tu peux passer le quiz quand tu te sens prêt. Il n'est pas nécessaire d'attendre la fin des cinq minutes.</p>
            <a class="btn" [routerLink]="['/quizzes', l.quizId]">Passer le quiz</a>
          </div>
        }
      </div>
    }
  `
})
export class LessonComponent {
  private api = inject(ApiService);
  lesson = signal<Lesson | null>(null);
  private sessionRequested = false;

  constructor() {
    const id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
    this.api.lesson(id).subscribe(l => {
      this.lesson.set(l);
      // La lecture est enregistrée dès l'ouverture. Le quiz reste accessible
      // immédiatement : la durée est indicative et ne constitue pas un verrou.
      if (!this.sessionRequested) {
        this.sessionRequested = true;
        this.api.startSession(id).subscribe({ error: () => { this.sessionRequested = false; } });
      }
    });
  }
}
