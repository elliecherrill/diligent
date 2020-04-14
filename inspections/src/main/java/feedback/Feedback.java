package feedback;

public class Feedback {

    private final int lineNumber;
    private final String errorMsg;
    private final String filename;
    private boolean isFixed;
    private boolean isHidden;

    public Feedback(int lineNumber, String errorMsg, String filename) {
        this.lineNumber = lineNumber;
        this.errorMsg = errorMsg;
        this.filename = filename;
        isFixed = false;
        isHidden = false;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(filename);
        sb.append(" > ");
        sb.append(lineNumber);
        sb.append(" : ");
        sb.append(errorMsg);
        sb.append(" : ");
        sb.append(isFixed);

        return sb.toString();
    }

    // TODO: How to feedback when Ignore Advice button has been pressed and *use* this?
    public String toHTMLString() {
        return "<div id=\"feedbackcontainer\">\n" +
                "   <div style=\"border: " + getColour() + " solid 2px;\" id=\"feedback\">\n" +
                "       <p style=\"font-weight: 500;\"> " + errorMsg + " </p>\n" +
                "       <p> " + filename + " > " + getLineNumberFormatted() + " </p>\n" +
                "   </div>\n" +
                getIgnoreAdviceButton() +
                "</div>";
    }

    private String getLineNumberFormatted() {
        if (lineNumber >= 0) {
            return String.valueOf(lineNumber);
        }

        //TODO: change this
        return "WHOLE FILE";
    }

    private String getColour() {
        return isFixed ? "green" : "red";
    }

    private String getIgnoreAdviceButton() {
        if (!isFixed) {
            return "<div style=\"margin-left: 2.5%;\">\n" +
                    "   <button id=\"ignorebutton\">\n" +
                    "       Ignore Advice\n" +
                    "   </button>\n" +
                    "</div>\n";
        }

        return "";
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setToFixed() {
        isFixed = true;
    }

    public void setToNotFixed() {
        isFixed = false;
    }

    // TODO: when to call this?
    public void setToHidden() {
        isHidden = true;
    }
}
