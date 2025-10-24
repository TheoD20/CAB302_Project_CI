package com.app.studysnap.services;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Helper class for showing simple JavaFX modal dialogs.
 * <p>
 * Provides convenience methods for information, warning, error, and confirmation popups.
 * All dialogs are shown synchronously using {@link Alert#showAndWait()}.
 * </p>
 */
public final class Popup {

    /**
     * Not instantiable.
     */
    private Popup() {
        throw new AssertionError("No instances");
    }

    /**
     * Shows an information dialog with an OK button.
     * @param msg the message to display
     */
    public static void info(String msg)  { new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait(); }

    /**
     * Shows a warning dialog with an OK button.
     * @param msg the warning message to display
     */
    public static void warn(String msg)  { new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait(); }

    /**
     * Shows an error dialog with an OK button.
     * @param msg the error message to display
     */
    public static void error(String msg) { new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait(); }

    /**
     * Shows a confirmation dialog with OK and Cancel buttons.
     * @param title the dialog window title
     * @param msg the confirmation prompt message
     * @return {@code true} if the user selects OK; {@code false} otherwise
     */
    public static boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(title);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}