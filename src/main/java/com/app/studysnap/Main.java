package com.app.studysnap;

import com.app.studysnap.model.SqliteQuizDAO;
import com.app.studysnap.model.SqliteUserDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.app.studysnap.model.User;

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
        //SqliteQuizDAO quizDAO = new SqliteQuizDAO();
        //userDAO.resetUsersTable();
        //quizDAO.resetQuizzesTable();
        //userDAO.seedMockUsers();

        //DEBUGGING REASONS: Print users from DB
        for (User user : userDAO.getAllUsers()) {
            System.out.println(user.getUserId() + " | " + user.getUsername() + " | " + user.getEmail() + " | " + user.getAuthProvider());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}