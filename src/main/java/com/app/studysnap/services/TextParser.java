package com.app.studysnap.services;

import com.app.studysnap.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parses the text shown in previewArea into structured Questions,
public final class TextParser {

    // Question patter to look for
    private static final Pattern BLOCK = Pattern.compile(
            "(?m)\\s*\\d+\\.\\s*(?<content>.+?)\\R" +
                    "\\s*A\\)\\s*(?<a>.+?)\\R" +
                    "\\s*B\\)\\s*(?<b>.+?)\\R" +
                    "\\s*C\\)\\s*(?<c>.+?)\\R" +
                    "\\s*D\\)\\s*(?<d>.+?)" +
                    "(?:\\R\\s*E\\)\\s*(?<e>.+?))?" +
                    "(?:\\R\\s*Answer:\\s*(?<ans>.+?))?" +
                    "\\R+",
            Pattern.DOTALL
    );

    // Parses text into Question objects
    public List<Question> parse(String text) {
        List<Question> list = new ArrayList<>();
        if (text == null || text.isBlank()) return list;

        // Look for pattern matches on input text
        Matcher m = BLOCK.matcher(text + "\n\n");

        // Loops through each match and create new object
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

    // Maps an answer string to option index 1 to 5
    private Integer mapAnswer(String ans, String a, String b, String c, String d, String e) {
        if (ans == null) return null;
        String upper = ans.trim().toUpperCase();

        // Accept numeric input ("1" to "5")
        if (upper.matches("[1-5]")) return Integer.parseInt(upper);

        // Accept letter input ("A" to "E")
        switch (upper) {
            case "A" -> {
                return 1;
            }
            case "B" -> {
                return 2;
            }
            case "C" -> {
                return 3;
            }
            case "D" -> {
                return 4;
            }
            case "E" -> {
                return 5;
            }
        }

        // Try match by option text
        if (equalsIgnoreCaseSafe(ans, a)) return 1;
        if (equalsIgnoreCaseSafe(ans, b)) return 2;
        if (equalsIgnoreCaseSafe(ans, c)) return 3;
        if (equalsIgnoreCaseSafe(ans, d)) return 4;
        if (equalsIgnoreCaseSafe(ans, e)) return 5;

        return null;
    }

    // test for string equality (case-insensitive)
    private static boolean equalsIgnoreCaseSafe(String x, String y) {
        if (x == null || y == null) return false;
        return x.trim().equalsIgnoreCase(y.trim());
    }

    // Trim and handles null string (not null)
    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    // Trim and handles null string (accepts null)
    public static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // Test if a string is null or empty
    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /**
     * Formats seconds as {@code HH:mm:ss}.
     * @param seconds total seconds elapsed
     * @return formatted time string
     */
    public static String formatTime(int seconds){
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}