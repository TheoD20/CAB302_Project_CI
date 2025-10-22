package com.app.studysnap.services;

import com.app.studysnap.model.Badge;
import com.app.studysnap.model.IBadgeProgressDAO;
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

import static com.app.studysnap.services.TextParser.*;

/**
 * Utility for rendering {@link Badge} instances into JavaFX nodes.
 * <p>
 * Produces a compact or detailed badge card (title, icon, description, and optional
 * progress bar) ready to be added to a {@code FlowPane} or similar container.
 * </p>
 */
public class BadgeRenderer {

    /**
     * Builds a visual card for a single badge, optionally showing progress for a given user.
     * @param b the badge to render
     * @param u the user for whom to compute earned/progress state
     * @param progressDAO DAO used to read progress/goal/earned flags (can be {@code null})
     * @param showProgress whether to include a progress bar
     * @return a JavaFX node representing the badge
     */
    public static Node buildCard(Badge b, User u, IBadgeProgressDAO progressDAO, boolean showProgress) {

        int userId = u.getUserId();

        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(200);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("badge");

        if (!showProgress) {
            card.getStyleClass().add("compact");
        }

        // Title
        Label title = new Label(trim(b.getBadgeName()));
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.getStyleClass().add("card-title");

        // Badge Image
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

        // Description
        Text desc = new Text(trim(b.getBadgeDescription()));
        desc.setWrappingWidth(180);
        desc.setTextAlignment(TextAlignment.CENTER);
        desc.getStyleClass().add("card-desc");

        // Badge progress visual
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(160);
        progressBar.setPrefHeight(8);
        progressBar.getStyleClass().add("progress");

        boolean earned = false;
        int progress = 0;
        int goal = 0;

        if (progressDAO != null && userId > 0) {
            try {
                progress = progressDAO.getProgress(userId, b.getBadgeId());
                goal    = progressDAO.getGoal(userId, b.getBadgeId());
                earned  = progressDAO.isEarned(userId, b.getBadgeId());
            } catch (Exception ignored) { }
        }

        // Set progress
        double pct = (goal > 0) ? Math.min(1.0, (double) progress / goal) : 0.0;
        progressBar.setProgress(pct);

        progressBar.setVisible(showProgress);
        progressBar.setManaged(showProgress);

        // Earned/locked state classes for CSS
        card.getStyleClass().add(earned ? "earned" : "locked");

        // Assemble
        if (showProgress) {
            card.getChildren().addAll(image, title, progressBar, desc);
        } else {
            card.getChildren().addAll(image, title, desc);
        }
        return card;
    }
}