package util;

public class Location {
    private final int codeBlock;
    private final int line;

    public Location(int codeBlock, int line) {
        this.codeBlock = codeBlock;
        this.line = line;
    }

    public int getCodeBlock() {
        return codeBlock;
    }

    public int getLine() {
        return line;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Location) {
            Location other = (Location) obj;

            return other.getCodeBlock() == codeBlock && other.getLine() == line;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return codeBlock * 41 + line * 7;
    }

    @Override
    public String toString() {
        return "<" + codeBlock + ", " + line + ">";
    }
}
