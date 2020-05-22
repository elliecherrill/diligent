package util;

public enum TipType {
    FOR_LOOPS("For loops are not ", " using for loops.", true),
    NO_FOR_LOOPS("For loops are ", " not using for loops.", false),
    WHILE_LOOPS("While loops are not ", " using while loops.", true),
    NO_WHILE_LOOPS("While loops are ", " not using while loops.", false),
    STREAMS("Streams are not ", " using streams.", true),
    NO_STREAMS("Streams are being ", " not using streams.", false),
    INHERITANCE("Inheritance is not ", " using inheritance.", true),
    NO_INHERITANCE("Inheritance is ", " not using inheritance.", false),
    INTERFACES("Interfaces are not ", " using interfaces.", true),
    NO_INTERFACES("Interfaces are ", " not using interfaces.", false);

    private final String tipMessage;
    private final String fixedMessage;
    private final boolean lookingForAbsence;

    TipType(String tipMessage, String fixedMessage, boolean lookingForAbsence) {
        this.tipMessage = tipMessage;
        this.fixedMessage = fixedMessage;
        this.lookingForAbsence = lookingForAbsence;
    }

    public String getTipMessage() {
        return tipMessage;
    }

    public String getFixedMessage() {
        return fixedMessage;
    }

    public boolean isLookingForAbsence() {
        return lookingForAbsence;
    }
}
