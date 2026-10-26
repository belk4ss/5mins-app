import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/admin.guard';
import { LoginComponent } from './pages/login.component';
import { RegisterComponent } from './pages/register.component';
import { SubjectsComponent } from './pages/subjects.component';
import { SubjectDetailComponent } from './pages/subject-detail.component';
import { LessonComponent } from './pages/lesson.component';
import { QuizComponent } from './pages/quiz.component';
import { DashboardComponent } from './pages/dashboard.component';
import { AdminComponent } from './pages/admin.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '', pathMatch: 'full', redirectTo: 'subjects' },
  { path: 'subjects', component: SubjectsComponent, canActivate: [authGuard] },
  { path: 'subjects/:id', component: SubjectDetailComponent, canActivate: [authGuard] },
  { path: 'lessons/:id', component: LessonComponent, canActivate: [authGuard] },
  { path: 'quizzes/:id', component: QuizComponent, canActivate: [authGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'admin', component: AdminComponent, canActivate: [adminGuard] },
  { path: '**', redirectTo: 'subjects' }
];
