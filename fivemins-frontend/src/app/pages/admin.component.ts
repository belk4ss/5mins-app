import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { LessonSummary, Subject } from '../core/models';

interface AnswerForm { text: string; correct: boolean; explanation?: string; }
interface QuestionForm { id?: number; text: string; answers: AnswerForm[]; }

const newQuestion = (): QuestionForm => ({
  text: '',
  answers: [{ text: '', correct: true, explanation: '' }, { text: '', correct: false, explanation: '' }]
});

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="page-heading"><div><span class="eyebrow">ESPACE ÉDITEUR</span><h2>Gestion des contenus</h2><p class="muted">Organisez les matières, les leçons et les quiz de 5MINUTES.</p></div></div>
    @if (msg()) { <p class="success">{{ msg() }}</p> }
    @if (err()) { <p class="error">{{ err() }}</p> }

    <!-- 1. Matière -->
    <div class="card">
      <h3><span class="step-number">01</span> Créer une matière</h3>
      <input placeholder="Titre" [(ngModel)]="sTitle" />
      <input placeholder="Description" [(ngModel)]="sDesc" />
      <button class="btn" (click)="createSubject()">Créer la matière</button>
    </div>

    <!-- 2. Leçon -->
    <div class="card">
      <h3><span class="step-number">02</span> Ajouter une leçon</h3>
      <select [(ngModel)]="lessonSubjectId">
        <option [ngValue]="null">— Choisir une matière —</option>
        @for (s of subjects(); track s.id) { <option [ngValue]="s.id">{{ s.title }}</option> }
      </select>
      <input placeholder="Titre de la leçon" [(ngModel)]="lTitle" />
      <textarea rows="5" placeholder="Contenu" [(ngModel)]="lContent"></textarea>
      <input type="number" min="1" placeholder="Durée (minutes)" [(ngModel)]="lDuration" />
      <button class="btn" (click)="createLesson()">Ajouter la leçon</button>
    </div>

    <!-- 3. Quiz -->
    <div class="card">
      <h3><span class="step-number">03</span> Créer un quiz (3 à 5 questions)</h3>
      <select [(ngModel)]="quizSubjectId" (ngModelChange)="loadLessons($event)">
        <option [ngValue]="null">— Choisir une matière —</option>
        @for (s of subjects(); track s.id) { <option [ngValue]="s.id">{{ s.title }}</option> }
      </select>
      <select [(ngModel)]="quizLessonId">
        <option [ngValue]="null">— Choisir une leçon —</option>
        @for (l of lessons(); track l.id) {
          <option [ngValue]="l.id">{{ l.title }}{{ l.hasQuiz ? ' (quiz existant, sera remplacé)' : '' }}</option>
        }
      </select>
      <input placeholder="Titre du quiz" [(ngModel)]="quizTitle" />

      @for (q of questions(); track q; let qi = $index) {
        <div class="question">
          <div class="row">
            <b>Question {{ qi + 1 }}</b>
            <button class="btn small danger" (click)="removeQuestion(qi)">Supprimer</button>
          </div>
          <input placeholder="Texte de la question" [(ngModel)]="q.text" />
          @for (a of q.answers; track a; let ai = $index) {
            <div class="answer">
              <input type="radio" [name]="'correct' + qi" [checked]="a.correct" (change)="setCorrect(q, ai)"
                     title="Bonne réponse" />
              <input placeholder="Réponse {{ ai + 1 }}" [(ngModel)]="a.text" /><input placeholder="Explication de la bonne réponse (facultatif)" [(ngModel)]="a.explanation" />
              <button class="btn small danger" (click)="removeAnswer(q, ai)">Retirer</button>
            </div>
          }
          <button class="btn small" (click)="addAnswer(q)">+ Réponse</button>
        </div>
      }
      <div>
        <button class="btn small" (click)="addQuestion()">+ Question</button>
        <button class="btn" (click)="createQuiz()">Enregistrer le quiz</button>
      </div>
      <p class="muted">Le bouton radio indique la bonne réponse.</p>
    </div>

    <!-- Gestion et suppression des contenus existants -->
    <div class="card">
      <h3>Gérer les cours et les quiz existants</h3>
      <p class="muted">La suppression d’un cours supprime également son quiz associé. Les actions sont réservées au rôle administrateur.</p>
      @for (s of subjects(); track s.id) {
        <section class="manage-subject">
          <h4>{{ s.title }}</h4>
          <button class="btn small danger" (click)="loadLessonsForManagement(s.id)">Afficher les cours</button>
          @if (managedSubjectId() === s.id) {
            @for (l of managedLessons(); track l.id) {
              <div class="manage-row">
                <div><b>{{ l.title }}</b><small>{{ l.durationMinutes }} min · {{ l.hasQuiz ? 'Quiz disponible' : 'Sans quiz' }}</small></div>
                <div class="manage-actions">
                  <button class="btn small" (click)="editLesson(l)">Modifier le cours</button>
                  @if (l.hasQuiz) { <button class="btn small" (click)="editQuiz(l)">Modifier le quiz</button><button class="btn small danger" (click)="deleteQuiz(l)">Supprimer le quiz</button> }
                  <button class="btn small danger" (click)="deleteLesson(l)">Supprimer le cours</button>
                </div>
                @if (editingLessonId() === l.id) {
                  <div class="edit-panel">
                    <h4>Modifier la leçon</h4>
                    <input placeholder="Titre" [(ngModel)]="editTitle" />
                    <textarea rows="5" placeholder="Contenu" [(ngModel)]="editContent"></textarea>
                    <input type="number" min="1" [(ngModel)]="editDuration" />
                    <button class="btn small" (click)="saveLesson(l.id)">Enregistrer les modifications</button>
                  </div>
                }
                @if (editingQuizLessonId() === l.id) {
                  <div class="edit-panel">
                    <h4>Modifier le quiz</h4>
                    <input placeholder="Titre du quiz" [(ngModel)]="quizTitle" />
                    @for (q of questions(); track q; let qi = $index) {
                      <div class="question"><b>Question {{ qi + 1 }}</b><input placeholder="Question" [(ngModel)]="q.text" />
                        @for (a of q.answers; track a; let ai = $index) {
                          <div class="answer"><input type="radio" [name]="'editcorrect' + qi" [checked]="a.correct" (change)="setCorrect(q, ai)" /><input placeholder="Réponse" [(ngModel)]="a.text" /><input placeholder="Explication si correcte" [(ngModel)]="a.explanation" /></div>
                        }
                        <button class="btn small" (click)="addAnswer(q)">+ Réponse</button><button class="btn small danger" (click)="removeQuestion(qi)">Retirer question</button>
                      </div>
                    }
                    <button class="btn small" (click)="addQuestion()">+ Question</button><button class="btn" (click)="saveQuiz(l.id)">Enregistrer le quiz</button>
                  </div>
                }
              </div>
            } @empty { <p class="muted">Aucun cours dans cette matière.</p> }
          }
        </section>
      }
    </div>
  `
})
export class AdminComponent {
  private api = inject(ApiService);

  subjects = signal<Subject[]>([]);
  lessons = signal<LessonSummary[]>([]);
  managedLessons = signal<LessonSummary[]>([]);
  managedSubjectId = signal<number | null>(null);
  editingLessonId = signal<number | null>(null);
  editingQuizLessonId = signal<number | null>(null);
  editTitle = ''; editContent = ''; editDuration = 5;
  questions = signal<QuestionForm[]>([newQuestion(), newQuestion(), newQuestion()]);
  msg = signal('');
  err = signal('');

  sTitle = ''; sDesc = '';
  lessonSubjectId: number | null = null; lTitle = ''; lContent = ''; lDuration = 5;
  quizSubjectId: number | null = null; quizLessonId: number | null = null; quizTitle = '';

  constructor() { this.loadSubjects(); }

  private ok(m: string) { this.err.set(''); this.msg.set(m); }
  private fail(m: string) { this.msg.set(''); this.err.set(m); }
  private loadSubjects() { this.api.subjects().subscribe(s => this.subjects.set(s)); }

  createSubject() {
    if (!this.sTitle.trim()) return this.fail('Le titre de la matière est obligatoire.');
    this.api.createSubject(this.sTitle.trim(), this.sDesc.trim()).subscribe({
      next: () => { this.ok('Matière créée.'); this.sTitle = ''; this.sDesc = ''; this.loadSubjects(); },
      error: () => this.fail('Échec de la création de la matière.')
    });
  }

  createLesson() {
    if (this.lessonSubjectId === null) return this.fail('Choisissez une matière.');
    if (!this.lTitle.trim()) return this.fail('Le titre de la leçon est obligatoire.');
    this.api.createLesson(this.lessonSubjectId, {
      title: this.lTitle.trim(), content: this.lContent, durationMinutes: this.lDuration || 5
    }).subscribe({
      next: () => { this.ok('Leçon ajoutée.'); this.lTitle = ''; this.lContent = ''; this.loadSubjects(); },
      error: () => this.fail('Échec de l\'ajout de la leçon.')
    });
  }

  loadLessons(subjectId: number | null) {
    this.quizLessonId = null;
    this.lessons.set([]);
    if (subjectId !== null) this.api.subject(subjectId).subscribe(d => this.lessons.set(d.lessons));
  }

  loadLessonsForManagement(subjectId: number) {
    if (this.managedSubjectId() === subjectId) { this.managedSubjectId.set(null); this.managedLessons.set([]); return; }
    this.api.subject(subjectId).subscribe({
      next: d => { this.managedSubjectId.set(subjectId); this.managedLessons.set(d.lessons); },
      error: () => this.fail('Impossible de charger les cours de cette matière.')
    });
  }

  editLesson(lesson: LessonSummary) {
    this.api.lesson(lesson.id).subscribe({ next: l => { this.editingLessonId.set(l.id); this.editTitle = l.title; this.editContent = l.content; this.editDuration = l.durationMinutes; this.editingQuizLessonId.set(null); }, error: () => this.fail('Impossible de charger la leçon.') });
  }
  saveLesson(id: number) {
    if (!this.editTitle.trim() || this.editDuration < 1) return this.fail('Titre et durée valides obligatoires.');
    this.api.updateLesson(id, { title: this.editTitle.trim(), content: this.editContent, durationMinutes: this.editDuration }).subscribe({ next: () => { this.ok('Leçon modifiée.'); this.editingLessonId.set(null); this.refreshManaged(); }, error: () => this.fail('Impossible de modifier la leçon.') });
  }
  editQuiz(lesson: LessonSummary) {
    this.api.adminQuiz(lesson.id).subscribe({ next: q => { this.quizTitle = q.title; this.questions.set(q.questions.map(x => ({ id: x.id, text: x.text, answers: x.answers.map(a => ({ text: a.text, correct: a.correct, explanation: a.explanation || '' })) }))); this.editingQuizLessonId.set(lesson.id); this.editingLessonId.set(null); }, error: () => this.fail('Impossible de charger le quiz à modifier.') });
  }
  saveQuiz(lessonId: number) {
    const qs = this.questions();
    if (!this.quizTitle.trim() || qs.length < 3 || qs.length > 5) return this.fail('Le quiz doit avoir un titre et 3 à 5 questions.');
    for (const q of qs) if (!q.text.trim() || q.answers.length < 2 || q.answers.some(a => !a.text.trim()) || q.answers.filter(a => a.correct).length !== 1) return this.fail('Chaque question doit avoir au moins deux réponses et exactement une bonne réponse.');
    this.api.updateQuiz(lessonId, { title: this.quizTitle.trim(), questions: qs.map(q => ({ text: q.text.trim(), answers: q.answers.map(a => ({ text: a.text.trim(), correct: a.correct, explanation: a.explanation || '' })) })) }).subscribe({ next: () => { this.ok('Quiz modifié.'); this.editingQuizLessonId.set(null); this.refreshManaged(); this.loadLessons(this.quizSubjectId); }, error: () => this.fail('Impossible de modifier le quiz.') });
  }

  deleteQuiz(lesson: LessonSummary) {
    if (!confirm(`Supprimer le quiz associé à « ${lesson.title} » ? Cette action est irréversible.`)) return;
    this.api.deleteQuiz(lesson.id).subscribe({
      next: () => { this.ok('Quiz supprimé.'); this.refreshManaged(); },
      error: () => this.fail('Impossible de supprimer ce quiz.')
    });
  }

  deleteLesson(lesson: LessonSummary) {
    if (!confirm(`Supprimer le cours « ${lesson.title} » et son quiz éventuel ? Cette action est irréversible.`)) return;
    this.api.deleteLesson(lesson.id).subscribe({
      next: () => { this.ok('Cours supprimé.'); this.refreshManaged(); this.loadSubjects(); this.loadLessons(this.quizSubjectId); },
      error: () => this.fail('Impossible de supprimer ce cours.')
    });
  }

  private refreshManaged() {
    const id = this.managedSubjectId();
    if (id !== null) this.api.subject(id).subscribe(d => this.managedLessons.set(d.lessons));
  }

  // --- gestion du formulaire de quiz ---
  private refresh() { this.questions.update(q => [...q]); }
  addQuestion() { if (this.questions().length < 5) this.questions.update(q => [...q, newQuestion()]); }
  removeQuestion(i: number) { if (this.questions().length > 3) this.questions.update(q => q.filter((_, x) => x !== i)); }
  addAnswer(q: QuestionForm) { q.answers.push({ text: '', correct: false }); this.refresh(); }
  removeAnswer(q: QuestionForm, i: number) {
    if (q.answers.length <= 2) return;
    q.answers.splice(i, 1);
    if (!q.answers.some(a => a.correct)) q.answers[0].correct = true;
    this.refresh();
  }
  setCorrect(q: QuestionForm, index: number) { q.answers.forEach((a, i) => a.correct = i === index); }

  createQuiz() {
    const qs = this.questions();
    if (this.quizLessonId === null) return this.fail('Choisissez une leçon.');
    if (!this.quizTitle.trim()) return this.fail('Le titre du quiz est obligatoire.');
    if (qs.length < 3 || qs.length > 5) return this.fail('Un quiz doit contenir 3 à 5 questions.');
    for (const q of qs) {
      if (!q.text.trim()) return this.fail('Toutes les questions doivent avoir un texte.');
      if (q.answers.length < 2 || q.answers.some(a => !a.text.trim()))
        return this.fail('Chaque question doit avoir au moins 2 réponses remplies.');
      if (q.answers.filter(a => a.correct).length !== 1)
        return this.fail('Chaque question doit avoir exactement une bonne réponse.');
    }
    this.api.createQuiz(this.quizLessonId, {
      title: this.quizTitle.trim(),
      questions: qs.map(q => ({ text: q.text.trim(), answers: q.answers.map(a => ({ text: a.text.trim(), correct: a.correct, explanation: a.explanation || '' })) }))
    }).subscribe({
      next: () => {
        this.ok('Quiz enregistré.');
        this.quizTitle = '';
        this.questions.set([newQuestion(), newQuestion(), newQuestion()]);
        this.loadLessons(this.quizSubjectId);
      },
      error: () => this.fail('Échec de l\'enregistrement du quiz.')
    });
  }
}
