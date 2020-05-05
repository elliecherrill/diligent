package util;

public enum InspectionPriority {
    HIGH(0),
    MEDIUM(1),
    LOW(2),
    NONE(-1);

    private final int index;

    InspectionPriority(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
