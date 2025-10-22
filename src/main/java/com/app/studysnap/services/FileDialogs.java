package com.app.studysnap.services;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.app.studysnap.services.TextParser.*;

/**
 * Helpers for standard file chooser dialogs.
 */
public final class FileDialogs {
    private FileDialogs() {}

    /**
     * Opens a native file chooser to pick a document suitable for quiz generation.
     * Accepts PDF and TXT files.
     * @param owner the owning window for modality, can be {@code null}
     * @return the selected file, or {@code null} if the user cancelled
     */
    public static File chooseOpenDoc(Window owner) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents", List.of("*.pdf", "*.txt")));
        return fc.showOpenDialog(owner);
    }

    /**
     * Opens a "Save As" dialog to export a quiz as a PDF. The initial filename and directory
     * are suggested using {@link #suggestedDownloadsFile(String)}.
     * @param owner the owning window for modality, can be {@code null}
     * @param baseName the base filename (without extension).
     * @return the destination file chosen by the user, or {@code null} if cancelled
     */
    public static File chooseSavePdf(Window owner, String baseName) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Quiz as PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File suggested = suggestedDownloadsFile(baseName);
        if (suggested.getParentFile().exists()) fc.setInitialDirectory(suggested.getParentFile());
        fc.setInitialFileName(suggested.getName());
        return fc.showSaveDialog(owner);
    }

    /**
     * Builds a suggested PDF file path under the user's {@code Downloads} folder (or home directory
     * if that folder is missing), using the provided base name and a timestamp to avoid collisions.
     * @param baseName the base name to use for the file (e.g., {@code "quiz"}); if blank, defaults to {@code "quiz"}
     * @return a {@link File} pointing to the suggested destination
     */
    public static File suggestedDownloadsFile(String baseName) {
        File dir = new File(System.getProperty("user.home"), "Downloads");
        if (!dir.exists() || !dir.isDirectory()) dir = new File(System.getProperty("user.home"));

        String base = isBlank(baseName) ? "quiz" : trim(baseName);

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return new File(dir, base + "-" + ts + ".pdf");
    }
}