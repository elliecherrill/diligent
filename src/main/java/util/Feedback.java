package util;

public class Feedback {

    private final int lineNumber;
    private final String errorMsg;

    public Feedback(int lineNumber, String errorMsg) {
        this.lineNumber = lineNumber;
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(lineNumber);
        sb.append(": ");
        sb.append(errorMsg);

        return sb.toString();
    }
}
