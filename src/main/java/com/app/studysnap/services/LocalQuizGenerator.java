package com.app.studysnap.services;

import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 This is a temporary AI generated quiz generator. just for test purposes.
 TODO: Replace with API later
 */
public final class LocalQuizGenerator {
    private final SecureRandom rnd = new SecureRandom();

    public String generateFromText(String text, boolean includeAnswers) {
        text = normalize(text);

        List<String> sentences = splitSentences(text);
        // Build a reasonably large pool for distractors
        List<String> vocab = topKeywords(text, 200);
        Set<String> allWords = allCandidateWords(text);

        StringBuilder out = new StringBuilder();
        int qn = 1;

        for (String s : sentences) {
            Optional<String> keyOpt = pickKeywordInSentence(s, vocab);
            if (keyOpt.isEmpty()) continue;

            String answer = keyOpt.get(); // correct term as text
            String stem = s.replaceAll("(?i)\\b" + Pattern.quote(answer) + "\\b", "_____");

            List<String> options = makeFiveOptions(answer, vocab, allWords);
            if (options.size() < 5) continue; // skip if we couldn't form 5 unique options

            // Shuffle for labeling
            Collections.shuffle(options, rnd);
            int correctIdx = indexOfIgnoreCase(options, answer);
            if (correctIdx < 0) continue; // sanity

            out.append(qn++).append(". ").append(stem.trim()).append("\n");
            char label = 'A';
            for (int i = 0; i < 5; i++) {
                out.append("   ").append((char)(label + i)).append(") ").append(options.get(i)).append("\n");
            }
            if (includeAnswers) {
                String correctLetter = String.valueOf((char)('A' + correctIdx));
                out.append("   Answer: ").append(correctLetter).append("\n");
            }
            out.append("\n");

            if (qn > 15) break; // keep it short by default
        }

        if (qn == 1) return "Not enough content to generate questions.";
        return out.toString();
    }

    public String generateFromPrompt(String prompt, int n, boolean includeAnswers) {
        // Use prompt doubled to bias towards its own words
        String text = prompt + "\n" + prompt;
        // Heuristic cap to n questions (generateFromText already caps to 15)
        String generated = generateFromText(text, includeAnswers);
        return generated;
    }

    // ---------- helpers ----------

    private List<String> splitSentences(String text) {
        return Arrays.stream(text.split("(?<=[.!?])\\s+"))
                .filter(s -> s.length() > 40 && s.split("\\s+").length >= 8)
                .limit(120)
                .toList();
    }

    private static final Set<String> STOP = Set.of(
            "the","and","a","an","of","to","in","is","are","was","were","be","been",
            "on","for","as","with","by","or","that","this","it","from","at","which",
            "but","we","you","i","they","their","our","your","his","her","its","them"
    );

    private List<String> topKeywords(String text, int k) {
        Map<String,Integer> freq = new HashMap<>();
        for (String w : text.split("\\W+")) {
            String t = w.toLowerCase(Locale.ROOT);
            if (t.length() < 4 || STOP.contains(t)) continue;
            freq.merge(t, 1, Integer::sum);
        }
        return freq.entrySet().stream()
                .sorted((a,b)->Integer.compare(b.getValue(), a.getValue()))
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Set<String> allCandidateWords(String text) {
        Set<String> out = new HashSet<>();
        for (String w : text.split("\\W+")) {
            String t = w.toLowerCase(Locale.ROOT);
            if (t.length() < 4 || STOP.contains(t)) continue;
            out.add(cap(t));
        }
        return out;
    }

    private Optional<String> pickKeywordInSentence(String s, List<String> vocab) {
        List<String> hits = new ArrayList<>();
        String low = s.toLowerCase(Locale.ROOT);
        for (String k : vocab) {
            if (low.matches(".*\\b" + Pattern.quote(k.toLowerCase(Locale.ROOT)) + "\\b.*")) {
                hits.add(k);
            }
        }
        if (hits.isEmpty()) return Optional.empty();
        return Optional.of(cap(hits.get(rnd.nextInt(hits.size()))));
    }

    private List<String> makeFiveOptions(String answer, List<String> primaryPool, Set<String> secondaryPool) {
        // Compose a large pool excluding the correct answer
        LinkedHashSet<String> pool = new LinkedHashSet<>();
        for (String s : primaryPool) if (!s.equalsIgnoreCase(answer)) pool.add(cap(s));
        for (String s : secondaryPool) if (!s.equalsIgnoreCase(answer)) pool.add(cap(s));

        List<String> options = new ArrayList<>();
        options.add(cap(answer)); // ensure correct is present

        List<String> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, rnd);
        for (String cand : shuffled) {
            if (options.size() >= 5) break;
            if (!containsIgnoreCase(options, cand)) options.add(cand);
        }
        return options.size() == 5 ? options : options; // return even if <5 (caller checks)
    }

    private boolean containsIgnoreCase(List<String> list, String s) {
        for (String x : list) if (x.equalsIgnoreCase(s)) return true;
        return false;
    }

    private int indexOfIgnoreCase(List<String> list, String s) {
        for (int i = 0; i < list.size(); i++) if (list.get(i).equalsIgnoreCase(s)) return i;
        return -1;
    }

    private String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.replace("\r\n","\n").replace("\r","\n");
        s = s.replace('\u00A0',' ').replace("\u200B",""); // NBSP/ZWSP
        s = s.replace("\u00AD","");                      // soft hyphen
        s = s.replaceAll("(?<=\\p{L})-\\n(?=\\p{Ll})",""); // unhyphenate
        s = s.replaceAll("(?<!\\n)\\n(?!\\n)"," ");        // join wrapped lines
        s = s.replaceAll("[ \\t\\x0B\\f]+"," ");
        s = s.replaceAll("\\n{3,}","\n\n");
        return s.trim();
    }
}