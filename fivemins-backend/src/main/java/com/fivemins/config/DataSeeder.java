package com.fivemins.config;

import com.fivemins.dto.Dtos.*;
import com.fivemins.model.*;
import com.fivemins.repo.SubjectRepository;
import com.fivemins.repo.LessonRepository;
import com.fivemins.repo.UserRepository;
import com.fivemins.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Données de démonstration : admin@5mins.app / admin123. Ajoute le contenu manquant sans dupliquer les leçons. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final SubjectRepository subjects;
    private final LessonRepository lessons;
    private final PasswordEncoder encoder;
    private final LearningService service;

    @Override
    @Transactional
    public void run(String... args) {
        if (users.findByEmail("admin@5mins.app").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@5mins.app");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            users.save(admin);
        }

        Subject subject = subjects.findAll().stream()
                .filter(s -> s.getTitle().equals("Java en 5 minutes"))
                .findFirst().orElse(null);
        if (subject == null) {
            SubjectDto created = service.createSubject(new CreateSubject(
                    "Java en 5 minutes", "Les bases de Java, une leçon à la fois."));
            subject = subjects.findById(created.id()).orElseThrow();
        }

        seedLesson(subject, "Variables et types",
                "En Java, chaque variable possède un type qui définit les données qu'elle peut contenir.\n\n" +
                "Exemples : int age = 20;\n double prix = 9.99;\n boolean actif = true;\n String nom = \"Sara\";\n\n" +
                "À retenir : String représente du texte, int un entier, double un nombre décimal et boolean une valeur vrai/faux.",
                "Quiz : variables et types",
                List.of(q("Quel type représente un nombre entier ?", a("int",true), a("String",false), a("boolean",false)),
                        q("Quel type représente vrai ou faux ?", a("boolean",true), a("double",false)),
                        q("Quel type est adapté pour stocker un prénom ?", a("String",true), a("int",false), a("char[] uniquement",false))));
        seedLesson(subject, "Conditions et décisions",
                "Les conditions permettent d'exécuter un bloc selon une situation.\n\n" +
                "if (note >= 10) { System.out.println(\"Validé\"); } else { System.out.println(\"À revoir\"); }\n\n" +
                "Utilise else if pour tester plusieurs cas. Les opérateurs courants sont ==, !=, <, >, <= et >=.",
                "Quiz : conditions",
                List.of(q("Quel mot-clé introduit une condition ?", a("if",true), a("repeat",false), a("when",false)),
                        q("Quel opérateur teste l'égalité de valeurs primitives ?", a("==",true), a("=",false), a("!=",false)),
                        q("Quel bloc s'exécute si le if est faux et qu'un else existe ?", a("else",true), a("try",false), a("import",false))));
        seedLesson(subject, "Boucles et répétitions",
                "Les boucles évitent de répéter manuellement les mêmes instructions.\n\n" +
                "for (int i = 0; i < 5; i++) { System.out.println(i); }\n\n" +
                "while répète tant que la condition est vraie. Vérifie que la condition évolue pour éviter une boucle infinie.",
                "Quiz : boucles",
                List.of(q("Quelle boucle est souvent pratique quand le nombre d'itérations est connu ?", a("for",true), a("if",false), a("switch",false)),
                        q("Quand une boucle while continue-t-elle ?", a("Tant que sa condition est vraie",true), a("Une seule fois",false)),
                        q("Que risque-t-on si la condition ne devient jamais fausse ?", a("Une boucle infinie",true), a("Une compilation automatique",false))));
        seedLesson(subject, "Méthodes et paramètres",
                "Une méthode regroupe des instructions dans un bloc réutilisable. Elle peut recevoir des paramètres et retourner un résultat.\n\n" +
                "static int additionner(int a, int b) { return a + b; }\n\n" +
                "L'appel additionner(2, 3) renvoie 5. Le type avant le nom indique le type retourné ; void signifie qu'aucune valeur n'est renvoyée.",
                "Quiz : méthodes",
                List.of(q("Quel mot-clé renvoie une valeur depuis une méthode ?", a("return",true), a("break",false), a("class",false)),
                        q("Que signifie void comme type de retour ?", a("Aucune valeur retournée",true), a("Retourne toujours zéro",false)),
                        q("Dans additionner(int a, int b), a et b sont…", a("Des paramètres",true), a("Des packages",false), a("Des classes",false))));
        seedLesson(subject, "Tableaux et collections de base",
                "Un tableau stocke plusieurs valeurs du même type dans une structure de taille fixe.\n\n" +
                "int[] notes = {12, 15, 18};\nSystem.out.println(notes[0]); // 12\n\n" +
                "Les indices commencent à zéro. Pour un tableau de longueur n, le dernier indice est n - 1 et sa taille se lit avec .length.",
                "Quiz : tableaux",
                List.of(q("Quel est le premier indice d'un tableau Java ?", a("0",true), a("1",false), a("-1",false)),
                        q("Comment obtenir la taille d'un tableau nommé notes ?", a("notes.length",true), a("notes.size()",false)),
                        q("Quel type de valeurs un int[] contient-il ?", a("Des entiers",true), a("Uniquement du texte",false), a("Des méthodes",false))));

        seedTopic("Linux", "Commandes Linux essentielles",
                "Le terminal Linux permet d'interagir avec le système par des commandes. pwd affiche le répertoire courant, ls liste son contenu, cd change de répertoire et mkdir crée un dossier. La commande man affiche le manuel d'une commande. Exemple : mkdir projets puis cd projets.",
                List.of(q("Quelle commande affiche le répertoire courant ?", a("pwd",true), a("cd",false), a("mkdir",false)),
                        q("Quelle commande liste les fichiers d'un répertoire ?", a("ls",true), a("rm",false), a("touch",false)),
                        q("Quelle commande sert à consulter le manuel d'une commande ?", a("man",true), a("mv",false), a("echo",false))));
        seedTopic("Python", "Bases de Python",
                "Python est un langage interprété. On peut affecter une valeur avec x = 5, afficher un résultat avec print(x), définir une fonction avec def et parcourir une séquence avec for. Les listes sont modifiables et leur premier indice est 0.",
                List.of(q("Quelle instruction affiche un texte dans la console ?", a("print()",true), a("show()",false), a("echo()",false)),
                        q("Quel mot-clé permet de définir une fonction ?", a("def",true), a("func",false), a("function",false)),
                        q("Quel est le premier indice d'une liste Python ?", a("0",true), a("1",false), a("-1 uniquement",false))));
        seedTopic("Git et GitHub", "Versionner un projet avec Git",
                "Git conserve l'historique des modifications d'un projet. git status montre l'état du dépôt, git add prépare des changements, git commit crée un instantané local et git push envoie les commits vers un dépôt distant. GitHub héberge des dépôts Git et facilite le travail collaboratif.",
                List.of(q("Quelle commande crée un commit ?", a("git commit",true), a("git push",false), a("git status",false)),
                        q("Quelle commande affiche les changements et fichiers non suivis ?", a("git status",true), a("git clone",false), a("git log -p uniquement",false)),
                        q("À quoi sert généralement git push ?", a("Envoyer les commits vers un dépôt distant",true), a("Supprimer l'historique local",false), a("Créer automatiquement une branche",false))));
        seedTopic("Bases de données SQL", "Interroger une base avec SQL",
                "SQL permet de lire et modifier des données relationnelles. SELECT lit des données, INSERT ajoute des lignes, UPDATE modifie des lignes et DELETE supprime des lignes. WHERE filtre les lignes concernées ; sans filtre, UPDATE ou DELETE peut affecter toutes les lignes de la table.",
                List.of(q("Quelle instruction permet de lire des données ?", a("SELECT",true), a("UPDATE",false), a("INSERT",false)),
                        q("Quel mot-clé filtre les lignes d'une requête ?", a("WHERE",true), a("ORDER",false), a("VALUES",false)),
                        q("Quelle instruction ajoute une nouvelle ligne ?", a("INSERT",true), a("DELETE",false), a("ALTER",false))));
        seedTopic("Réseaux informatiques", "Comprendre les bases des réseaux",
                "Une adresse IP identifie une interface sur un réseau IP. DNS traduit les noms de domaine en adresses IP. TCP fournit un transport fiable et ordonné, tandis qu'UDP privilégie un transport léger sans garantie de livraison. ping aide à tester la joignabilité d'un hôte.",
                List.of(q("Quel service traduit les noms de domaine en adresses IP ?", a("DNS",true), a("DHCP uniquement",false), a("FTP",false)),
                        q("Quel protocole fournit un transport fiable et ordonné ?", a("TCP",true), a("UDP",false), a("ARP",false)),
                        q("Quelle commande est souvent utilisée pour tester la joignabilité d'un hôte ?", a("ping",true), a("mkdir",false), a("chmod",false))));
    }

    private void seedTopic(String subjectTitle, String lessonTitle, String content, List<CreateQuestion> questions) {
        Subject subject = subjects.findAll().stream().filter(s -> s.getTitle().equals(subjectTitle)).findFirst().orElse(null);
        if (subject == null) {
            SubjectDto created = service.createSubject(new CreateSubject(subjectTitle, "Leçons pratiques et quiz pour apprendre " + subjectTitle + "."));
            subject = subjects.findById(created.id()).orElseThrow();
        }
        seedLesson(subject, lessonTitle, content, "Quiz : " + subjectTitle, questions);
    }

    private void seedLesson(Subject subject, String title, String content, String quizTitle, List<CreateQuestion> questions) {
        Long subjectId = subject.getId();
        Lesson lesson = lessons.findAll().stream()
                .filter(l -> l.getSubject().getId().equals(subjectId) &&
                        (l.getTitle().equals(title) || (title.equals("Boucles et répétitions") && l.getTitle().equals("Les boucles"))))
                .findFirst().orElse(null);
        if (lesson != null && title.equals("Boucles et répétitions") && lesson.getTitle().equals("Les boucles")) {
            lesson.setTitle(title);
            lessons.save(lesson);
        }
        if (lesson == null) {
            LessonDto created = service.createLesson(subject.getId(), new CreateLesson(title, content, 5));
            lesson = lessons.findById(created.id()).orElseThrow();
        }
        if (lesson.getQuiz() == null) service.createQuiz(lesson.getId(), new CreateQuiz(quizTitle, questions));
    }

    private static CreateAnswer a(String text, boolean correct) {
        String explanation = null;
        if (correct) explanation = switch (text) {
            case "int" -> "En Java, int est le type primitif utilisé pour stocker un nombre entier.";
            case "boolean" -> "Le type boolean représente une valeur logique : true ou false.";
            case "String" -> "String représente une chaîne de caractères, par exemple un prénom.";
            case "if" -> "if exécute un bloc lorsque la condition associée est vraie.";
            case "==" -> "Pour les types primitifs, == teste l'égalité des valeurs ; = sert à l'affectation.";
            case "else" -> "Le bloc else est exécuté lorsque la condition du if est fausse.";
            case "for" -> "Une boucle for regroupe généralement l'initialisation, la condition et la mise à jour, ce qui convient quand le nombre d'itérations est connu.";
            case "Tant que sa condition est vraie" -> "Une boucle while réévalue sa condition avant chaque itération.";
            case "Une boucle infinie" -> "Si la condition ne devient jamais fausse et qu'aucune sortie n'est prévue, la boucle continue indéfiniment.";
            case "return" -> "return termine l'exécution de la méthode et renvoie la valeur indiquée, si le type de retour l'exige.";
            case "Aucune valeur retournée" -> "Une méthode déclarée void ne renvoie pas de valeur à son appelant.";
            case "Des paramètres" -> "Les paramètres sont les variables déclarées dans la signature et reçoivent les arguments lors de l'appel.";
            case "0" -> "En Java comme dans la plupart des langages courants, les indices de tableau commencent à zéro.";
            case "notes.length" -> "La propriété length donne le nombre d'éléments d'un tableau Java.";
            case "Des entiers" -> "int[] est un tableau dont chaque élément est de type int, donc un entier.";
            case "pwd" -> "pwd signifie print working directory et affiche le répertoire courant.";
            case "ls" -> "ls affiche les fichiers et dossiers du répertoire ciblé.";
            case "man" -> "man ouvre la page de manuel qui documente une commande Linux.";
            case "print()" -> "print() écrit une valeur ou un texte dans la sortie standard en Python.";
            case "def" -> "Le mot-clé def introduit la définition d'une fonction en Python.";
            case "git commit" -> "git commit enregistre les changements préparés dans l'historique local du dépôt.";
            case "git status" -> "git status indique les fichiers modifiés, préparés et non suivis.";
            case "Envoyer les commits vers un dépôt distant" -> "git push transmet les commits locaux vers le dépôt distant configuré.";
            case "SELECT" -> "SELECT sert à lire des données depuis une ou plusieurs tables SQL.";
            case "WHERE" -> "WHERE filtre les lignes selon une condition dans une requête SQL.";
            case "INSERT" -> "INSERT INTO ajoute une ou plusieurs lignes à une table SQL.";
            case "DNS" -> "Le DNS résout les noms de domaine en adresses IP.";
            case "TCP" -> "TCP fournit un flux fiable et ordonné grâce à des mécanismes de contrôle et de retransmission.";
            case "ping" -> "ping envoie des requêtes ICMP pour vérifier la joignabilité d'un hôte et mesurer le temps de réponse.";
            default -> "Cette réponse correspond à la notion évaluée dans la question : " + text + ".";
        };
        return new CreateAnswer(text, correct, explanation);
    }
    private static CreateQuestion q(String text, CreateAnswer... answers) { return new CreateQuestion(text, List.of(answers)); }
}
