package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.auth.Session;
import com.app.studysnap.model.IQuizDAO;
import com.app.studysnap.model.Quiz;
import com.app.studysnap.model.SqliteQuizDAO;
import com.app.studysnap.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.List;

public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private FlowPane deckContainer;
    @FXML private VBox emptyState;

    private final IQuizDAO dao = new SqliteQuizDAO();

    @FXML
    public void initialize() {
        User u = Session.getCurrentUser();
        String name = (u != null && u.getUsername() != null && !u.getUsername().isBlank())
                ? u.getUsername() : "there";
        welcomeLabel.setText("Welcome, " + name);

        loadMyQuizzes();
    }

    private void loadMyQuizzes() {
        deckContainer.getChildren().clear();

        User u = Session.getCurrentUser();
        if (u == null) {
            showEmpty(true);
            return;
        }

        List<Quiz> mine = dao.getQuizzesByUser(u.getUserId());
        if (mine.isEmpty()) {
            showEmpty(true);
            return;
        }

        showEmpty(false);
        for (Quiz q : mine) {
            deckContainer.getChildren().add(buildCard(q));
        }
    }

    private void showEmpty(boolean show) {
        emptyState.setVisible(show);
        emptyState.setManaged(show);
    }

    private Node buildCard(Quiz q) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(280);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);
            """);

        Label title = new Label(nz(q.getTitle()));
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox meta = new HBox(10);
        Label subject = new Label("📚 " + nz(q.getSubject()));
        subject.setStyle("-fx-text-fill: #444;");
        Label vis = new Label(q.get_is_private() ? "🔒 Private" : "🌐 Public");
        vis.setStyle("-fx-text-fill: #666;");
        meta.getChildren().addAll(subject, vis);

        Text desc = new Text(nz(q.getDescription()));
        desc.setWrappingWidth(256);

        HBox actions = new HBox(8);
        Button openBtn = new Button("Open");
        Button exportBtn = new Button("Export");
        Button editBtn = new Button("Edit");

        openBtn.setOnAction(e -> openInDashboard("quiz_view.fxml", e));
        editBtn.setOnAction(e -> openInDashboard("quiz_edit.fxml", e));
        exportBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION,
                "TODO: Wire exporter for quiz_id=" + q.getQuizId()).showAndWait());

        card.getChildren().addAll(title, meta, desc, actions);
        return card;
    }

    // Empty-state button -> go to quiz generator inside the dashboard
    @FXML
    private void onCreateQuiz() {
        // Load quizgen.fxml into the dashboard content area
        openInDashboard("quizgen.fxml", deckContainer);
    }

    /**
     * Inject an FXML into the Dashboard content area without touching Navigator or Stage.
     * Requires dashboard.fxml to set: <StackPane fx:id="contentArea" id="contentArea">
     */
    private void openInDashboard(String fxml, Object anyChildNode) {
        try {
            // Get any node to walk up to the scene/root
            Node any = (anyChildNode instanceof Node n) ? n : deckContainer;
            BorderPane dashRoot = (BorderPane) any.getScene().getRoot();
            StackPane contentArea = (StackPane) dashRoot.lookup("#contentArea"); // CSS id lookup
            if (contentArea == null) throw new IllegalStateException("contentArea not found. Did you add id=\"contentArea\" in dashboard.fxml?");
            Node view = FXMLLoader.load(Main.class.getResource(fxml));
            contentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open " + fxml + ".").showAndWait();
        }
    }

    private void openInDashboard(String fxml, javafx.event.ActionEvent e) {
        openInDashboard(fxml, (Node) e.getSource());
    }

    private String nz(String s) { return s == null ? "" : s; }
}