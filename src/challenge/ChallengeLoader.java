package challenge;

import engine.AssetManager;
import exception.AssetLoadException;
import exception.InvalidChallengeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads challenges from a text file in res/challenges/. Owner: Andrew (written by Zrs as the working example).
 * The file format is provisional; see the comment at the top of sample.txt.
 */
public final class ChallengeLoader {

    private ChallengeLoader() { }

    /** Loads every question in the file, e.g. load("/challenges/sample.txt"). */
    public static List<Challenge> load(String resourcePath) throws AssetLoadException, InvalidChallengeException {
        List<Challenge> challenges = new ArrayList<>();
        List<String> block = new ArrayList<>(); // lines of the question being read

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(AssetManager.open(resourcePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("---")) {
                    addIfNotEmpty(challenges, block);
                    block = new ArrayList<>();
                } else {
                    block.add(line);
                }
            }
            addIfNotEmpty(challenges, block); // the last question has no --- after it
        } catch (IOException e) {
            throw new AssetLoadException("Could not read " + resourcePath, e);
        }
        return challenges;
    }

    private static void addIfNotEmpty(List<Challenge> challenges, List<String> block)
            throws InvalidChallengeException {
        boolean hasContent = block.stream().anyMatch(l -> !l.isBlank() && !l.trim().startsWith("#"));
        if (hasContent) {
            challenges.add(buildChallenge(block, challenges.size() + 1));
        }
    }

    // Turns one block of KEY=VALUE lines into a Challenge object.
    private static Challenge buildChallenge(List<String> lines, int questionNumber)
            throws InvalidChallengeException {
        String type = null;
        String code = null;
        List<String> choices = new ArrayList<>();
        int answer = -1; // 1-based in the file

        for (String line : lines) {
            if (line.isBlank() || line.trim().startsWith("#")) {
                continue;
            }
            int equals = line.indexOf('=');
            if (equals < 0) {
                throw new InvalidChallengeException("Question " + questionNumber + ": expected KEY=VALUE but got '" + line + "'");
            }
            String key = line.substring(0, equals).trim().toUpperCase();
            String value = line.substring(equals + 1).trim();

            switch (key) {
                case "TYPE" -> type = value;
                case "CODE" -> code = value.replace("\\n", "\n"); // file has a literal backslash-n
                case "CHOICE" -> choices.add(value);
                case "ANSWER" -> {
                    try {
                        answer = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        throw new InvalidChallengeException("Question " + questionNumber + ": ANSWER must be a number");
                    }
                }
                default -> throw new InvalidChallengeException("Question " + questionNumber + ": unknown key " + key);
            }
        }

        if (type == null) {
            throw new InvalidChallengeException("Question " + questionNumber + ": missing TYPE");
        }
        if (type.equals("PREDICT_OUTPUT")) {
            if (code == null || choices.size() < 2 || answer < 1 || answer > choices.size()) {
                throw new InvalidChallengeException("Question " + questionNumber
                        + ": needs CODE, at least 2 CHOICE lines, and an ANSWER between 1 and the number of choices");
            }
            return new PredictOutputChallenge(code, choices, answer - 1);
        }
        throw new InvalidChallengeException("Question " + questionNumber + ": unknown TYPE " + type);
    }
}
