package com.app.studysnap.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.app.studysnap.services.TextParser.*;

import com.app.studysnap.exceptions.ExternalServiceException;
import com.app.studysnap.exceptions.ValidationException;
import com.app.studysnap.exceptions.AppException;

/**
 * API quiz generator that produces five-option MCQs (A–E) with optional answer lines.
 * <p>
 * Sources supported:
 * <ul>
 *   <li>Uploaded file (PDF/TXT) – parsed to text then summarized into MCQs.</li>
 *   <li>Raw pasted text – summarized into MCQs.</li>
 *   <li>Freeform topic prompt – generated MCQs around a topic.</li>
 * </ul>
 * Default provider: Google Gemini ({@code gemini-2.0-flash}).
 * The expected output format per question is:
 * <pre>
 * 1. Question text
 * A) ...
 * B) ...
 * C) ...
 * D) ...
 * E) ...
 * Answer: A
 * </pre>
 */
public final class QuizService {
    private final PdfTextExtractor extractor = new PdfTextExtractor();

    /**
     * Resolve the first non-blank value from a list of environment variable names.
     */
    private static String env(String... names) {
        for (String n : names) {
            String v = System.getenv(n);
            if (!isBlank(v)) return v;
        }
        return null;
    }

    // Gemini model used
    private static final String GEMINI_MODEL = "gemini-2.0-flash";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
            + GEMINI_MODEL + ":generateContent?key=";

    // Http connection
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    // Debugging
    private static boolean DEBUG() { return false; }
    private static void dbg(String label, String text) {
        if (!DEBUG()) return;
        System.out.println("[QUIZ DEBUG] " + label + ": " +
                (text == null ? "null" : text.substring(0, Math.min(1000, text.length()))));
    }

    /**
     * Generates MCQs from an uploaded document.
     * @param file PDF or TXT file
     * @param includeAnswers if {@code true}, append {@code Answer: X} lines
     * @return rendered questions in the target plain-text format
     */
    public String generateFromUpload(File file, boolean includeAnswers) {
        if (file == null) {
            throw new ValidationException("No file provided.");
        }
        try {
            String source = extractor.extract(file);
            String input  = truncate(source, 6000);
            String prompt = promptFromSource(input, 10, includeAnswers);
            dbg("Upload.prompt.len", String.valueOf(prompt.length()));
            dbg("Upload.prompt.head", prompt);
            return callGemini(prompt, 1200, 0.7);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw new ValidationException(e.getMessage(), e);
            }

            // Unexpected error
            throw new ExternalServiceException("Failed to generate questions from upload.", e);
        }
    }

    /**
     * Generates MCQs from pasted raw text.
     * @param text source content
     * @param includeAnswers whether to add {@code Answer: } lines
     * @return rendered questions
     */
    public String generateFromPaste(String text, boolean includeAnswers) {
        if (isBlank(text)) {
            throw new ValidationException("Nothing to generate. Paste some content first.");
        }
        try {
            String input  = truncate(text, 6000);
            String prompt = promptFromSource(input, 10, includeAnswers);
            return callGemini(prompt, 1200, 0.7);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw new ValidationException(e.getMessage(), e);
            }

            throw new ExternalServiceException("Failed to generate questions from pasted text.", e);
        }
    }

    /**
     * Generates MCQs from a topic prompt.
     * @param topic topic/keywords
     * @param n desired number of questions (clamped to 5–15)
     * @param includeAnswers include answers flag
     * @return rendered questions
     */
    public String generateFromPrompt(String topic, int n, boolean includeAnswers) {
        if (isBlank(topic)) {
            throw new ValidationException("Empty prompt. Write a short topic or question seed.");
        }
        try {
            String input  = truncate(topic, 2000);
            String prompt = promptFromTopic(input, clamp(n, 5, 15), includeAnswers);
            dbg("Prompt.prompt.head", prompt);
            return callGemini(prompt, 1200, 0.9);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw new ValidationException(e.getMessage(), e);
            }

            throw new ExternalServiceException("Failed to generate questions from prompt.", e);
        }
    }

    // Prompt builders

    /**
     * Build a prompt from a source text.
     * @param source text to generate quiz on.
     * @param n number of questions to be generated.
     * @param includeAnswers to include or not answers on the response.
     * @return prompt as a string.
     */
    private String promptFromSource(String source, int n, boolean includeAnswers) {
        return """
               Generate exactly %d multiple-choice questions from the SOURCE text.
               
               FORMAT (match this exactly):
               <question number>. <question text>
               A) <option A>
               B) <option B>
               C) <option C>
               D) <option D>
               E) <option E>
               Answer: <correct option>

               Formatting rules:
               - Each question MUST have exactly five options labeled A), B), C), D), E). Each in an individual line.
               - %s
               - No extra commentary, no markdown, no JSON. Just the questions list.
               - Questions must be self-contained and unambiguous.

               SOURCE:
               %s
               """.formatted(
                n,
                includeAnswers ? "Include the line: \"Answer: X\" for each question, where X is A, B, C, or D, or E."
                        : "Do NOT include any Answer lines.",
                source
        );
    }

    /**
     * Build a prompt from a topic.
     * @param topic topic to generate quiz on.
     * @param n number of questions to be generated.
     * @param includeAnswers to include or not answers on the response.
     * @return prompt as a string.
     */
    private String promptFromTopic(String topic, int n, boolean includeAnswers) {
        return """
               Create exactly %d multiple-choice questions about the topic below.

               Topic: %s
               
               FORMAT (match this exactly):
               <question number>. <question text>
               A) <option A>
               B) <option B>
               C) <option C>
               D) <option D>
               E) <option E>
               Answer: <correct option>

               Formatting rules:
               - Each question MUST have exactly five options labeled A), B), C), D), E). Each in an individual line.
               - %s
               - No extra commentary, no markdown, no JSON. Just the questions list.
               """.formatted(
                n,
                topic,
                includeAnswers ? "Include the line: \"Answer: X\" for each question." : "Do NOT include any Answer lines."
        );
    }

    // Gemini REST

    /**
     * Call API with formatted prompt and receive response
     * @param prompt prompt to run through API
     * @param maxTokens maximum number of tokens allowed per request
     * @param temperature sampling temperature in the range {@code [0.0, 1.0]} (higher = more randomness)
     * @return the plain-text content returned by the provider, or the raw JSON body if no text parts are present
     * @throws AppException if the client is misconfigured (e.g., missing {@code GEMINI_API_KEY})
     * @throws ExternalServiceException if the service cannot be reached, triggers a safety block, or the expected fields are absent.
     */
    private String callGemini(String prompt, int maxTokens, double temperature)
            throws AppException {
        String key = env("GEMINI_API_KEY");
        if (isBlank(key)) {
            throw new AppException("Missing GEMINI_API_KEY.");
        }

        String body = """
                {"contents":[{"role":"user","parts":[{"text":"%s"}]}],
                 "generationConfig":{"temperature":%.2f,"maxOutputTokens":%d}}
                """.formatted(jsonEscape(prompt), temperature, maxTokens);

        HttpRequest req = HttpRequest.newBuilder(URI.create(GEMINI_URL + key))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res;
        try {
            res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() == 429) { // simple retry-on-rate-limit
                Thread.sleep(1200);
                res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            }
        } catch (Exception io) {
            throw new ExternalServiceException("Failed to reach Gemini service.", io);
        }

        dbg("Gemini.status", String.valueOf(res.statusCode()));
        dbg("Gemini.body", res.body());

        if (res.statusCode() / 100 != 2) {
            throw new ExternalServiceException("Gemini error " + res.statusCode() + ": " + res.body());
        }

        // Parse with Gson
        String json = res.body();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        // Check safety block
        JsonObject pf = root.has("promptFeedback") ? root.getAsJsonObject("promptFeedback") : null;
        if (pf != null && pf.has("blockReason")) {
            String reason = pf.get("blockReason").getAsString();
            throw new ExternalServiceException("Gemini safety block: " + reason);
        }

        // candidates[0].content.parts[*].text  (join all parts just in case)
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new ExternalServiceException("Gemini response has no candidates.");
        }

        JsonObject candidate0 = candidates.get(0).getAsJsonObject();
        JsonObject content = candidate0.getAsJsonObject("content");
        if (content == null) throw new ExternalServiceException("Gemini response has no content.");

        JsonArray parts = content.getAsJsonArray("parts");
        if (parts == null || parts.isEmpty()) throw new ExternalServiceException("Gemini response has no parts.");

        StringBuilder out = new StringBuilder();
        for (JsonElement el : parts) {
            JsonObject p = el.getAsJsonObject();
            if (p.has("text")) {
                String t = p.get("text").getAsString();
                if (!isBlank(t)) {
                    if (!out.isEmpty()) out.append('\n');
                    out.append(t);
                }
            }
        }

        String text = trim(out.toString());
        if (isBlank(text)) {
            // last resort: give raw JSON so UI never shows "null"
            return json;
        }

        dbg("Gemini.text.len", String.valueOf(text.length()));
        dbg("Gemini.text.head", text);
        return text;
    }

    // Helpers

    /**
     * Handle null input and reduce big texts to a maximum number of characters.
     */
    private static String truncate(String s, int maxChars) {
        if (s == null) return "";
        if (s.length() <= maxChars) return s;
        return s.substring(0, maxChars);
    }

    /**
     * Clamp an int to {@code [min, max]}.
     */
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

    /**
     * Minimal JSON-string escaper for the request body.
     */
    private static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 32);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}