package util;

public enum FeedbackType {
    SNAKECASE("Have you used snake_case throughout file $className?",
            "Great job! You used snake_case on line $lineNumber."),
    METHOD_LENGTH("Are all your methods of reasonable length in file $className?",
            "Nicely done. You made the method on line $lineNumber a more reasonable length."),
    UNUSED_VARIABLES("Did you use '_' where possible in file $className?",
            "Nice. You used '_' for an unused variable on line $lineNumber.");

    private final String message;
    private final String fixedMessage;

    FeedbackType(String message, String fixedMessage) {
        this.message = message;
        this.fixedMessage = fixedMessage;
    }

    public String getMessage() {
        return message;
    }

    public String getFixedMessage() {
        return fixedMessage;
    }
}
