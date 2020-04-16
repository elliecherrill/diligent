package util;

public enum TipType {
    FOR_LOOPS("For loops are not ", " using for loops."),
    NO_FOR_LOOPS("For loops are ", " not using for loops."),
    WHILE_LOOPS("While loops are not ", " using while loops."),
    NO_WHILE_LOOPS("While loops are ", " not using while loops."),
    STREAMS("Streams are not ", " using streams."),
    NO_STREAMS("Streams are being ", " not using streams."),
    INHERITANCE("Inheritance is not ", " using inheritance."),
    NO_INHERITANCE("Inheritance is ", " not using inheritance."),
    INTERFACES("Interfaces are not ", " using interfaces."),
    NO_INTERFACES("Interfaces are ", " not using interfaces.");

    private final String tipMessage;
    private final String fixedMessage;

    TipType(String tipMessage, String fixedMessage) {
        this.tipMessage = tipMessage;
        this.fixedMessage = fixedMessage;
    }

    public String getTipMessage() {
        return tipMessage;
    }

    public String getFixedMessage() {
        return fixedMessage;
    }
}
