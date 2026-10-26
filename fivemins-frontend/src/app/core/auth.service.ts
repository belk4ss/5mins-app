import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { AuthResponse } from './models';

export const API_URL = 'http://localhost:8081/api';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  user = signal<AuthResponse | null>(JSON.parse(localStorage.getItem('user') ?? 'null'));

  get token(): string | null { return this.user()?.token ?? null; }
  isLoggedIn(): boolean { return !!this.user(); }

  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, { email, password }).pipe(tap(r => this.store(r)));
  }
  register(name: string, email: string, password: string) {
    return this.http.post<AuthResponse>(`${API_URL}/auth/register`, { name, email, password }).pipe(tap(r => this.store(r)));
  }
  logout() {
    localStorage.removeItem('user');
    this.user.set(null);
    this.router.navigate(['/login']);
  }
  private store(r: AuthResponse) {
    localStorage.setItem('user', JSON.stringify(r));
    this.user.set(r);
  }
}
