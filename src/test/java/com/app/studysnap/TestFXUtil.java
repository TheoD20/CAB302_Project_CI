package com.app.studysnap;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Window;

import java.util.concurrent.CountDownLatch;

public class TestFXUtil {

    private static boolean fxStarted = false;

    public static void initFX() {
        if (!fxStarted) {
            fxStarted = true;
            try {
                CountDownLatch latch = new CountDownLatch(1);
                Platform.startup(latch::countDown);
                try {
                    latch.await(); // ✅ fixed: handled properly
                } catch (InterruptedException ignored) {}
            } catch (IllegalStateException ignored) {}
        }
    }


    /** Suppresses all JavaFX popups globally (Alerts, etc.) */
    public static void disableAlerts() {
        System.setProperty("javafx.headless", "true");
        try {
            // Override JavaFX Alert showAndWait()
            javafx.scene.control.Dialog<Void> dummy = new javafx.scene.control.Dialog<>();
            dummy.initOwner(new Window() {});

            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setOnShown(event -> {
                System.out.println("[Alert suppressed during test]");
                alert.close();
            });

            // Optional: also override Popup service if available
            try {
                Class<?> popupClass = Class.forName("com.app.studysnap.services.Popup");
                var setHandler = popupClass.getDeclaredMethod("setHandler", java.util.function.BiConsumer.class);
                setHandler.setAccessible(true);
                setHandler.invoke(null, (java.util.function.BiConsumer<String, String>)
                        (type, msg) -> System.out.println("[Popup suppressed] " + msg));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            System.out.println("⚠ Failed to suppress alerts: " + e.getMessage());
        }
    }
}