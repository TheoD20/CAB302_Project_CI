package com.app.studysnap.controllers;

import com.app.studysnap.auth.Session;
import com.app.studysnap.model.*;
import com.app.studysnap.services.BadgeRenderer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;
import java.util.Objects;

/**
 * Controller responsible for rendering the full badge catalog for the current user.
 */
public class BadgesController {

    // Fxml
    @FXML private FlowPane flow;

    /**
     * Default constructor:
     * Creates a new {@code BadgesController}.
     */
    public BadgesController() {}

    /**
     * JavaFX initialization method that loads badges and renders the grid view.
     * <p>
     * After loading DAOs it resolves the current session user, queries all
     * badges, and renders each with an earned/locked state.
     * </p>
     */
    public void initialize() {

        IBadgeDAO badgeDAO;

        try { badgeDAO = new SqliteBadgeDAO(); } catch (Throwable t) { badgeDAO = null; }
        IBadgeProgressDAO badgeProgressDAO;
        try { badgeProgressDAO = new SqliteBadgeProgressDAO(); } catch (Throwable t) { badgeProgressDAO = null; }

        // Get current user
        User user = Session.getCurrentUser();
        if (user == null) {
            return;
        }

        flow.getChildren().clear();

        List<Badge> all = null;
        if (badgeDAO != null) {
            all = badgeDAO.getAllBadges();
        }

        if (all == null || all.isEmpty()) {
            flow.getChildren().add(new Label("No badges available yet."));
            return;
        }

        final int userId = user.getUserId();

        for (Badge b : all) {
            Node card = BadgeRenderer.buildCard(b, user, badgeProgressDAO, true);

            boolean earned = userId > 0 && Objects.requireNonNull(badgeProgressDAO).isEarned(userId, b.getBadgeId());
            card.getStyleClass().add(earned ? "earned" : "locked");

            flow.getChildren().add(card);
        }
    }
}
