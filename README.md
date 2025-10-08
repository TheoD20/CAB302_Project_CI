# CAB302 Project

Java/JavaFX application focused on improving learning.

Group Members:
- Theo Negrao
- Yuta Matsuzaki
- Nur Syazeera Binti Shatri
- Nicolas Ruiz Guarin

---

## Features
- AI flashcard/quiz generation
- gamified learning experience
- multiplayer interaction / study sessions
- decks sharing
- study group performance competition

---

## Tech stack

| Area                | Tools                   |
|---------------------|-------------------------|
| Java                | OpenJDK 21 (Corretto)   |
| GUI (Client)        | JavaFX                  |
| Auth                | OAuth + simple password |
| Password encryption | Bcrypt                  |
| Database            | SQLite                  |
| AI                  | Gemini Java SDK         |
| Build & Test        | Maven, JUnit            |
| Version Control     | GitHub                  |

---

## Project Structure
```
src/
 └── test/
 └── main/
     ├── java/
     │   ├── module-info.java             # module declarations (requires javafx.controls, javafx.fxml)
     │   └── com.app.studysnap/
     │       ├── Main.java                # entry point, launches UI
     │       └── controllers/             # different controllers bound to FXML
     │       └── auth/                    # authentication manager + sessions
     │       └── model/                   # object classes
     │       └── services/                # API, pdf handeling, navigation, Popups, Async
     │       └── exceptions/              # Custom exceptions
     └── resources/
         └── com.app.studysnap/
             ├── content.fxml             # UI layout files
             ├── images/                  # Static images
             └── styles/                  # CSS files
```