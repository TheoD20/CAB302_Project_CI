package com.app.studysnap.services;

import java.io.File;

/**
 * Single facade the controller should call for generation.
 * Swap LOCAL -> API by editing only this class.
 */
public final class QuizService {
    private final LocalQuizGenerator local = new LocalQuizGenerator();
    private final PdfTextExtractor extractor = new PdfTextExtractor();

    /**
     * Generate from an uploaded file.
     * Currently uses LOCAL generator after extracting text.
     * TODO(API): call API with extracted text and return its response instead.
     */
    public String generateFromUpload(File file, boolean includeAnswers) throws Exception {
        String src = extractor.extract(file);
        // TODO(API): return apiClient.generateFromText(src, includeAnswers);
        return local.generateFromText(src, includeAnswers);
    }

    /**
     * Generate from pasted text.
     * TODO(API): call API with the pasted text and return its response.
     */
    public String generateFromPaste(String pasted, boolean includeAnswers) {
        // TODO(API): return apiClient.generateFromText(pasted, includeAnswers);
        return local.generateFromText(pasted, includeAnswers);
    }

    /**
     * Generate from a prompt (MCQ assumed).
     * TODO(API): call API with prompt and return its response.
     */
    public String generateFromPrompt(String prompt, int n, boolean includeAnswers) {
        // TODO(API): return apiClient.generateFromPrompt(prompt, n, includeAnswers);
        return local.generateFromPrompt(prompt, n, includeAnswers);
    }
}