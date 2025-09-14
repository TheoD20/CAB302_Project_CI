package com.app.studysnap.services;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class FileDialogs {
    private FileDialogs() {}

    public static File chooseOpenDoc(Window owner) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents", List.of("*.pdf", "*.txt")));
        return fc.showOpenDialog(owner);
    }

    public static File chooseSavePdf(Window owner, String baseName) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Quiz as PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File suggested = suggestedDownloadsFile(baseName);
        if (suggested.getParentFile().exists()) fc.setInitialDirectory(suggested.getParentFile());
        fc.setInitialFileName(suggested.getName());
        return fc.showSaveDialog(owner);
    }

    public static File suggestedDownloadsFile(String baseName) {
        File dir = new File(System.getProperty("user.home"), "Downloads");
        if (!dir.exists() || !dir.isDirectory()) dir = new File(System.getProperty("user.home"));
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return new File(dir, baseName + "-" + ts + ".pdf");
    }
}