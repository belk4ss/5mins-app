# 5mins — MVP

## Backend (Spring Boot 3 / Java 17)
```
cd backend
mvn spring-boot:run          # http://localhost:8080
```
Compte admin de démo : `admin@5mins.app` / `admin123`  (base H2 en mémoire, données de démo créées au démarrage)

## Frontend (Angular 17+)
```
npm i -g @angular/cli
ng new fivemins-web --standalone --routing=false --style=css
# puis copier le contenu de frontend/src/ dans fivemins-web/src/ (en remplaçant app.config.ts, app.component.ts, styles.css)
cd fivemins-web && ng serve   # http://localhost:4200
```
Supprimer `app.component.html/css/spec.ts` et `app.routes.ts` générés si Angular les crée (les nôtres les remplacent).

## Endpoints principaux
POST /api/auth/register | /login
GET  /api/subjects, /api/subjects/{id}, /api/lessons/{id}, /api/quizzes/{id}
POST /api/lessons/{id}/sessions        (StudySession + streak + progression)
POST /api/quizzes/{id}/attempts        body: {"answers": {"<questionId>": <answerId>}}
GET  /api/me/progress | /me/streak | /me/attempts
ADMIN: POST /api/subjects, /api/subjects/{id}/lessons, /api/lessons/{id}/quiz
