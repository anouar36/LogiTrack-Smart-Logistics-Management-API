# LogiTrack API - Phase de Sécurité

## 📋 Description du Projet
Ce module vise à sécuriser l'API logistique **LogiTrack** en implémentant une couche de sécurité robuste basée sur **Spring Security 6**.
L'objectif principal de cette phase est de mettre en place une authentification **Basic Auth** et une gestion des autorisations par rôles (RBAC) pour protéger les endpoints critiques du système.

## 🚀 Fonctionnalités Clés
- **Authentification Stateless :** Utilisation du mécanisme HTTP Basic Auth.
- **Contrôle d'Accès (RBAC) :** Gestion des droits basée sur des rôles spécifiques :
    - `ADMIN` : Accès complet au système.
    - `WAREHOUSE_MANAGER` : Gestion des stocks et expéditions.
    - `CLIENT` : Gestion des commandes de vente.
- **Sécurisation des Mots de Passe :** Hachage des mots de passe avec l'algorithme **BCrypt**.
- **Protection des Endpoints :** Configuration fine des accès via `SecurityFilterChain`.

## 🛠️ Stack Technique
- **Langage :** Java 17+
- **Framework :** Spring Boot 3
- **Sécurité :** Spring Security 6
- **Base de Données :** PostgreSQL
- **Outils :** Maven, Git, Postman

## ⚙️ Installation et Démarrage

### 1. Cloner le dépôt
```bash
git clone [https://github.com/ton-username/logitrack.git](https://github.com/ton-username/logitrack.git)
cd logitrack