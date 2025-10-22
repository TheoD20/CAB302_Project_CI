package com.app.studysnap.services;

import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;

import java.util.List;

import static com.app.studysnap.services.TextParser.*;

/**
 * Renders {@link Quiz} instances to a human-readable plain-text format suitable for previews and exports.
 * <p>
 * Output uses a numbered list for questions (1., 2., …) and up to five options labelled A)–E).
 * When requested, the correct option letter is appended as an {@code Answer: X} line.
 * </p>
 */
public final class QuizRenderer {

    /**
     * Builds a plain-text representation of the given quiz.
     * @param quiz the quiz to render.
     * @param includeAnswers whether to include answers in output
     * @return a string containing the quiz title, subject (if present) and all questions/options
     */
    public String renderAsText(Quiz quiz, boolean includeAnswers) {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(quiz.getTitle())) {
            sb.append(trim(quiz.getTitle()));
            if (!isBlank(quiz.getSubject())) {
                sb.append(" — ").append(trim(quiz.getSubject()));
            }
            sb.append("\n\n");
        }

        List<Question> qs = quiz.getQuestions();
        if (qs == null || qs.isEmpty()) return sb.toString();

        int i = 1;
        for (Question q : qs) {
            sb.append(i++).append(". ").append(trim(q.getQuestion())).append("\n");

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
}