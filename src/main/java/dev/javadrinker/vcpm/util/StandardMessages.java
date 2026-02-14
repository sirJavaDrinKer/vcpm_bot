package dev.javadrinker.vcpm.util;

public class StandardMessages {
    public static String randomRegularAdvert() {
        String[] messages = {
                "Consider supporting me @ ko-fi.com/javadrinker",
                "Running the bot costs money, consider ko-fi.com/javadrinker",
                "Support development @ ko-fi.com/javadrinker",
                "Keep the bot online by supporting me @ ko-fi.com/javadrinker",
                "Want to help with costs?: ko-fi.com/javadrinker"
        };

        int randomIndex = (int) (Math.random() * messages.length);
        return messages[randomIndex];
    }

    public static String randomEasterEgg() {
        String[] messages = {
                "Java was here",
                "Pros don't fake",
                "What is a Schmeebling?",
                "Waiting for the next match...",
                "Where Tenz?",
                "Valorant Champs Prediction Market"
        };

        int randomIndex = (int) (Math.random() * messages.length);
        return messages[randomIndex];
    }
}
