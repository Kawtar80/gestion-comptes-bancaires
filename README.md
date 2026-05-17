# Projet : Gestion de Comptes Bancaires avec IA

Salut ! Voici notre projet de gestion de comptes bancaires.

L'idée de base était de créer une application CRUD classique en respectant l'architecture MVC avec **Java Spring Boot** et **Thymeleaf**. Mais pour rendre le projet plus intéressant et moderne, on a décidé d'y intégrer un véritable **Agent IA Local** ! 

Grâce à **Spring AI** et **Ollama**, l'utilisateur peut discuter avec l'assistant directement sur la page web pour faire ses dépôts, ses retraits ou consulter son solde, le tout en langage naturel.

## Prérequis
Avant d'essayer de lancer le code, vérifie que tu as bien installé ceci sur ta machine :
- **Java 17** (ou plus récent).
- Ton IDE habituel (VS Code, IntelliJ, etc.).
- **Ollama** : C'est indispensable pour faire tourner l'IA en local sur le PC. Sans ça, le chatbot ne pourra pas répondre.

---

## Comment lancer le projet étape par étape ?

### 1. Allumer le moteur de l'IA (Ollama)
L'application a besoin du modèle "Mistral" pour comprendre les requêtes. Il faut donc le lancer en arrière-plan.
1. Ouvre un terminal (ou l'invite de commandes).
2. Tape cette commande :
   ```bash
   ollama run mistral
   ```
*(Si c'est la première fois, il va télécharger le modèle, ça prend quelques minutes. Une fois qu'il est lancé et prêt à répondre, laisse simplement ce terminal ouvert dans un coin).*

### 2. Démarrer l'application Spring Boot
Maintenant que le "cerveau" de l'IA est allumé, on peut lancer le code Java.
1. Ouvre le projet dans ton IDE.
2. Va dans le fichier principal : `src/main/java/com/example/bank/BankApplication.java`.
3. Clique sur **Run**.
*(Note : On a mis à jour les versions dans le `pom.xml` pour avoir la dernière version de Spring AI (1.0.0), donc laisse Maven charger les dépendances au premier démarrage).*

### 3. Tester le site web !
Une fois que la console de l'IDE t'indique que le serveur a démarré (`Started BankApplication`), ouvre ton navigateur et va sur :
👉 **http://localhost:8080/accounts**

### Comment tester l'Agent IA ?
Tu peux gérer les comptes avec les boutons classiques, mais je te conseille de tester la fenêtre de chat en bas de l'écran. 

Essaie de taper exactement ces phrases :
- *"dépôt 500 sur compte 1"*
- *"retrait 200 du compte 2"*
- *"liste des comptes"*

*(N'oublie pas d'actualiser la page (F5) après une transaction pour voir le tableau se mettre à jour).*

**Détail technique pour le prof :** Nous avons implémenté le fameux "Function Calling" demandé via les outils de Spring AI (`@Bean public Function...`). Mais pour garantir une fiabilité à 100% lors de la démo (car le modèle Mistral 7B hallucine parfois les formats JSON), nous avons renforcé l'architecture avec un routeur d'intention hybride (Layered Architecture) qui intercepte et garantit l'exécution des transactions financières.

Bon test !
