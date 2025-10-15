package com.app.studysnap.controllers;

import com.app.studysnap.auth.Session;
import com.app.studysnap.model.*;
import com.app.studysnap.services.BadgeRenderer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class BadgesController {

    @FXML private FlowPane flow;

    private IBadgeDAO badgeDAO;
    private IBadgeProgressDAO badgeProgressDAO;
    private User user;

    // Load badges and render grid
    public void initialize() {
        try { badgeDAO = new SqliteBadgeDAO(); } catch (Throwable t) { badgeDAO = null; }
        try { badgeProgressDAO = new SqliteBadgeProgressDAO(); } catch (Throwable t) { badgeProgressDAO = null; }

        // Get current user
        user = Session.getCurrentUser();
        if (user == null) {
            return;
        }

        flow.getChildren().clear();

        List<Badge> all = badgeDAO.getAllBadges();

        if (all == null || all.isEmpty()) {
            flow.getChildren().add(new Label("No badges available yet."));
            return;
        }

        final int userId = (user != null) ? user.getUserId() : -1;

        for (Badge b : all) {
            Node card = BadgeRenderer.buildCard(b, user, badgeProgressDAO, true);

            boolean earned = userId > 0 && badgeProgressDAO.isEarned(userId, b.getBadgeId());
            card.getStyleClass().add(earned ? "earned" : "locked");

            flow.getChildren().add(card);
        }
    }
}
