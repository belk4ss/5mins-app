import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AttemptResult, Quiz } from '../core/models';

@Component({
  selector: 'app-quiz',
  standalone: true,
  imports: [RouterLink],
  template: `
    @if (quiz(); as q) {
      <h2>{{ q.title }}</h2>
      @if (!result()) {
        @for (question of q.questions; track question.id) {
          <div class="card">
            <b>{{ $index + 1 }}. {{ question.text }}</b>
            @for (a of question.answers; track a.id) {
              <label class="option">
                <input type="radio" [name]="'q' + question.id"
                       [checked]="selected()[question.id] === a.id" (change)="choose(question.id, a.id)" />
                {{ a.text }}
              </label>
            }
          </div>
        }
        <button class="btn" [disabled]="!complete()" (click)="submit()">Valider</button>
      } @else {
        <div class="card center">
          <h1>{{ result()!.score }} / {{ result()!.total }}</h1>
          <p>{{ result()!.score === result()!.total ? '🎉 Parfait !' : 'Continuez, vous progressez !' }}</p>
          <a class="btn" routerLink="/dashboard">Voir mon progrès</a>
        </div>
      }
    }
  `
})
export class QuizComponent {
  private api = inject(ApiService);
  private id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
  quiz = signal<Quiz | null>(null);
  selected = signal<Record<number, number>>({});
  result = signal<AttemptResult | null>(null);
  complete = computed(() => {
    const q = this.quiz();
    return !!q && q.questions.every(x => this.selected()[x.id] !== undefined);
  });

  constructor() { this.api.quiz(this.id).subscribe(q => this.quiz.set(q)); }

  choose(questionId: number, answerId: number) {
    this.selected.update(s => ({ ...s, [questionId]: answerId }));
  }
  submit() { this.api.submitQuiz(this.id, this.selected()).subscribe(r => this.result.set(r)); }
}
