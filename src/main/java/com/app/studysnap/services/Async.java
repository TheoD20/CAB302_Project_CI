package com.app.studysnap.services;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class Async {
    private Async() {}

    // Async to run work off the UI thread and switch back on success
    public static <T> void run(Callable<T> work,
                               Consumer<T> onSuccess,
                               ProgressIndicator progress,
                               TabPane pane) {
        setBusy(progress, pane, true);

        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception {
                try { return work.call(); }
                catch (Throwable t) {
                    if (t instanceof Exception e) throw e;
                    throw new RuntimeException(t);
                }
            }
        };

        task.setOnSucceeded(e -> {
            try {
                if (onSuccess != null) onSuccess.accept(task.getValue()); // may be null; caller handles it
            } catch (Throwable t) {
                t.printStackTrace();
                Popup.error(describe(t));
            } finally {
                setBusy(progress, pane, false);
            }
        });

        task.setOnFailed(e -> {
            try {
                Throwable ex = task.getException();
                if (ex != null) ex.printStackTrace();
                Popup.error(describe(ex));
            } finally {
                setBusy(progress, pane, false);
            }
        });

        task.setOnCancelled(e -> {
            try {
                Popup.info("Operation cancelled.");
            } finally {
                setBusy(progress, pane, false);
            }
        });

        Thread t = new Thread(task, "quiz-bg");
        t.setDaemon(true);
        t.start();
    }

    private static void setBusy(ProgressIndicator p, TabPane pane, boolean busy) {
        if (p != null) p.setVisible(busy);
        if (pane != null) pane.setDisable(busy);
    }

    private static String describe(Throwable ex) {
        if (ex == null) return "Unknown background error (no exception available).";
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        String name = root.getClass().getSimpleName();
        String msg  = root.getMessage();
        if (msg == null || msg.isBlank()) msg = ex.toString();
        return name + ": " + msg;
    }
}