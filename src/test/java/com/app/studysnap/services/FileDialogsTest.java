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
}

