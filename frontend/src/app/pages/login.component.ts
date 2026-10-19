import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="card narrow">
      <h2>Connexion</h2>
      <input placeholder="Email" type="email" [(ngModel)]="email" />
      <input placeholder="Mot de passe" type="password" [(ngModel)]="password" (keyup.enter)="submit()" />
      @if (error()) { <p class="error">{{ error() }}</p> }
      <button class="btn" (click)="submit()">Se connecter</button>
      <p class="muted">Pas de compte ? <a routerLink="/register">Créer un compte</a></p>
    </div>
  `
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  email = ''; password = '';
  error = signal('');

  submit() {
    this.auth.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/subjects']),
      error: () => this.error.set('Email ou mot de passe incorrect')
    });
  }
}
