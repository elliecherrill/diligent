package util;

public enum InspectionPriority {
    HIGH(0, 3, "high"),
    MEDIUM(1, 2, "medium"),
    LOW(2, 1, "low"),
    NONE(-1, 0, "");

    private final int index;
    private final int numberOfIcons;
    private final String outputString;

    InspectionPriority(int index, int numberOfIcons, String outputString) {
        this.index = index;
        this.numberOfIcons = numberOfIcons;
        this.outputString = outputString;
    }

    public int getIndex() {
        return index;
    }

    public int getNumberOfIcons() {
        return numberOfIcons;
    }

    public String getOutputString() {
        return outputString;
    }
}
