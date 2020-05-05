package util;

public enum FeedbackType {
    CAMELCASE("Have you used camelCase throughout class $className?"),
    CLONE("Have you checked for code duplication in class $className?"),
    CONSTRUCTORS_FIRST("Is the constructor(s) the first method(s) in class $className?"),
    FIELDS_FIRST("In class $className, did you declare your fields at the start of the class?"),
    REDUNDANT_ELSE("Are all of your 'else' keywords necessary in class $className?"),
    METHOD_LENGTH("Are all your methods of reasonable length in class $className?"),
    SCREAMING_SNAKE_CASE("Have you used SCREAMING_SNAKE_CASE suitably in class $className?"),
    SHORTHAND_ASSIGNMENT("Did you use shorthand notation (e.g. +=) where possible in class $className?"),
    SIMPLIFY_IF("Did you simplify 'if' statements where possible in class $className?"),
    SINGLE_CHAR_NAME("Have you been sensible with your naming in class $className?"),
    STRING_COMPARISON("In class $className, have you compared Strings correctly?"),
    STRING_CONCAT("Have you avoided string concatenation in loops in class $className?"),
    REDUNDANT_THIS("Have you only used 'this' where necessary in class $className?");

    private final String message;

    FeedbackType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
