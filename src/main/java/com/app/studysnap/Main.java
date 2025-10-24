package com.app.studysnap;

import com.app.studysnap.model.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX entry point for StudySnap.
 * <p>Loads the initial FXML and applies window defaults.
 * A guarded, optional "dev seeding" section can reset and pre-populate the DB.
 * </p>
 */
public class Main extends Application {
    /** Window title displayed in the application frame. */
    public static final String TITLE = "StudySnap";

    /** Default application window width in pixels. */
    public static final int WIDTH = 1280;

    /** Default application window height in pixels. */
    public static final int HEIGHT = 720;

    /**
     * Creates a new {@code Main} application instance.
     */
    public Main() {}

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("signup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);

        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();

        /* ADMIN/SETUP REASONS:

        //Database logic
        SqliteUserDAO userDAO = new SqliteUserDAO();
        SqliteBadgeDAO badgeDAO = new SqliteBadgeDAO();
        SqliteBadgeProgressDAO badgeProgressDAO = new SqliteBadgeProgressDAO();
        SqliteQuizDAO quizDAO = new SqliteQuizDAO();

        // Reset db
        userDAO.resetUsersTable();
        quizDAO.resetQuizzesTable();

        // Seed badges and users
        badgeDAO.initializeBadges();
        userDAO.seedMockUsers();

        // Print users from DB
        for (User user : userDAO.getAllUsers()) {
            System.out.println(user.getUserId() + " | " + user.getUsername() + " | " + user.getEmail() + " | " + user.getAuthProvider());
        }

        //*/
    }

    /**
     * Standard Java entry point; delegates to {@link Application#launch(String...)}.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}