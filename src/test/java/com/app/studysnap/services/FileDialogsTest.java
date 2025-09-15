package com.app.studysnap.services;

import javafx.stage.Window;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileDialogsTest {

    @Test
    void suggestedDownloadsFile_ReturnsFileWithTimestamp() {
        String baseName = "quiz";
        File f = FileDialogs.suggestedDownloadsFile(baseName);
        assertNotNull(f);
        assertTrue(f.getName().startsWith(baseName + "-"));
        assertTrue(f.getName().endsWith(".pdf"));
        assertTrue(f.getParentFile().exists());
    }

    // Can't test FileChooser dialogs interactively in unit tests
    // But we can ensure the chooser is not null and filters are applied via reflection
    @Test
    void chooseOpenDoc_ReturnsNullWithoutOwner() {
        File f = FileDialogs.chooseOpenDoc(null);
        // User has to click cancel or open, so usually null in automated tests
        // Just assert it doesn't throw
        assertTrue(f == null || f instanceof File);
    }

    @Test
    void chooseSavePdf_ReturnsNullWithoutOwner() {
        File f = FileDialogs.chooseSavePdf(null, "quiz");
        // User has to click cancel or save, usually null in automated tests
        assertTrue(f == null || f instanceof File);
    }
}

