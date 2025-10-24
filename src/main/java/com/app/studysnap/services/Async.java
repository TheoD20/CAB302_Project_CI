package com.app.studysnap.services;

import com.app.studysnap.exceptions.AppException;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static com.app.studysnap.services.TextParser.*;

/**
 * Utility to run background work off the JavaFX Application Thread and return
 * back to the UI thread with simple progress/disable handling.
 * <p>
 * On success, {@code onSuccess} is invoked on the FX thread with the task result.
 * On failure or cancellation, a user-visible {@link Popup} message is shown.
 * </p>
 */
public final class Async {

    /**
     * Not instantiable.
     */
    private Async() {
        throw new AssertionError("No instances");
    }

    /**
     * Executes the given unit of work on a separate thread, toggling a progress indicator
     * and disabling a container while it runs.
     *
     * @param work background computation
     * @param onSuccess callback invoked on the FX thread with the result (can be {@code null})
     * @param progress progress indicator to show/hide (can be {@code null})
     * @param pane container to disable/enable during execution (can be {@code null})
     * @param <T> result type
     */
    public static <T> void run(Callable<T> work,
                               Consumer<T> onSuccess,
                               ProgressIndicator progress,
                               TabPane pane) {
        setBusy(progress, pane, true);

        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception {
                try {
                    return work.call();
                } catch (Throwable t) {
                    if (t instanceof Exception e) throw e;
                    throw new AppException("Background task failed.", t);
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

    /**
     * Shows/hides the progress indicator and disables/enables the provided pane.
     * @param p progress indicator (nullable)
     * @param pane container to disable/enable (nullable)
     * @param busy {@code true} to show progress and disable; {@code false} to restore
     */
    private static void setBusy(ProgressIndicator p, TabPane pane, boolean busy) {
        if (p != null) p.setVisible(busy);
        if (pane != null) pane.setDisable(busy);
    }

    /**
     * Builds a concise error description from a throwable, including the root cause.
     * @param ex exception (nullable)
     * @return user-friendly description string
     */
    private static String describe(Throwable ex) {
        if (ex == null) return "Unknown background error (no exception available).";
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        String name = root.getClass().getSimpleName();
        String msg  = root.getMessage();
        if (isBlank(msg)) msg = ex.toString();
        return name + ": " + msg;
    }
}