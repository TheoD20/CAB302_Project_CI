package com.app.studysnap.controllers;

import com.app.studysnap.auth.Session;
import com.app.studysnap.model.IUserDAO;
import com.app.studysnap.model.SqliteUserDAO;
import com.app.studysnap.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProfileController {

    @FXML private Label displayName;
    @FXML private Label providerLabel;
    @FXML private Label providerValue;
    @FXML private Label statusLabel;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;

    @FXML private Button saveButton;

    private IUserDAO userDAO;
    private User currentUser;

    @FXML
    private void initialize() {
        try { userDAO = new SqliteUserDAO(); } catch (Throwable t) { userDAO = null; }

        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            setStatus("No session found. Please log in again.");
            disableForm(true);
            return;
        }

        // Prefill
        usernameField.setText(safe(currentUser.getUsername()));
        emailField.setText(safe(currentUser.getEmail()));

        String provider = safe(currentUser.getAuthProvider()).isBlank() ? "LOCAL" : currentUser.getAuthProvider();
        providerValue.setText(provider);
        providerLabel.setText(provider.equalsIgnoreCase("GOOGLE") ? "Google account" : "Local account");
        displayName.setText(safe(currentUser.getUsername()).isBlank() ? "User" : currentUser.getUsername());

        // Para GOOGLE, email sólo lectura
        emailField.setEditable(!provider.equalsIgnoreCase("GOOGLE"));

        // Habilitar guardar sólo si hay cambios válidos
        saveButton.setDisable(true);
        usernameField.textProperty().addListener((obs, a, b) -> validateDirty());
        emailField.textProperty().addListener((obs, a, b) -> validateDirty());
    }

    private void validateDirty() {
        if (currentUser == null) { saveButton.setDisable(true); return; }

        String newUsername = safe(usernameField.getText());
        String newEmail    = safe(emailField.getText());

        boolean changed = !newUsername.equals(safe(currentUser.getUsername()))
                || !newEmail.equals(safe(currentUser.getEmail()));

        boolean valid = validateUsername(newUsername)
                && (emailField.isEditable() ? validateEmail(newEmail) : true);

        saveButton.setDisable(!(changed && valid));
        statusLabel.setText(changed && !valid ? "Fix validation errors to continue." : "");
    }

    @FXML
    private void handleSave() {
        if (currentUser == null || userDAO == null) {
            setStatus("Internal error. Try again.");
            return;
        }

        String newUsername = safe(usernameField.getText());
        String newEmail    = safe(emailField.getText());

        // Validaciones
        if (!validateUsername(newUsername)) { setStatus("Username must be 3–24 characters."); return; }
        if (emailField.isEditable() && !validateEmail(newEmail)) { setStatus("Enter a valid email."); return; }

        // Si es GOOGLE, no permitir cambiar el email (ya está deshabilitado, de todas formas normalizamos)
        if (!emailField.isEditable()) {
            newEmail = safe(currentUser.getEmail());
        }

        // Actualizar modelo
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);

        // Persistir — ¡ojo! updateUser() es void
        boolean ok = true;
        try {
            userDAO.updateUser(currentUser);   // <= sin asignación
        } catch (Exception ex) {
            ex.printStackTrace();
            ok = false;
        }

        if (ok) {
            Session.setCurrentUser(currentUser);                    // refresca sesión
            displayName.setText(currentUser.getUsername());         // refresca encabezado
            setStatus("Profile saved ✓");
            saveButton.setDisable(true);
        } else {
            setStatus("Could not save changes.");
        }
    }

    /* ------------ helpers ------------ */
    private void setStatus(String msg) { statusLabel.setText(msg == null ? "" : msg); }
    private void disableForm(boolean b) {
        usernameField.setDisable(b);
        emailField.setDisable(b);
        saveButton.setDisable(b);
    }
    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static boolean validateUsername(String s) { return s != null && s.trim().length() >= 3 && s.trim().length() <= 24; }
    private static boolean validateEmail(String s) {
        if (s == null) return false;
        String v = s.trim().toLowerCase();
        return v.contains("@") && v.indexOf('@') > 0 && v.indexOf('@') < v.length() - 3 && v.contains(".");
    }
}
