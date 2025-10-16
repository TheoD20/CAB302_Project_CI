package com.app.studysnap.controllers;

import com.app.studysnap.model.*;
import com.app.studysnap.services.BadgeRenderer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;

import java.util.Collections;
import java.util.List;

public class BadgeAwardController {
    @FXML
    private FlowPane flow;
    @FXML
    private Label congratsSub;

    private List<Badge> newBadges;
    private SqliteBadgeProgressDAO badgeProgressDAO;
    private User user;

    // set new badges
    public void setupNewBadgeDisplay(List<Badge> b, SqliteBadgeProgressDAO dao, User u) {
        newBadges = (b == null) ? Collections.emptyList() : b;
        badgeProgressDAO = dao;
        user = u;
        render();
        updateCongratsSub();
    }

    // Load badges and render grid
    private void render() {
        if (flow == null) return;
        flow.getChildren().clear();

        for (Badge b : newBadges) {
            Node card = BadgeRenderer.buildCard(b, user, badgeProgressDAO, true);
            card.getStyleClass().add("earned");
            flow.getChildren().add(card);
        }
    }

    private void updateCongratsSub() {
        if (congratsSub == null) return;

        int n = newBadges.size();
        if (n == 1) {
            String name = safe(newBadges.get(0).getBadgeName());
            congratsSub.setText("You’ve earned the " + name + " badge!");
            return;
        }

        // For multiple, show a short list
        String names = newBadges.stream()
                .map(b -> safe(b.getBadgeName()))
                .limit(3)
                .collect(java.util.stream.Collectors.joining(", "));

        // compress if more than 3
        if (n > 3) names += "…";

        congratsSub.setText("You’ve earned " + n + " new badges: " + names);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
