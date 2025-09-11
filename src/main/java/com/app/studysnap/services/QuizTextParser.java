package com.app.studysnap.services;

import com.app.studysnap.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the text shown in previewArea into structured Questions,
 */
public final class QuizTextParser {

    private static final Pattern BLOCK = Pattern.compile(
            "(?m)\\s*\\d+\\.\\s*(?<stem>.+?)\\R" +                           // "1. ..." line
                    "\\s*A\\)\\s*(?<a>.+?)\\R" +                                     // A)
                    "\\s*B\\)\\s*(?<b>.+?)\\R" +                                     // B)
                    "\\s*C\\)\\s*(?<c>.+?)\\R" +                                     // C)
                    "\\s*D\\)\\s*(?<d>.+?)" +                                        // D)
                    "(?:\\R\\s*E\\)\\s*(?<e>.+?))?" +                                // optional E)
                    "(?:\\R\\s*Answer:\\s*(?<ans>.+?))?" +                           // optional Answer:
                    "\\R{1,}",                                                       // blank line(s) end of block
            Pattern.DOTALL
    );

    public List<Question> parse(String text) {
        List<Question> list = new ArrayList<>();
        if (text == null || text.isBlank()) return list;

        Matcher m = BLOCK.matcher(text + "\n\n"); // pad to ease last match
        while (m.find()) {
            String stem = trim(m.group("stem"));
            String a = trim(m.group("a"));
            String b = trim(m.group("b"));
            String c = trim(m.group("c"));
            String d = trim(m.group("d"));
            String e = trimOrNull(m.group("e"));
            String ans = trimOrNull(m.group("ans"));

            Integer correct = mapAnswer(ans, a, b, c, d, e);

            Question q = new Question();
            // Your schema uses "content" for the stem/question text
            q.setQuestion(stem);
            q.setOption1(a);
            q.setOption2(b);
            q.setOption3(c);
            q.setOption4(d);
            q.setOption5(e);                 // may be null
            q.setCorrectOption(correct);     // 1–5 or null
            list.add(q);
        }
        return list;
    }

    private Integer mapAnswer(String ans, String a, String b, String c, String d, String e) {
        if (ans == null) return null;
        String upper = ans.trim().toUpperCase();

        // Accept numeric input ("1".."5")
        if (upper.matches("[1-5]")) return Integer.parseInt(upper);

        // Accept letter input ("A".."E")
        if (upper.equals("A")) return 1;
        if (upper.equals("B")) return 2;
        if (upper.equals("C")) return 3;
        if (upper.equals("D")) return 4;
        if (upper.equals("E")) return 5;

        // Try match by option text
        if (equalsIgnoreCaseSafe(ans, a)) return 1;
        if (equalsIgnoreCaseSafe(ans, b)) return 2;
        if (equalsIgnoreCaseSafe(ans, c)) return 3;
        if (equalsIgnoreCaseSafe(ans, d)) return 4;
        if (equalsIgnoreCaseSafe(ans, e)) return 5;

        return null; // unknown
    }

    private static boolean equalsIgnoreCaseSafe(String x, String y) {
        if (x == null || y == null) return false;
        return x.trim().equalsIgnoreCase(y.trim());
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}