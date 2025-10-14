package com.app.studysnap;

import com.app.studysnap.model.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static final String TITLE = "StudySnap";
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("signup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);

        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();

        //Database logic
        SqliteUserDAO userDAO = new SqliteUserDAO();
        SqliteBadgeDAO badgeDAO = new SqliteBadgeDAO();
        SqliteBadgeProgressDAO progressDAO = new SqliteBadgeProgressDAO();
        SqliteQuizDAO quizDAO = new SqliteQuizDAO();

        // Reset db
        userDAO.resetUsersTable();
        quizDAO.resetQuizzesTable();

        // Seed badges and users
        badgeDAO.initializeBadges();
        userDAO.seedMockUsers();

        //DEBUGGING REASONS: Print users from DB
        for (User user : userDAO.getAllUsers()) {
            System.out.println(user.getUserId() + " | " + user.getUsername() + " | " + user.getEmail() + " | " + user.getAuthProvider());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}