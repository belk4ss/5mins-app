import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="card narrow">
      <h2>Créer un compte</h2>
      <input placeholder="Nom" [(ngModel)]="name" />
      <input placeholder="Email" type="email" [(ngModel)]="email" />
      <input placeholder="Mot de passe (6 caractères min.)" type="password" [(ngModel)]="password" />
      @if (error()) { <p class="error">{{ error() }}</p> }
      <button class="btn" (click)="submit()">S'inscrire</button>
      <p class="muted">Déjà inscrit ? <a routerLink="/login">Connexion</a></p>
    </div>
  `
})
export class RegisterComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  name = ''; email = ''; password = '';
  error = signal('');

  submit() {
    this.auth.register(this.name, this.email, this.password).subscribe({
      next: () => this.router.navigate(['/subjects']),
      error: e => this.error.set(e.status === 409 ? 'Email déjà utilisé' : 'Données invalides')
    });
  }
}
