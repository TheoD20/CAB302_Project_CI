package com.app.studysnap.controllers;

import com.app.studysnap.model.*;
import com.app.studysnap.services.BadgeRenderer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;

import java.util.Collections;
import java.util.List;

import static com.app.studysnap.services.TextParser.trim;

/**
 * Controller responsible for displaying newly earned badges in a grid
 */
public class BadgeAwardController {
    @FXML
    private FlowPane flow;
    @FXML
    private Label congratsSub;

    private List<Badge> newBadges;
    private SqliteBadgeProgressDAO badgeProgressDAO;
    private User user;

    /**
     * Initializes the view with the newly earned badges and supporting context,
     * then renders the badge grid and updates the subtitle.
     * @param b The list of newly earned {@link Badge}s; if {@code null}, an empty list is used
     * @param dao The badge progress DAO to query progress for rendering
     * @param u The current {@link User} associated with the earned badges
     */
    public void setupNewBadgeDisplay(List<Badge> b, SqliteBadgeProgressDAO dao, User u) {
        newBadges = (b == null) ? Collections.emptyList() : b;
        badgeProgressDAO = dao;
        user = u;
        render();
        updateCongratsSub();
    }

    /**
     * Renders the badge cards into the flow container.
     * If the view is not yet injected, the method returns safely.
     */
    private void render() {
        if (flow == null) return;
        flow.getChildren().clear();

        for (Badge b : newBadges) {
            Node card = BadgeRenderer.buildCard(b, user, badgeProgressDAO, true);
            card.getStyleClass().add("earned");
            flow.getChildren().add(card);
        }
    }

    /**
     * Updates the subtitle with a friendly congratulations message.
     * If the label is not yet injected, the method returns safely.
     */
    private void updateCongratsSub() {
        if (congratsSub == null) return;

        int n = newBadges.size();
        if (n == 1) {
            String name = trim(newBadges.get(0).getBadgeName());
            congratsSub.setText("You’ve earned the " + name + " badge!");
            return;
        }

        // For multiple, show a short list
        String names = newBadges.stream()
                .map(b -> trim(b.getBadgeName()))
                .limit(3)
                .collect(java.util.stream.Collectors.joining(", "));

        // compress if more than 3
        if (n > 3) names += "…";

        congratsSub.setText("You’ve earned " + n + " new badges: " + names);
    }
}
