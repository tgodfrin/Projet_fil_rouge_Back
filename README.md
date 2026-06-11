# LOC MNS — API back-end

API REST de gestion du parc informatique et matériel de Metz Numeric School (MNS). Le service gère les utilisateurs, les équipements, les familles de matériel, les emprunts et les événements associés. Il expose une authentification par jeton JWT et une autorisation par rôle.

Ce dépôt contient la partie back-end du projet fil rouge réalisé dans le cadre du titre Concepteur Développeur d'Applications (CDA), promotion 2025-2026. L'interface utilisateur est développée dans un dépôt distinct (Angular).

## Fonctionnalités

L'API couvre les domaines suivants :

- Authentification par JWT et autorisation par rôle (Gestionnaire, Collaborateur, Intervenant, Stagiaire).
- Gestion des équipements avec calcul automatique de leur statut (disponible, en prêt, hors service, en réparation).
- Gestion des familles de matériel et des droits d'emprunt associés à chaque rôle.
- Cycle de vie complet des emprunts : demande, validation, refus, retour, emprunts groupés.
- Signalement d'événements par l'emprunteur (panne, retour anticipé, prolongation) et suivi des alertes non lues.
- Gestion des documents et des caractéristiques techniques rattachés aux équipements.

## Stack technique

| Technologie | Version | Rôle |
|---|---|---|
| Java | 17 | Langage |
| Spring Boot | 3.4.4 | Framework applicatif |
| Spring Security | gérée par Spring Boot | Authentification et autorisation |
| Spring Data JPA | gérée par Spring Boot | Accès aux données |
| Hibernate | géré par Spring Boot | ORM |
| jjwt | 0.12.6 | Génération et vérification des jetons JWT |
| springdoc OpenAPI | 2.8.4 | Documentation interactive de l'API |
| PostgreSQL | 16 | Base de données relationnelle |
| Lombok | 1.18.46 | Réduction du code répétitif |
| Maven | wrapper inclus | Construction et gestion des dépendances |

## Architecture du projet

Le code source suit une organisation en couches classique de Spring Boot :

```
src/main/java/com/locmns
  config         Configuration transverse (OpenAPI)
  controller     Points d'entrée REST
  dao            Interfaces Spring Data JPA
  dto            Objets de requête (corps des POST et PUT)
  entity         Entités JPA et énumérations
  exception      Gestion centralisée des erreurs
  security       Spring Security, filtre et service JWT, annotations de rôle
  service        Logique métier
  view           Vues Jackson (@JsonView) pour les réponses

src/main/resources
  application.properties   Configuration de l'application
  data.sql                 Jeu de données de démonstration
```

Le projet sépare les requêtes et les réponses : les corps de requête passent par des objets DTO validés, tandis que les réponses sont filtrées par des vues Jackson. L'autorisation est portée par les méthodes des contrôleurs au moyen des annotations de rôle, sans configuration globale des routes.

## Prérequis

- Java JDK 17.
- Docker et Docker Compose, pour la base PostgreSQL et l'exécution conteneurisée.
- Le wrapper Maven fourni dans le dépôt (`mvnw`), aucune installation de Maven n'est nécessaire.

## Configuration

L'application lit sa configuration depuis `application.properties`, avec des valeurs par défaut adaptées au développement local. Chaque valeur sensible peut être surchargée par une variable d'environnement.

| Variable | Valeur par défaut | Description |
|---|---|---|
| SPRING_DATASOURCE_URL | jdbc:postgresql://localhost:5432/locmns | URL de la base PostgreSQL |
| SPRING_DATASOURCE_USERNAME | locmns_user | Utilisateur de la base |
| SPRING_DATASOURCE_PASSWORD | locmns_pass | Mot de passe de la base |
| JWT_SECRET | clé de développement | Clé de signature des jetons JWT |
| CORS_ALLOWED_ORIGINS | http://localhost:4200 | Origines autorisées (liste séparée par des virgules) |
| MAIL_USERNAME | locmns.noreply@gmail.com | Identifiant SMTP |
| MAIL_PASSWORD | vide | Mot de passe SMTP |

Toutes les routes de l'API sont préfixées par `/api` (propriété `server.servlet.context-path`). En développement le service écoute donc sur `http://localhost:8080/api`.

## Lancement en développement

La base de données est fournie par un conteneur PostgreSQL. L'application peut ensuite être lancée localement par Maven.

Démarrer la base seule :

```bash
docker compose up -d postgres
```

Lancer l'application :

```bash
./mvnw spring-boot:run
```

L'API est alors disponible sur `http://localhost:8080/api`. Au premier démarrage, le schéma est généré par Hibernate puis le fichier `data.sql` insère un jeu de données de démonstration.

Le stack de développement complet (base et back-end conteneurisés) peut aussi être lancé en une commande :

```bash
docker compose up --build
```

## Documentation de l'API

Une documentation interactive est exposée par springdoc OpenAPI :

- Interface Swagger : `http://localhost:8080/api/swagger-ui.html`
- Spécification JSON : `http://localhost:8080/api/v3/api-docs`

Une collection Postman complète est disponible dans le dossier `postman`.

## Aperçu des points d'entrée

| Méthode et chemin | Rôle requis | Description |
|---|---|---|
| POST /api/login | public | Authentifie et renvoie un jeton JWT |
| GET /api/user/me | authentifié | Renvoie l'utilisateur connecté |
| GET /api/equipment/list | authentifié | Liste les équipements avec statut calculé |
| GET /api/equipment/available | authentifié | Liste les équipements disponibles sur une période |
| POST /api/loan | authentifié | Crée une demande d'emprunt |
| PUT /api/loan/{id}/validate | gestionnaire | Valide un emprunt |
| PUT /api/loan/{id}/return | gestionnaire | Enregistre le retour d'un emprunt |
| POST /api/event | authentifié | Signale un événement sur un emprunt |

La liste exhaustive des points d'entrée est consultable dans Swagger.

## Tests

Deux niveaux de tests cohabitent et sont séparés par leur suffixe.

Tests unitaires (suffixe `Test`), sans base de données, exécutés par Surefire :

```bash
./mvnw test
```

Tests d'intégration (suffixe `IT`), sur une base PostgreSQL réelle, exécutés par Failsafe. La base doit être démarrée au préalable :

```bash
docker compose up -d postgres
./mvnw verify
```

## Déploiement en production

Le fichier `docker-compose.prod.yml` décrit un déploiement à trois services : base PostgreSQL, back-end et front-end servi par nginx. Seul le front est exposé publiquement sur le port 80 ; le back et la base restent sur le réseau interne. Les images proviennent de Docker Hub et la configuration est fournie par un fichier `.env`.

```bash
docker compose -f docker-compose.prod.yml up -d
```

En production, l'origine autorisée pour le CORS est fournie par la variable `CORS_ALLOWED_ORIGINS` et la clé `JWT_SECRET` doit être remplacée par une valeur forte et aléatoire.

## Stratégie de gestion de versions

Le projet suit un modèle à trois niveaux de branches :

- `main` : version stable et validée.
- `develop` : branche d'intégration du travail courant.
- `feature/*` : une branche par fonctionnalité, fusionnée dans `develop` après revue.

Les messages de commit suivent la convention Conventional Commits.

## Auteur

GODFRIN Thomas, formation Concepteur Développeur d'Applications, Metz Numeric School, promotion 2025-2026.
