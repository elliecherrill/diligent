package util;

public class Feedback {

    private final int lineNumber;
    private final String errorMsg;

    public Feedback(int lineNumber, String errorMsg) {
        this.lineNumber = lineNumber;
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(lineNumber);
        sb.append(": ");
        sb.append(errorMsg);

        return sb.toString();
    }

    public String toHTMLString() {
        return "<div style=\"border: red solid 2px; width: 40%; margin-top: 2.5%; margin-left: 2.5%;border-radius: 5px; padding: 1%;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "   <p style=\"font-weight: 500;\"> " + errorMsg + " </p>\n" +
                "   <p> " + lineNumber + " </p>\n" +
                "</div>\n";

    }
}
