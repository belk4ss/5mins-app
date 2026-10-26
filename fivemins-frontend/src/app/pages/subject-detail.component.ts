import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { SubjectDetail } from '../core/models';

@Component({
  selector: 'app-subject-detail',
  standalone: true,
  imports: [RouterLink],
  template: `
    <a routerLink="/subjects">← Retour</a>
    @if (subject(); as s) {
      <h2>{{ s.title }}</h2>
      <p class="muted">{{ s.description }}</p>
      @for (l of s.lessons; track l.id) {
        <a class="card link row" [routerLink]="['/lessons', l.id]">
          <span>{{ $index + 1 }}. {{ l.title }}</span>
          <span>
            <span class="badge">{{ l.durationMinutes }} min</span>
            @if (l.hasQuiz) { <span class="badge alt">Quiz</span> }
          </span>
        </a>
      }
    }
  `
})
export class SubjectDetailComponent {
  subject = signal<SubjectDetail | null>(null);
  constructor() {
    const id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
    inject(ApiService).subject(id).subscribe(s => this.subject.set(s));
  }
}
