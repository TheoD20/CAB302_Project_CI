package com.app.studysnap.services;

import com.app.studysnap.model.Badge;
import com.app.studysnap.model.IBadgeProgressDAO;
import com.app.studysnap.model.SqliteBadgeProgressDAO;
import com.app.studysnap.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.FileInputStream;
import java.util.Objects;

public class BadgeRenderer {
    // Builds a visual card for a single badge to be injected into a FlowPane
    public static Node buildCard(Badge b, User u, IBadgeProgressDAO progressDAO, boolean showProgress) {

        int userId = u.getUserId();

        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(200);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("card");

        // === Title ===
        Label title = new Label(safe(b.getBadgeName()));
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.getStyleClass().add("card-title");

        // === Badge Image ===
        ImageView image = new ImageView();
        try {
            Image img = new Image(new FileInputStream(b.getBadgeIconPath()), 96, 96, true, true);
            image.setImage(img);
        } catch (Exception e) {
            // Fallback icon if image not found
            image.setImage(new Image(
                    Objects.requireNonNull(BadgeRenderer.class.getResourceAsStream("/com/app/studysnap/images/badges/default.png")),
                    96, 96, true, true
            ));
        }
        image.setPreserveRatio(true);
        image.setFitWidth(96);
        image.setFitHeight(96);
        image.getStyleClass().add("badge-icon");

        // === Description ===
        Text desc = new Text(safe(b.getBadgeDescription()));
        desc.setWrappingWidth(180);
        desc.setTextAlignment(TextAlignment.CENTER);
        desc.getStyleClass().add("card-desc");

        // === badge progress visual ===
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(160);
        progressBar.getStyleClass().add("badge-progress");

        boolean earned = false;
        int progress = 0;
        int goal = 0;

        if (progressDAO != null && userId > 0) {
            try {
                progress = progressDAO.getProgress(userId, b.getBadgeId());
                goal = progressDAO.getGoal(userId, b.getBadgeId()); // your DAO returns int
                earned = progressDAO.isEarned(userId, b.getBadgeId());
            } catch (Exception ignored) { /* fail softly */ }
        }

        double pct = (goal > 0) ? Math.min(1.0, (double) progress / goal) : 0.0;
        progressBar.setProgress(pct);

        // Hide progress bar for goal-less / event-only badges
        if (showProgress) {
            showProgress = goal > 0;
        }

        progressBar.setVisible(showProgress);
        progressBar.setManaged(showProgress);

        // Earned/locked state classes for CSS
        if (earned) {
            card.getStyleClass().add("earned");
        } else {
            card.getStyleClass().add("locked");
        }

        // Assemble
        if (showProgress) {
            card.getChildren().addAll(image, title, progressBar, desc);
        } else {
            card.getChildren().addAll(image, title, desc);
        }
        return card;
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
