package util;

public class Feedback {

    private final int lineNumber;
    private final String errorMsg;
    private final String filename;
    private boolean fixed;

    public Feedback(int lineNumber, String errorMsg, String filename) {
        this.lineNumber = lineNumber;
        this.errorMsg = errorMsg;
        this.filename = filename;
        fixed = false;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(filename);
        sb.append(" > ");
        sb.append(lineNumber);
        sb.append(" : ");
        sb.append(errorMsg);

        return sb.toString();
    }

    // TODO: How to feedback when Ignore Advice button has been pressed and *use* this?
    public String toHTMLString() {
        return "<div id=\"feedbackcontainer\">\n" +
                "   <div style=\"border: " + getColour() + " solid 2px;\" id=\"feedback\">\n" +
                "       <p style=\"font-weight: 500;\"> " + errorMsg + " </p>\n" +
                "       <p> " + filename + " > " + lineNumber + " </p>\n" +
                "   </div>\n" +
                getIgnoreAdviceButton() +
                "</div>";
    }

    // TODO: When to update fixed?
    public void setToFixed() {
        fixed = true;
    }

    private String getColour() {
        return fixed ? "green" : "red";
    }

    private String getIgnoreAdviceButton() {
        if (!fixed) {
            return "<div style=\"margin-left: 2.5%;\">\n" +
                    "   <button id=\"ignorebutton\">\n" +
                    "       Ignore Advice\n" +
                    "   </button>\n" +
                    "</div>\n";
        }

        return "";
    }
}
