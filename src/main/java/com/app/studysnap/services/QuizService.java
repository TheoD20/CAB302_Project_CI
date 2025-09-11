package com.app.studysnap.services;

import java.io.File;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 API-based quiz generator for upload, paste, and prompt.

 Default: Google Gemini 2.0 Flash
 Output format:

 1. Question text
 A) ...
 B) ...
 C) ...
 D) ...
 E) ...
 Answer: A
 */
public final class QuizService {
    private final LocalQuizGenerator local = new LocalQuizGenerator();
    private final PdfTextExtractor extractor = new PdfTextExtractor();

    /**
     Generate from an uploaded file.
     Currently uses LOCAL generator after extracting text.
     TODO(API): call API with extracted text and return its response instead.
     */
    public String generateFromUpload(File file, boolean includeAnswers) throws Exception {
        String src = extractor.extract(file);
        // TODO(API): return apiClient.generateFromText(src, includeAnswers);
        return local.generateFromText(src, includeAnswers);
    }

    /**
     Generate from pasted text.
     TODO(API): call API with the pasted text and return its response.
     */
    public String generateFromPaste(String pasted, boolean includeAnswers) {
        // TODO(API): return apiClient.generateFromText(pasted, includeAnswers);
        return local.generateFromText(pasted, includeAnswers);
    }

    /**
     Generate from a prompt (MCQ assumed).
     TODO(API): call API with prompt and return its response.
     */
    public String generateFromPrompt(String prompt, int n, boolean includeAnswers) {
        // TODO(API): return apiClient.generateFromPrompt(prompt, n, includeAnswers);
        return local.generateFromPrompt(prompt, n, includeAnswers);
    }
}