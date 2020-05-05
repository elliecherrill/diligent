package util;

public enum InspectionPriority {
    HIGH(0, 3),
    MEDIUM(1, 2),
    LOW(2, 1),
    NONE(-1, 0);

    private final int index;
    private final int numberOfIcons;

    InspectionPriority(int index, int numberOfIcons) {
        this.index = index;
        this.numberOfIcons = numberOfIcons;
    }

    public int getIndex() {
        return index;
    }

    public int getNumberOfIcons() {
        return numberOfIcons;
    }
}
