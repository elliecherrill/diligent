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
        return "<div style=\"display: flex;flex-direction: row; align-items: center;margin-top: 2.5%; margin-left: 2.5%;\">\n" +
                "   <div style=\"border: " + getColour() + " solid 2px; width: 40%; border-radius: 5px; padding: 1%;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "       <p style=\"font-weight: 500;\"> " + errorMsg + " </p>\n" +
                "       <p> " + filename + " > " + lineNumber + " </p>\n" +
                "   </div>\n" +
                "   <div style=\"margin-left: 2.5%;\">\n" +
                "       <button style=\"background-color: #34558b; color: white; border: none; border-radius: 5px;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "           Ignore Advice\n" +
                "       </button>\n" +
                "   </div>\n" +
                "</div>";
    }

    // TODO: When to update fixed?
    public void setToFixed() {
        fixed = true;
    }

    private String getColour() {
        return fixed ? "green" : "red";
    }
}
