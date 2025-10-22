package com.app.studysnap.services;

import com.app.studysnap.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provide text parsing and handling helpers
 * <p>
 *     Parses the quiz preview text into structured {@link Question} objects.
 *     Provides trimming, formatting, safeguards, blank testing.
 * </p>
 */
public final class TextParser {

    /**
     * Question block pattern:
     * <ul>
     *   <li>Numbered question line capturing {@code content}</li>
     *   <li>Mandatory options A–D, optional E</li>
     *   <li>Optional {@code Answer: X} (case-insensitive)</li>
     * </ul>
     */
    private static final Pattern BLOCK = Pattern.compile(
            // (?mi) => multiline + case-insensitive for "Answer:"
            "(?mi)\\s*\\d+\\.\\s*(?<content>.+?)\\R" +
                    "\\s*A\\)\\s*(?<a>.+?)\\R" +
                    "\\s*B\\)\\s*(?<b>.+?)\\R" +
                    "\\s*C\\)\\s*(?<c>.+?)\\R" +
                    "\\s*D\\)\\s*(?<d>.+?)" +
                    "(?:\\R\\s*E\\)\\s*(?<e>.+?))?" +
                    "(?:\\R\\s*Answer:\\s*(?<ans>.+?))?" +
                    "\\R+",
            Pattern.DOTALL
    );

    /**
     * Parses text into a list of {@link Question}s.
     * @param text source in the expected MCQ format
     * @return a list of parsed questions
     */
    public List<Question> parse(String text) {
        List<Question> list = new ArrayList<>();
        if (text == null || text.isBlank()) return list;

        // Look for pattern matches on input text
        Matcher m = BLOCK.matcher(text + "\n\n");

        while (m.find()) {
            String content = trim(m.group("content"));
            String a = trim(m.group("a"));
            String b = trim(m.group("b"));
            String c = trim(m.group("c"));
            String d = trim(m.group("d"));
            String e = trimOrNull(m.group("e"));
            String ans = trimOrNull(m.group("ans"));

            Integer correct = mapAnswer(ans, a, b, c, d, e);

            Question q = new Question();
            q.setQuestion(content);
            q.setOption1(a);
            q.setOption2(b);
            q.setOption3(c);
            q.setOption4(d);
            q.setOption5(e);
            q.setCorrectOption(correct);
            list.add(q);
        }
        return list;
    }

    /**
     * Maps answer string to the 1–5 index of the correct option.
     * Accepts:
     * <ul>
     *   <li>Letters: {@code A..E}, with or without trailing {@code )} or leading "Option"</li>
     *   <li>Digits: {@code 1..5}</li>
     *   <li>Full option text (case-insensitive) as a fallback</li>
     * </ul>
     *
     * @return 1 - 5 for A - E, or {@code null} if it can’t be determined
     */
    private Integer mapAnswer(String ans, String a, String b, String c, String d, String e) {
        if (ans == null) return null;

        // Normalize
        String upper = ans.trim()
                .replaceFirst("(?i)^option\\s+", "")
                .replaceFirst("\\)$", "")
                .trim()
                .toUpperCase();

        // Accept numeric input ("1" to "5")
        if (upper.matches("[1-5]")) return Integer.parseInt(upper);

        // Accept letter input ("A" to "E")
        switch (upper) {
            case "A" -> { return 1; }
            case "B" -> { return 2; }
            case "C" -> { return 3; }
            case "D" -> { return 4; }
            case "E" -> { return 5; }
        }

        // Try match by option text
        if (equalsIgnoreCaseSafe(ans, a)) return 1;
        if (equalsIgnoreCaseSafe(ans, b)) return 2;
        if (equalsIgnoreCaseSafe(ans, c)) return 3;
        if (equalsIgnoreCaseSafe(ans, d)) return 4;
        if (equalsIgnoreCaseSafe(ans, e)) return 5;

        return null;
    }

    // Utilities

    /** Case-insensitive string equality with trimming; returns false if either is null. */
    private static boolean equalsIgnoreCaseSafe(String x, String y) {
        if (x == null || y == null) return false;
        return x.trim().equalsIgnoreCase(y.trim());
    }

    /** Null-safe trim (never returns null). */
    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /** Null-safe trim that returns null if the trimmed result is empty. */
    public static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** True if null or only whitespace. */
    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /** Formats seconds as {@code HH:mm:ss}. */
    public static String formatTime(int seconds){
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}