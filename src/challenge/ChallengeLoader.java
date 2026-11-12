package challenge;

import battle.Difficulty;
import engine.AssetManager;
import exception.AssetLoadException;
import exception.InvalidChallengeException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads all Challenge types from res/challenges/ using getResourceAsStream or AssetManager.open.
 * Owner: Andrew.
 */
public final class ChallengeLoader {

    private ChallengeLoader() { }

    /** Legacy alias: returns challenges or empty list on exception. */
    public static List<Challenge> load(String resourcePath) throws AssetLoadException, InvalidChallengeException {
        return loadChallenges(resourcePath);
    }

    /** Loads all challenges from the given resource path. */
    public static List<Challenge> loadChallenges(String resourcePath) {
        List<Challenge> challenges = new ArrayList<>();
        String normalizedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;

        InputStream is = null;
        try {
            is = AssetManager.open(normalizedPath);
        } catch (Exception e) {
            is = ChallengeLoader.class.getResourceAsStream(normalizedPath);
        }

        if (is == null) {
            System.err.println("Warning: Resource not found: " + resourcePath);
            return challenges;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> block = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("---")) {
                    parseBlock(challenges, block);
                    block.clear();
                } else {
                    block.add(line);
                }
            }
            if (!block.isEmpty()) {
                parseBlock(challenges, block);
            }
        } catch (Exception e) {
            System.err.println("Error reading challenges from " + resourcePath + ": " + e.getMessage());
        }

        return challenges;
    }

    private static void parseBlock(List<Challenge> challenges, List<String> block) {
        if (block.isEmpty() || block.stream().allMatch(l -> l.isBlank() || l.trim().startsWith("#"))) {
            return;
        }

        // Check if block uses legacy format (starts with TYPE= or CHOICE=)
        boolean isLegacy = block.stream().anyMatch(l -> {
            String t = l.trim().toUpperCase();
            return t.startsWith("TYPE=") || t.startsWith("CHOICE=");
        });

        if (isLegacy) {
            Challenge c = parseLegacyBlock(block);
            if (c != null) {
                challenges.add(c);
            }
            return;
        }

        // Parse modern sectioned format (ID:, TYPE:, DIFFICULTY:, etc.)
        String id = "";
        String type = "";
        Difficulty diff = Difficulty.EASY;
        StringBuilder prompt = new StringBuilder();
        StringBuilder code = new StringBuilder();
        String answer = "";
        String explanation = "";
        String currentSection = "";

        for (String line : block) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            if (trimmed.startsWith("ID:")) {
                id = trimmed.substring(3).trim();
                currentSection = "";
            } else if (trimmed.startsWith("TYPE:")) {
                type = trimmed.substring(5).trim();
                currentSection = "";
            } else if (trimmed.startsWith("DIFFICULTY:")) {
                try {
                    diff = Difficulty.valueOf(trimmed.substring(11).trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    diff = Difficulty.EASY;
                }
                currentSection = "";
            } else if (trimmed.startsWith("PROMPT:")) {
                currentSection = "PROMPT";
            } else if (trimmed.startsWith("CODE:")) {
                currentSection = "CODE";
            } else if (trimmed.startsWith("ANSWER:")) {
                answer = trimmed.substring(7).trim();
                currentSection = "";
            } else if (trimmed.startsWith("EXPLANATION:")) {
                explanation = trimmed.substring(12).trim();
                currentSection = "";
            } else {
                if ("PROMPT".equals(currentSection)) {
                    prompt.append(trimmed).append("\n");
                } else if ("CODE".equals(currentSection)) {
                    code.append(line).append("\n");
                }
            }
        }

        Challenge created = buildChallenge(
                type, id, prompt.toString().trim(), code.toString().stripTrailing(), diff, answer, explanation
        );
        if (created != null) {
            challenges.add(created);
        }
    }

    private static Challenge parseLegacyBlock(List<String> lines) {
        String type = null;
        String code = null;
        List<String> choices = new ArrayList<>();
        int answer = -1;

        for (String line : lines) {
            if (line.isBlank() || line.trim().startsWith("#")) {
                continue;
            }
            int equals = line.indexOf('=');
            if (equals < 0) continue;
            String key = line.substring(0, equals).trim().toUpperCase();
            String value = line.substring(equals + 1).trim();

            switch (key) {
                case "TYPE" -> type = value;
                case "CODE" -> code = value.replace("\\n", "\n");
                case "CHOICE" -> choices.add(value);
                case "ANSWER" -> {
                    try {
                        answer = Integer.parseInt(value);
                    } catch (NumberFormatException ignored) { }
                }
            }
        }

        if ("PREDICT_OUTPUT".equalsIgnoreCase(type) && code != null) {
            return new PredictOutputChallenge(code, choices, answer > 0 ? answer - 1 : 0);
        }
        return null;
    }

    private static Challenge buildChallenge(String type, String id, String prompt, String code,
                                            Difficulty diff, String answer, String explanation) {
        return switch (type.toUpperCase()) {
            case "PREDICT", "PREDICT_OUTPUT" -> new PredictOutputChallenge(id, prompt, code, diff, answer);
            case "FILL_BLANK" -> new FillBlankChallenge(id, prompt, code, diff, answer);
            case "SPOT_BUG" -> new SpotTheBugChallenge(id, prompt, code, diff, answer, explanation);
            case "ARRANGE" -> new ArrangeCodeChallenge(id, prompt, code, diff, answer);
            default -> {
                System.err.println("Unknown challenge type: " + type);
                yield null;
            }
        };
    }
}
