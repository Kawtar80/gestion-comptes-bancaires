# Gestion des Comptes Bancaires - Projet Spring Boot

Ce projet est une application web de gestion de comptes bancaires développée avec **Java Spring Boot** et **Thymeleaf**, respectant l'architecture MVC.

Il intègre également une fonctionnalité avancée : un **Agent IA Local** capable de comprendre des requêtes en langage naturel (grâce à **Spring AI** et **Ollama**) pour effectuer des opérations bancaires.

## Prérequis

Pour exécuter ce projet sur votre machine, vous aurez besoin de :
1. **Java 17** (ou version supérieure).
2. Un IDE (IntelliJ IDEA, Eclipse, VS Code) ou **Maven** installé sur votre machine.
3. **Ollama** installé localement pour faire fonctionner l'Agent IA.

## Étape 1 : Démarrer le moteur IA (Ollama)

L'application communique en local avec le modèle **Mistral** via Ollama.
1. Téléchargez et installez [Ollama](https://ollama.com/).
2. Ouvrez un terminal (ou invite de commandes).
3. Exécutez la commande suivante pour télécharger et lancer le modèle :
   ```bash
   ollama run mistral
   ```
   *(Laissez le terminal ouvert en arrière-plan pendant l'utilisation de l'application).*

## Étape 2 : Lancer l'application Spring Boot

Vous pouvez démarrer l'application de deux façons :

**Option A : Via votre IDE (Recommandé)**
1. Ouvrez le projet dans votre IDE (IntelliJ, Eclipse, VS Code).
2. Naviguez vers `src/main/java/com/example/bank/BankApplication.java`.
3. Exécutez la méthode `main()`.

**Option B : Via Maven (Ligne de commande)**
1. Ouvrez un terminal dans le dossier racine du projet.
2. Exécutez la commande :
   ```bash
   mvn spring-boot:run
   ```

## Étape 3 : Accéder à l'application

Une fois l'application démarrée, ouvrez votre navigateur web et accédez à l'URL suivante :
👉 **http://localhost:8080/accounts**

### Tester l'Agent IA
Sur la page principale, un panneau "🤖 Agent Bancaire" est disponible. Vous pouvez y entrer des commandes en langage naturel telles que :
- *"Liste tous les comptes"*
- *"Dépôt de 500 sur le compte 1"*
- *"Créer un compte pour Alice"*
