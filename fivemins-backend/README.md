# 5MINUTES — Backend (Spring Boot + SQLite)

## Prérequis
- Java 17 ou supérieur
- Maven 3.9 ou supérieur

## Lancement
Depuis ce dossier :

```bash
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8081`. SQLite est utilisé par défaut. Le fichier `fivemins.db` est créé automatiquement dans le dossier courant et conserve les données après l'arrêt et le redémarrage du backend. Ne supprime pas ce fichier si tu veux conserver les comptes, cours, quiz et résultats.

Pour changer son emplacement, définis la variable `DB_URL`, par exemple `jdbc:sqlite:./data/fivemins.db` ; le dossier `data` doit alors exister.

## Compte de démonstration
- Email : `admin@5mins.app`
- Mot de passe : `admin123`

Change ce mot de passe avant tout déploiement réel. La clé JWT peut être configurée avec la variable d'environnement `JWT_SECRET`.

## Fonctionnalités techniques
- Authentification par JWT et mots de passe hachés avec BCrypt.
- Routes de gestion protégées par le rôle `ADMIN` côté backend.
- Création, modification et suppression des leçons et quiz.
- Les bonnes réponses ne sont renvoyées à l'étudiant qu'après la soumission du quiz, sous forme de correction détaillée.
- Une leçon est comptée comme terminée après une session de lecture enregistrée et la soumission du quiz associé.
- Tableau de bord avec progression globale, prochaine leçon recommandée et dernier score.

## Quiz sans délai imposé
Le quiz est accessible dès que l'étudiant ouvre la leçon. La durée indiquée est informative et n'est pas un verrou. Une leçon n'est terminée que si une session de lecture existe et qu'une tentative du quiz associé est enregistrée.
