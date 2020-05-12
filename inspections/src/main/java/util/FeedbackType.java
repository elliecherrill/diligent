package util;

public enum FeedbackType {
    CAMELCASE("Have you used camelCase throughout class $className?",
            "Great job! You used camelCase on line $lineNumber."),
    CLONE("Have you checked for code duplication in class $className?",
            "Nice. You reduced code duplication on line $lineNumber."),
    CONSTRUCTORS_FIRST("Is the constructor(s) the first method(s) in class $className?",
            "Good work! You moved the constructor to the top of the class from line $lineNumber."),
    FIELDS_FIRST("In class $className, did you declare your fields at the start of the class?",
            "Good work! You moved the fields to the top of the class from line $lineNumber."),
    REDUNDANT_ELSE("Are all of your 'else' keywords necessary in class $className?",
            "Great job! You removed the redundant 'else' from line $lineNumber."),
    METHOD_LENGTH("Are all your methods of reasonable length in class $className?",
            "Nicely done. You made the method on line $lineNumber a more reasonable length."),
    SCREAMING_SNAKE_CASE("Have you used SCREAMING_SNAKE_CASE suitably in class $className?",
            "Great job! You used SCREAMING_SNAKE_CASE on line $lineNumber."),
    SHORTHAND_ASSIGNMENT("Did you use shorthand notation (e.g. +=) where possible in class $className?",
            "Nicely done. You used shorthand notation on line $lineNumber."),
    SIMPLIFY_IF("Did you simplify 'if' statements where possible in class $className?",
            "Nice. You simplified the 'if' statement on line $lineNumber."),
    SINGLE_CHAR_NAME("Have you been sensible with your naming in class $className?",
            "Nicely done. Your name on line $lineNumber is more sensible."),
    STRING_COMPARISON("In class $className, have you compared Strings correctly?",
            "Great! You used the right method for comparing Strings on line $lineNumber."),
    STRING_CONCAT("Have you avoided string concatenation in loops in class $className?",
            "Great! You have removed the string concatenation on line $lineNumber."),
    REDUNDANT_THIS("Have you only used 'this' where necessary in class $className?",
            "Great job! You removed the redundant 'this' from line $lineNumber.");

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
