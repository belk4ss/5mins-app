import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Streak, Subject } from '../core/models';

@Component({
  selector: 'app-subjects',
  standalone: true,
  imports: [RouterLink],
  template: `
    @if (streak(); as s) {
      <div class="streak">Série en cours : <b>{{ s.currentStreak }}</b> jour(s) · record : {{ s.longestStreak }}</div>
    }
    <h2>Choisissez une matière</h2>
    <div class="grid">
      @for (s of subjects(); track s.id) {
        <a class="card link" [routerLink]="['/subjects', s.id]">
          <h3>{{ s.title }}</h3>
          <p class="muted">{{ s.description }}</p>
          <span class="badge">{{ s.lessonCount }} leçon(s)</span>
        </a>
      } @empty { <p class="muted">Aucune matière pour le moment.</p> }
    </div>
  `
})
export class SubjectsComponent {
  private api = inject(ApiService);
  subjects = signal<Subject[]>([]);
  streak = signal<Streak | null>(null);

  constructor() {
    this.api.subjects().subscribe(s => this.subjects.set(s));
    this.api.streak().subscribe(s => this.streak.set(s));
  }
}
