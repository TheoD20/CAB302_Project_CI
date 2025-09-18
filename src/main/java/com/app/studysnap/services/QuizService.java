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

/*
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
    private final PdfTextExtractor extractor = new PdfTextExtractor();

    private static String env(String... names) {
        for (String n : names) {
            String v = System.getenv(n);
            if (v != null && !v.isBlank()) return v;
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
    private static boolean DEBUG() {return true;};
    private static void dbg(String label, String text) {
        if (!DEBUG()) return;
        System.out.println("[QUIZ DEBUG] " + label + ": " +
                (text == null ? "null" : text.substring(0, Math.min(1000, text.length()))));
    }

    // Generate from an uploaded file.
    public String generateFromUpload(File file, boolean includeAnswers) throws Exception {
        String source = extractor.extract(file);
        String input  = truncate(source, 6000);
        String prompt = promptFromSource(input, 10, includeAnswers);
        dbg("Upload.prompt.len", String.valueOf(prompt.length()));
        dbg("Upload.prompt.head", prompt);
        return callGemini(prompt, 1200, 0.7);
    }

    // Generate from pasted text.
    public String generateFromPaste(String text, boolean includeAnswers) throws Exception {
        String input  = truncate(text, 6000);
        String prompt = promptFromSource(input, 10, includeAnswers);
        return callGemini(prompt, 1200, 0.7);
    }

    // Generate from a prompt
    public String generateFromPrompt(String topic, int n, boolean includeAnswers) throws Exception {
        String input  = truncate(topic, 2000);
        String prompt = promptFromTopic(input, clamp(n, 5, 15), includeAnswers);
        dbg("Prompt.prompt.head", prompt);
        return callGemini(prompt, 1200, 0.9);
    }

    // Prompt builders

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
    private String callGemini(String prompt, int maxTokens, double temperature) throws Exception {
        String key = env("GEMINI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Missing GEMINI_API_KEY.");

        String body = """
                {"contents":[{"role":"user","parts":[{"text":"%s"}]}],
                 "generationConfig":{"temperature":%.2f,"maxOutputTokens":%d}}
                """.formatted(jsonEscape(prompt), temperature, maxTokens);

        HttpRequest req = HttpRequest.newBuilder(URI.create(GEMINI_URL + key))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() == 429) {
            Thread.sleep(1200);
            res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        }

        dbg("Gemini.status", String.valueOf(res.statusCode()));
        dbg("Gemini.body", res.body());

        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("Gemini error " + res.statusCode() + ": " + res.body());
        }

        String json = res.body();
        dbg("Gemini.status", String.valueOf(res.statusCode()));
        dbg("Gemini.body", json);

        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("Gemini error " + res.statusCode() + ": " + json);
        }

        // Parse with Gson
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        // If Gemini blocked the prompt, it reports here:
        JsonObject pf = root.has("promptFeedback") ? root.getAsJsonObject("promptFeedback") : null;
        if (pf != null && pf.has("blockReason")) {
            String reason = pf.get("blockReason").getAsString();
            throw new RuntimeException("Gemini safety block: " + reason);
        }

        // candidates[0].content.parts[*].text  (join all parts just in case)
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.size() == 0) {
            throw new RuntimeException("Gemini response has no candidates.");
        }

        JsonObject candidate0 = candidates.get(0).getAsJsonObject();
        JsonObject content = candidate0.getAsJsonObject("content");
        if (content == null) throw new RuntimeException("Gemini response has no content.");

        JsonArray parts = content.getAsJsonArray("parts");
        if (parts == null || parts.size() == 0) throw new RuntimeException("Gemini response has no parts.");

        StringBuilder out = new StringBuilder();
        for (JsonElement el : parts) {
            JsonObject p = el.getAsJsonObject();
            if (p.has("text")) {
                String t = p.get("text").getAsString();
                if (t != null && !t.isBlank()) {
                    if (out.length() > 0) out.append('\n');
                    out.append(t);
                }
            }
        }

        String text = out.toString().trim();
        if (text.isBlank()) {
            // Last resort: give the raw JSON so the UI never shows "null"
            return json;
        }

        dbg("Gemini.text.len", String.valueOf(text.length()));
        dbg("Gemini.text.head", text);
        return text;
    }

    // Helpers
    // Handle null string and reduce big texts to a max characters
    private static String truncate(String s, int maxChars) {
        if (s == null) return "";
        if (s.length() <= maxChars) return s;
        return s.substring(0, maxChars);
    }

    // Clamp int in between min and max boundaries
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

    // Handle string escape characters for Json conversion
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
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int)c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}