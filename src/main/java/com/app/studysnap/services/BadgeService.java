package com.app.studysnap.services;

import com.app.studysnap.Main;
import com.app.studysnap.controllers.BadgeAwardController;
import com.app.studysnap.model.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.*;
import java.util.stream.Collectors;

public final class BadgeService {

    private final IBadgeDAO badgeDAO = new SqliteBadgeDAO();
    private final IBadgeProgressDAO progressDAO = new SqliteBadgeProgressDAO();
    private final IAttemptDAO attemptDAO = new SqliteAttemptDAO();
    private final IQuizDAO quizDAO = new SqliteQuizDAO();

    private List<Badge> newBadges;
    private List<Integer> oldBadgeIds;

    // Called after an attempt handle each badge individually
    public void UpdateScoreTypeBadges(Attempt attempt, int unanswered) {
        newBadges = new ArrayList<>();
        oldBadgeIds = new ArrayList<>();

        for (Badge b : progressDAO.getCompletedBadgesByUser(attempt.getUserId())) {
            oldBadgeIds.add(b.getBadgeId());
        }

        handleFirstStep(attempt);
        handleFlawlessSeries(attempt);
        handleAccuracyHero(attempt);
        handleQuickLearner(attempt);
        handleDecaGenius(attempt);
        handleSpeedReader(attempt);
        handlePersistencePays(attempt);
        handleComebackKid(attempt);
        handleExplorer(attempt);
        handleConsistencyMaster(attempt, unanswered);
        handleSevenDayActivityStreak(attempt);
        handleBigBrain(attempt);

        // award any new badges
        awardIfNew(attempt.getUserId());
    }

    // Called after quiz creation handle each badge individually
    // Quiz Creator (Bronze/Silver/Gold)
    public void UpdateCreationTypeBadges(int userId) {
        newBadges = new ArrayList<>();
        oldBadgeIds = new ArrayList<>();

        // snapshot what user already had
        for (Badge b : progressDAO.getCompletedBadgesByUser(userId)) {
            oldBadgeIds.add(b.getBadgeId());
        }

        for (Badge b : badgeDAO.getBadgesByType("creation")) {
            int createdCount = quizDAO.getQuizzesByUser(userId).size();
            progressDAO.setProgress(userId, b.getBadgeId(), Math.min(createdCount, b.getBadgeGoal()));

            addIfEarned(userId, b);
        }

        awardIfNew(userId);
    }

    // ==== Badge handlers ====

    // Handles "First Step" (complete your first quiz)
    private void handleFirstStep(Attempt a) {
        Badge b = byName("First Step"); if (b == null) return;
        progressDAO.setProgress(a.getUserId(), b.getBadgeId(), b.getBadgeGoal());

        addIfEarned(a.getUserId(), b);
    }

    // Handles "Flawless Five/Ten/Twenty" (count of 100% scores)
    private void handleFlawlessSeries(Attempt a) {
        if (percent(a) != 100) return;
        for (Badge b : badgeDAO.getBadgesByType("score")) {
            String n = b.getBadgeName().toLowerCase(Locale.ROOT);
            if (n.contains("flawless")) {
                progressDAO.addProgress(a.getUserId(), b.getBadgeId(), 1);
            }

            addIfEarned(a.getUserId(), b);
        }
    }

    // Handles "Accuracy Hero" (score 100% once)
    private void handleAccuracyHero(Attempt a) {
        Badge b = byName("Accuracy Hero"); if (b == null) return;
        if (percent(a) == 100) {
            progressDAO.setProgress(a.getUserId(), b.getBadgeId(), b.getBadgeGoal());
        }

        addIfEarned(a.getUserId(), b);
    }

    // Handles "Quick Learner" (score ≥80% once)
    private void handleQuickLearner(Attempt a) {
        Badge b = byName("Quick Learner"); if (b == null) return;
        if (percent(a) >= 80) {
            progressDAO.setProgress(a.getUserId(), b.getBadgeId(), b.getBadgeGoal());
        }

        addIfEarned(a.getUserId(), b);
    }

    // Handles "DecaGenius" (complete 10 quizzes with ≥80%)
    private void handleDecaGenius(Attempt a) {
        Badge b = byName("DecaGenius"); if (b == null) return;
        if (percent(a) >= 80) {
            progressDAO.addProgress(a.getUserId(), b.getBadgeId(), 1);
        }

        addIfEarned(a.getUserId(), b);
    }

    // Handles "Speed Reader" (≤2 minutes AND ≥80%)
    private void handleSpeedReader(Attempt a) {
        Badge b = byName("Speed Reader"); if (b == null) return;
        if (a.getTimeTaken() <= 120 && percent(a) >= 80) {
            progressDAO.setProgress(a.getUserId(), b.getBadgeId(), b.getBadgeGoal());
        }

        addIfEarned(a.getUserId(), b);
    }

    // Handles "Persistence Pays" (≥3 attempts on same quiz and at least ≥50%)
    private void handlePersistencePays(Attempt a) {
        Badge b = byName("Persistence Pays"); if (b == null) return;
        // ≥3 attempts on THIS quiz by this user AND latest is passing (>=50%)
        int countForUserOnQuiz = (int) attemptDAO.getAttemptsByQuiz(a.getQuizId())
                .stream().filter(x -> x.getUserId() == a.getUserId()).count();
        if (countForUserOnQuiz >= 3 && percent(a) >= 50) {
            progressDAO.addProgress(a.getUserId(), b.getBadgeId(), 1);
        }

        addIfEarned(a.getUserId(), b);
    }

    // Handles "Comeback Kid" (improve by ≥20 percentage points vs previous attempt on same quiz)
    private void handleComebackKid(Attempt a) {
        Badge b = byName("Comeback Kid"); if (b == null) return;
        // last two attempts for this user on this quiz, most recent first
        var lastTwo = attemptDAO.getAttemptsByQuiz(a.getQuizId()).stream()
                .filter(x -> x.getUserId() == a.getUserId())
                .limit(2) // DAO sorts DESC by attempt_at
                .collect(Collectors.toList());
        if (lastTwo.size() < 2) return;
        int now  = percent(lastTwo.get(0));
        int prev = percent(lastTwo.get(1));
        if (now - prev >= 20) {
            progressDAO.setProgress(a.getUserId(), b.getBadgeId(), b.getBadgeGoal());
        }

        addIfEarned(a.getUserId(), b);
    }

    // Handles "Explorer" (play quizzes from N distinct categories)
    private void handleExplorer(Attempt a) {
        Badge b = byName("Explorer"); if (b == null) return;
        Set<String> subjects = new HashSet<>();
        for (Attempt at : attemptDAO.getAttemptsByUser(a.getUserId())) {
            Quiz q = quizDAO.getQuizById(at.getQuizId());
            if (q != null && q.getSubject() != null && !q.getSubject().isBlank()) {
                subjects.add(q.getSubject().trim());
            }
        }
        progressDAO.setProgress(a.getUserId(), b.getBadgeId(), Math.min(subjects.size(), b.getBadgeGoal()));

        addIfEarned(a.getUserId(), b);
    }

    // Handles "Consistency Master" (streak of attempts with no skipped questions)
    private void handleConsistencyMaster(Attempt a, int unanswered) {
        Badge b = byName("Consistency Master"); if (b == null) return;
        if (unanswered == 0) {
            progressDAO.addProgress(a.getUserId(), b.getBadgeId(), 1);
        } else {
            progressDAO.setProgress(a.getUserId(), b.getBadgeId(), 0); // reset streak
        }

        addIfEarned(a.getUserId(), b);
    }

    // Handles "Persistence" (7-day activity streak)
    private void handleSevenDayActivityStreak(Attempt a) {
        Badge b = byName("Persistence"); if (b == null) return;
        int streak = attemptDAO.getCurrentStreakByUser(a.getUserId()); // your DAO handles streak calc
        progressDAO.setProgress(a.getUserId(), b.getBadgeId(), Math.min(streak, b.getBadgeGoal()));

        addIfEarned(a.getUserId(), b);
    }

    // Handles "Big Brain" (Get 200 correct questions)
    private void handleBigBrain(Attempt a) {
        Badge b = byName("Big Brain"); if (b == null) return;
        int totalCorrect = attemptDAO.getCorrectAnswersByUser(a.getUserId()); // DAO you already have
        int value = Math.max(0, Math.min(totalCorrect, b.getBadgeGoal()));
        progressDAO.setProgress(a.getUserId(), b.getBadgeId(), value);

        addIfEarned(a.getUserId(), b);
    }

    // ==== helpers ====

    // Test if badge has been earned and add to new badges list
    private void addIfEarned(int userId, Badge b) {
        if (progressDAO.isEarned(userId, b.getBadgeId()) && !oldBadgeIds.contains(b.getBadgeId()) && !alreadyInNew(b)) {
            newBadges.add(b);
        }
    }

    private boolean alreadyInNew(Badge b) {
        for (Badge existing : newBadges) {
            if (existing.getBadgeId() == b.getBadgeId()) return true;
        }
        return false;
    }

    // Award badges if any new was earned
    private void awardIfNew(int userId) {
        if (newBadges == null || newBadges.isEmpty()) return;

        // copy the list for thread run and clear immediately
        final List<Badge> earned = new ArrayList<>(newBadges);
        newBadges.clear();
        oldBadgeIds.clear();

        // Run Thread
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Main.class.getResource("badgeAward.fxml")));
                Node view = loader.load();

                BadgeAwardController ctrl = loader.getController();

                IUserDAO udao = new SqliteUserDAO();
                User u = udao.getUserById(userId);

                // pass data (works with your dynamic subtitle too)
                ctrl.setupNewBadgeDisplay(earned, (SqliteBadgeProgressDAO) progressDAO, u);

                Dialog<Void> dlg = new Dialog<>();
                dlg.setTitle("New Badge");
                dlg.getDialogPane().setContent(view);
                dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                dlg.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to open badges window.").showAndWait();
            }
        });
    }

    private Badge byName(String name) {
        for (Badge b : badgeDAO.getAllBadges()) {
            if (name.equalsIgnoreCase(b.getBadgeName())) return b;
        }
        return null;
    }

    private int percent(Attempt a) {
        String s = a.getScore();
        if (s == null || !s.contains("/")) return 0;
        try {
            String[] t = s.split("/");
            int correct = Integer.parseInt(t[0].trim());
            int total   = Integer.parseInt(t[1].trim());
            if (total <= 0) return 0;
            return (int)Math.round(correct * 100.0 / total);
        } catch (Exception e) {
            return 0;
        }
    }
}