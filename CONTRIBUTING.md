# Contribuer à Vidygo

Nous apprécions les contributions ! Voici comment vous pouvez aider.

## Comment contribuer

### Signaler des bugs

Si vous trouvez un bug, veuillez :

1. Vérifier si le bug a déjà été signalé
2. Créer un nouvel "Issue" avec :
   - Une titre clair et descriptif
   - Une description détaillée du comportement
   - Les étapes pour reproduire
   - Le comportement attendu vs réel
   - Les captures d'écran si pertinent
   - Les informations du device et de l'OS

### Suggérer des améliorations

Pour suggérer une amélération :

1. Ouvrir un nouvel "Issue" avec le label "enhancement"
2. Décrire votre idée en détail
3. Expliquer pourquoi cette amélioration serait utile
4. Dresser une liste des changements nécessaires

### Soumettre des modifications

#### Processus Git

1. **Fork** le projet sur votre compte
2. **Clone** votre fork localement
   ```bash
   git clone https://github.com/YOUR_USERNAME/Vidygo.git
   cd Vidygo
   ```

3. **Créer une branche** pour votre fonctionnalité
   ```bash
   git checkout -b feature/ma-nouvelle-fonctionnalite
   ```

4. **Faire vos changements** et tester
   ```bash
   ./gradlew build
   ```

5. **Commit** vos changements
   ```bash
   git commit -am 'Ajouter ma nouvelle fonctionnalité'
   ```

6. **Push** votre branche
   ```bash
   git push origin feature/ma-nouvelle-fonctionnalite
   ```

7. **Créer une Pull Request**
   - Décrivez les changements
   - Mentionnez les issues liées
   - Attendez la revue du code

#### Messages de commit

Utilisez des messages clairs :

```
Type: Description courte

- Description plus détaillée
- Points clés des changements
- Références aux issues (Fixes #123)
```

Types:
- `feat:` Nouvelle fonctionnalité
- `fix:` Correction de bug
- `refactor:` Restructuration du code
- `test:` Ajout/modification de tests
- `docs:` Modifications de documentation
- `style:` Formatage, sans changement logique
- `chore:` Mise à jour des dépendances, scripts

#### Exemples

```
feat: Ajouter la recherche de vidéos

- Implémente une barre de recherche
- Filtre les vidéos par titre et chaîne
- Ajoute l'highlighting des résultats

Fixes #45
```

```
fix: Corriger le crash au suppression d'une vidéo

- Ajouter une vérification null
- Mettre à jour l'adaptateur correctement

Fixes #67
```

## Standards de code

### Formatage Java

```java
// Classes
public class MyClassName {
    // Constantes
    private static final String CONSTANT = "value";
    
    // Variables de classe
    private static int classVariable;
    
    // Variables d'instance
    private String instanceVariable;
    
    // Constructeurs
    public MyClassName() {
    }
    
    // Méthodes publiques
    public void publicMethod() {
    }
    
    // Méthodes privées
    private void privateMethod() {
    }
}
```

### Nommage

- **Classes** : PascalCase (`MyClassName`)
- **Méthodes** : camelCase (`myMethodName`)
- **Variables** : camelCase (`myVariableName`)
- **Constantes** : UPPER_SNAKE_CASE (`MY_CONSTANT`)
- **IDs de layout** : snake_case (`my_view_id`)

### Commentaires

```java
/**
 * Description brève de la classe/méthode
 */
public void myMethod() {
    // Commentaire pour la logique complexe
    int result = complex_calculation();
    
    return result;
}
```

### Indentation

- Utilisez 4 espaces (pas de tabs)
- Aligner le code correctement
- Une déclaration par ligne

## Tests

### Avant de soumettre

1. Testez localement sur l'émulateur
2. Testez sur un appareil physique si possible
3. Exécutez les tests :
   ```bash
   ./gradlew test
   ```

4. Vérifiez pour les erreurs de lint :
   ```bash
   ./gradlew lint
   ```

### Ajouter des tests

Pour les nouvelles fonctionnalités, ajoutez des tests :

```java
@Test
public void testMyNewFeature() {
    // Arrange
    MyClass obj = new MyClass();
    
    // Act
    obj.doSomething();
    
    // Assert
    assertTrue(obj.isValid());
}
```

## Documentation

Pour les changements majeurs, mettez à jour :

- `README.md` - Vue d'ensemble du projet
- `DOCUMENTATION.md` - Architecture et structure
- `GETTING_STARTED.md` - Guide de démarrage
- Code source - Commentaires Javadoc

## Questions ?

- Consultez la section GitHub Discussions
- Ouvrez une Issue avec le label "question"
- Contactez les mainteneurs

## Code de conduite

Soyez respectueux et courtois. Toute contribution harassment, abuse, ou contenu offensant sera rejetée.

---

Merci de contribuer à Vidygo ! 🙏

