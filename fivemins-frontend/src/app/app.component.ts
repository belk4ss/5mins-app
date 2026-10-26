import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="nav">
      <a routerLink="/" class="brand" aria-label="5MINUTES - accueil">
        <span class="brand-mark">5</span><span class="brand-name">MINUTES<small>APPRENDRE, CINQ MINUTES À LA FOIS</small></span>
      </a>
      @if (auth.user(); as u) {
        <nav>
          <a routerLink="/subjects">Explorer</a>
          <a routerLink="/dashboard">Mon parcours</a>
          @if (u.role === 'ADMIN') { <a routerLink="/admin">Gestion</a> }
          <span class="muted">{{ u.name }}</span>
          <button class="btn small" (click)="auth.logout()">Déconnexion</button>
        </nav>
      }
    </header>
    <main class="container"><router-outlet /></main>
    <footer class="site-footer">
      <div class="footer-school"><img src="/assets/emi-logo.png" alt="Logo de l'École Mohammadia d'Ingénieurs" /><span>Un projet étudiant · École Mohammadia d’Ingénieurs</span></div>
      <span class="footer-note">Petits pas. Vraies compétences.</span>
    </footer>
  `
})
export class AppComponent {
  auth = inject(AuthService);
}
