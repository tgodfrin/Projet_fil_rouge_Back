# LOC MNS — Back-end Spring Boot

Application web de gestion du parc informatique et matériel de Metz Numeric School (MNS).
Développée dans le cadre de la formation Concepteur Développeur d'Applications (CDA) 2025-2026.

---

## 📋 Description du projet

API REST back-end de LOC MNS permettant de gérer l'ensemble du parc informatique
et matériel mis à disposition des étudiants, stagiaires et intervenants de MNS.

### Fonctionnalités principales
- Authentification JWT et gestion des rôles (Gestionnaire, Collaborateur, Intervenant, Stagiaire)
- Gestion des équipements (CRUD, états calculés, catégories)
- Gestion des catégories de matériel (CRUD) et des droits d'emprunt par rôle
- Gestion des emprunts (demande, validation, refus, retour, emprunts groupés)
- Signalement d'événements (panne, retour anticipé, prolongation)
- Système d'alertes et notifications
- Export des données (CSV, XML)

---

## 🛠️ Stack technique

| Technologie | Version | Rôle |
|---|---|---|
| Java | 17 LTS | Langage back-end |
| Spring Boot | 3.4.4 | Framework back-end |
| Spring Security | intégré | Sécurité et authentification |
| Spring Data JPA | intégré | Persistance des données |
| Hibernate | intégré | ORM |
| jjwt | 0.12.6 | Génération / vérification des tokens JWT |
| PostgreSQL | 16 | Base de données |
| Maven | 3.x | Gestionnaire de dépendances |
| Lombok | dernière stable | Réduction du code boilerplate |

---

## ⚙️ Prérequis

- Java JDK 17 LTS
- Maven ou Maven Wrapper (`./mvnw`)
- Docker Desktop (pour PostgreSQL)

---

## 🚀 Installation et lancement
```bash
# Cloner le dépôt
git clone https://github.com/tgodfrin/Projet_fil_rouge_Back.git

# Aller dans le dossier
cd Projet_fil_rouge_Back

# Lancer PostgreSQL via Docker
docker run --name locmns-db -e POSTGRES_USER=locmns_user -e POSTGRES_PASSWORD=locmns_pass -e POSTGRES_DB=locmns -p 5432:5432 -d postgres:16

# Lancer l'application
./mvnw spring-boot:run
```

API accessible sur : **http://localhost:8080**

---

## 🌿 Stratégie Git
```
main                        → version stable et validée
  └── develop               → branche de travail principal
        ├── feature/login
        ├── feature/equipment
        └── feature/loan
```

---

## 🔗 Liens utiles

- Front-end Angular : [lien vers le dépôt front]
- Maquettes Figma : [lien vers Figma]
- Tableau Trello : [lien vers Trello]

---

## 👤 Auteur

GODFRIN Thomas— Formation CDA — Metz Numeric School 2025-2026