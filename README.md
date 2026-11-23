# ☕ Itinireves – Application Desktop Java

Projet développé dans le cadre du module **Projet Intégré : Développement Java\_\_3A26**
à **Esprit School of Engineering**, année universitaire 2024–2025.

---

## Table des matières

* [Présentation](#présentation)
* [Modules Fonctionnels](#modules-fonctionnels)
* [Fonctionnalités Avancées](#fonctionnalités-avancées)
* [Technologies Utilisées](#technologies-utilisées)
* [Services & APIs Intégrés](#services--apis-intégrés-srcmainjava)
* [Topics](#topics)
* [Structure du Projet](#structure-du-projet)
* [Installation](#installation)
* [Statistiques Intégrées](#statistiques-intégrées)
* [Contribution](#contribution)
* [Remerciements](#remerciements)

---

## Présentation

**Itinireves** est une application desktop JavaFX modulaire permettant à des utilisateurs de :

* Réserver des **packs voyages**, **vols**, **hôtels** et **excursions**
* Participer à des **événements VIP**
* Donner des **avis** après leurs expériences
* Gérer leur profil et visualiser leur historique
* Bénéficier d’une **expérience de paiement fluide et sécurisée**

Côté utilisateur, une interface locale ergonomique permet la gestion des réservations et des statistiques personnelles.

---

## Modules Fonctionnels

## 🧹 Modules Fonctionnels

### 👤 Utilisateurs

* Inscription, connexion (gestion locale de session)
* Modification de profil, historique des réservations
* Statuts de paiement (payée / non payée)
* Rôles utilisateur (client, admin)

### 🋦 Vols

* Consultation des vols disponibles
* Filtres dynamiques (prix, date, compagnie)
* Réservation intégrée ou indépendante
* Paiement local + récapitulatif PDF

### 🏨 Hôtels

* Liste filtrable par ville, prix, type
* Affichage des détails (photos, équipements)
* Réservation flexible multi-nuits
* Génération de confirmation PDF

### 🗺️ Excursions

* Circuits et trajets culturels ou nature
* Affichage cartographique, nombre de places
* Réservation directe ou pack
* Itinéraire PDF + QR Code à présenter

### 🎉 Événements VIP

* Réservés aux utilisateurs éligibles
* Gestion des quotas et invitations
* Notification en popup JavaFX
* Affichage dynamique des événements

### 🏛️ Packs Voyage

* Offre groupée : vol + hôtel + excursion
* Réservation unique via interface fluide
* Paiement unique (PayPal Java)
* Billet PDF + QR Code généré automatiquement

### 📰 Posts / Annonces

* Cartes promotionnelles dans l’interface principale
* Détails en modale
* Système de recherche (titre, lieu)
* Gestion locale ou via API backend

### 💬 Feedback

* Avis post-expérience (notation + commentaire)
* Modération locale ou centralisée
* Intégration dans les statistiques globales

---

## Fonctionnalités Avancées

* Graphiques JavaFX dynamiques
* Notifications internes et alertes
* Génération automatique de billets PDF
* Paiement intégré (PayPal Java SDK)
* QR Code avec ZXing
* Synchronisation des images avec Symfony
* Gestion de session persistante locale

---

## Technologies Utilisées

* Java 17
* JavaFX 17
* Maven
* JDBC / MySQL
* iText PDF, ZXing QRCode
* Gson, Apache HttpClient
* SceneBuilder, JFoenix

---

## Services & APIs Intégrés (`src/main/java`)

| Service/API     | Rôle                                |
| --------------- | ----------------------------------- |
| PayPal Java SDK | Paiement sécurisé                   |
| ImageUtils      | Copie et synchronisation des images |
| PdfGenerator    | Génération de billets PDF           |
| QrCodeGenerator | QR Code de confirmation             |
| UnsplashService | Images automatiques (posts, packs)  |
| SessionManager  | Gestion de la session               |

---

## Topics

`java` `javafx` `esprit` `travel` `packs` `flight-booking` `hotel-reservation`
`excursion` `event-management` `paypal` `user-feedback` `desktop-app`

---

## Structure du Projet

```plaintext
PIDEV/
├── .idea/
├── src/
│   └── main/
│       ├── java/
│       │   ├── controller/
│       │   ├── entities/
│       │   ├── services/
│       │   ├── tests/
│       │   └── utils/
│       └── resources/
├── .gitignore
├── pom.xml

```

---

## Installation

Cloner le projet :

```bash
git clone https://github.com/aymen74407/PIDEV.git
```

Ouvrir le projet dans **IntelliJ IDEA** (JDK 17)

Configurer la base MySQL dans `DatabaseConnection.java` :

```java
url = "jdbc:mysql://localhost:3306/basecommune";
```

Puis taper en terminal :  mvn javafx:run


---

## Statistiques Intégrées

* Réservations mensuelles (LineChart)
* Top utilisateurs les plus actifs (BarChart)
* Popularité des modules (PieChart)

---

## Contribution

Fork du projet
Créer une branche :

```bash
git checkout -b feature/ma-feature
```

Commit :

```bash
git commit -m "Ajout fonctionnalité"
git push origin feature/ma-feature
```

Puis ouvrir une Pull Request sur GitHub

---

## Remerciements

Projet réalisé sous la supervision de l’équipe pédagogique
**Esprit School of Engineering – Module Projet Intégré : Développement Java 3A26**

 "Chaque ligne de code rapproche un peu plus du départ." 🚀
"# TuniTravelDesktop" 
