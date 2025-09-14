package com.app.studysnap.services;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class Popup {
    private Popup() {}

    public static void info(String msg)  { new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait(); }
    public static void warn(String msg)  { new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait(); }
    public static void error(String msg) { new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait(); }

    // Returns true if user clicks OK.
    public static boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(title);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
