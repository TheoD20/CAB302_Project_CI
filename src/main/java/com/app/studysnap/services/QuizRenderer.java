package com.app.studysnap.services;

import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;

import java.util.List;

/**
 Renders a Quiz back to preview text with up to 5 options (A–E).
 */
public final class QuizRenderer {

    public String renderAsText(Quiz quiz, boolean includeAnswers) {
        StringBuilder sb = new StringBuilder();
        if (quiz.getTitle() != null && !quiz.getTitle().isBlank()) {
            sb.append(quiz.getTitle());
            if (quiz.getSubject() != null && !quiz.getSubject().isBlank()) {
                sb.append(" — ").append(quiz.getSubject());
            }
            sb.append("\n\n");
        }

        List<Question> qs = quiz.getQuestions();
        if (qs == null || qs.isEmpty()) return sb.toString();

        int i = 1;
        for (Question q : qs) {
            sb.append(i++).append(". ").append(nz(q.getQuestion())).append("\n");

            if (q.getOption1()!=null) sb.append("   A) ").append(q.getOption1()).append("\n");
            if (q.getOption2()!=null) sb.append("   B) ").append(q.getOption2()).append("\n");
            if (q.getOption3()!=null) sb.append("   C) ").append(q.getOption3()).append("\n");
            if (q.getOption4()!=null) sb.append("   D) ").append(q.getOption4()).append("\n");
            if (q.getOption5()!=null) sb.append("   E) ").append(q.getOption5()).append("\n");

            if (includeAnswers && q.getCorrectOption() != null) {
                int idx = q.getCorrectOption();
                String letter = switch (idx) {
                    case 1 -> "A";
                    case 2 -> "B";
                    case 3 -> "C";
                    case 4 -> "D";
                    case 5 -> "E";
                    default -> "?";
                };
                sb.append("   Answer: ").append(letter).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String nz(String s) { return s == null ? "" : s; }
}