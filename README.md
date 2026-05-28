# StudySnap

StudySnap is a JavaFX desktop application that helps users turn study material into interactive quizzes. Users can generate multiple-choice quizzes from uploaded files, pasted text, or topic prompts, save quizzes to a local SQLite database, play quizzes, track results, and earn badges for progress.

The project focuses on building a complete desktop learning app with a clean Java structure, persistent storage, authentication, AI-assisted quiz generation, and a polished JavaFX user interface.

## Features

* User signup and login with email/password
* Optional Google OAuth login
* Secure password hashing with BCrypt
* Quiz generation from PDF or TXT uploads
* Quiz generation from pasted text
* Quiz generation from topic prompts using Google Gemini
* Save generated quizzes to a local SQLite database
* Mark quizzes as private or public
* Search and download public quizzes
* Edit quiz metadata and questions
* Play quizzes with five-option multiple-choice questions
* Track quiz score and completion time
* View quiz results and restart attempts
* Export quizzes to PDF
* Profile page with avatar support and progress statistics
* Badge system for learning milestones and achievements
* JUnit test coverage for core model, DAO, authentication, and service logic

## Documentation

Generated Java documentation is available through GitHub Pages:

[View StudySnap JavaDocs](https://theod20.github.io/StudySnap_CI/com.app.studysnap/module-summary.html)

## Tech Stack

| Area               | Technology                                           |
| ------------------ | ---------------------------------------------------- |
| Language           | Java                                                 |
| UI                 | JavaFX + FXML                                        |
| Build Tool         | Maven                                                |
| Database           | SQLite                                               |
| Authentication     | Email/password + Google OAuth                        |
| Password Security  | BCrypt                                               |
| AI Quiz Generation | Google Gemini API                                    |
| PDF Handling       | Apache PDFBox                                        |
| Testing            | JUnit 5                                              |
| Architecture       | MVC-style JavaFX controllers, DAOs, services, models |

## Project Structure

```text
StudySnap/
├── src/
│   ├── main/
│   │   ├── java/com/app/studysnap/
│   │   │   ├── Main.java
│   │   │   ├── auth/          # Authentication, Google OAuth, session handling
│   │   │   ├── controllers/   # JavaFX controllers for each FXML screen
│   │   │   ├── exceptions/    # Custom application exceptions
│   │   │   ├── model/         # Domain models and SQLite DAO classes
│   │   │   └── services/      # Quiz generation, PDF export, navigation, badges, parsing
│   │   └── resources/com/app/studysnap/
│   │       ├── *.fxml         # JavaFX layouts
│   │       ├── images/        # Logo, avatars, badge icons
│   │       └── styles/        # JavaFX CSS files
│   └── test/java/com/app/studysnap/
│       ├── auth/
│       ├── model/
│       └── services/
├── Wireframes/                # Early UI planning and design sketches
├── UserStories/               # Product planning notes and feature requirements
├── pom.xml
├── mvnw
└── mvnw.cmd
```

## Core Application Areas

### Quiz Generation

StudySnap can generate quiz questions in three ways:

1. Uploading a PDF or TXT file
2. Pasting study notes directly into the app
3. Entering a topic prompt

The app sends the source content to the Gemini API, receives structured multiple-choice questions, and parses the output into question objects that can be previewed, edited, saved, played, or exported.

### Quiz Management

Users can save generated quizzes as study decks. Each quiz stores a title, subject, description, privacy setting, and a list of questions. Saved quizzes appear on the dashboard and can be edited later.

Public quizzes can be searched and downloaded by other users, while private quizzes remain visible only to the creator.

### Quiz Play and Results

The quiz player presents each question with five answer options. After completing a quiz, users can view their score, completion time, and result summary. Attempts are saved so progress can be tracked over time.

### User Profiles and Badges

The profile screen shows user details, avatar controls, quiz statistics, accuracy information, and activity history. The badge system rewards users for milestones such as creating quizzes, completing attempts, improving accuracy, and maintaining consistency.

### Data Persistence

The application uses SQLite through DAO classes. The main database tables include:

* `Users`
* `Quizzes`
* `Questions`
* `QuizAttempts`
* `Badges`
* `BadgeProgress`

This keeps the app lightweight and easy to run locally without requiring a separate database server.

## Environment Variables

Some features require API credentials.

### Gemini Quiz Generation

Required for AI quiz generation:

```bash
GEMINI_API_KEY=your_gemini_api_key
```

### Google OAuth Login

Required for Google login:

```bash
STUDYSNAP_GOOGLE_CLIENT_ID=your_google_client_id
STUDYSNAP_GOOGLE_CLIENT_SECRET=your_google_client_secret
```

Optional:

```bash
STUDYSNAP_GOOGLE_OAUTH_PORT=8888
```

Email/password authentication works without Google OAuth credentials.

## How to Run

### Prerequisites

* Java JDK 21, or another compatible JDK version
* Maven, or the included Maven Wrapper

Check Java is installed:

```bash
java -version
```

### Windows

From the project root:

```bash
.\mvnw.cmd clean javafx:run
```

### macOS / Linux

From the project root:

```bash
chmod +x mvnw
./mvnw clean javafx:run
```

If the Maven wrapper fails because of line-ending issues, run Maven directly if it is installed:

```bash
mvn clean javafx:run
```

## Running Tests

Windows:

```bash
.\mvnw.cmd test
```

macOS / Linux:

```bash
./mvnw test
```

The test suite covers authentication, sessions, model classes, SQLite DAO logic, PDF/text services, quiz rendering, and parsing behaviour.

## Design Focus

This project was built around a few key design goals:

* Keep UI logic inside JavaFX controllers
* Keep business logic inside service classes
* Keep database access behind DAO interfaces and SQLite implementations
* Use custom exceptions for validation, authentication, data access, and external service failures
* Keep quiz generation, parsing, rendering, exporting, and persistence as separate responsibilities
* Make the app usable as a complete desktop application rather than only a code exercise

## Notes on Current Limitations

StudySnap is a local desktop application, not a production web service. It uses a local SQLite database and environment variables for API configuration.

AI-generated quiz content may still need manual review, which is why the app includes preview and editing tools before saving a generated quiz.

## Future Improvements

Potential improvements include:

* Add installer/build packaging for easier desktop distribution
* Add a first-run database setup flow
* Add stronger validation around generated AI quiz formatting
* Improve public quiz discovery with tags and categories
* Add spaced repetition scheduling
* Add richer analytics for long-term study progress
* Add cloud sync so users can access quizzes across devices
* Add CI workflows to run tests automatically on GitHub

## Contributors

Maintained by Theo Negrao.

Original contributors:

* Theo Negrao
* Yuta Matsuzaki
* Nur Syazeera Binti Shatri
* Nicolas Ruiz Guarin
