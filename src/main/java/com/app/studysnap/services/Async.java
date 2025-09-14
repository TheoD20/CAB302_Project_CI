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

        Task<T> task = new Task<>() { @Override protected T call() throws Exception { return work.call(); } };
        task.setOnSucceeded(e -> {
            try { if (onSuccess != null) onSuccess.accept(task.getValue()); }
            catch (Exception ex) { Popup.error("Error: " + ex.getMessage()); }
            finally { setBusy(progress, pane, false); }
        });
        task.setOnFailed(e -> {
            try {
                Throwable ex = task.getException();
                Popup.error("Error: " + (ex == null ? "Unknown failure" : ex.getMessage()));
            } finally { setBusy(progress, pane, false); }
        });

        Thread t = new Thread(task, "quiz-bg");
        t.setDaemon(true);
        t.start();
    }

    private static void setBusy(ProgressIndicator p, TabPane pane, boolean busy) {
        if (p != null) p.setVisible(busy);
        if (pane != null) pane.setDisable(busy);
    }
}