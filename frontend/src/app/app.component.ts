import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="nav">
      <a routerLink="/" class="logo">⏱ 5mins</a>
      @if (auth.user(); as u) {
        <nav>
          <a routerLink="/subjects">Matières</a>
          <a routerLink="/dashboard">Mon progrès</a>
          <span class="muted">{{ u.name }}</span>
          <button class="btn small" (click)="auth.logout()">Déconnexion</button>
        </nav>
      }
    </header>
    <main class="container"><router-outlet /></main>
  `
})
export class AppComponent {
  auth = inject(AuthService);
}
