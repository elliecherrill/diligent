package feedback;

import util.InspectionPriority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Feedback {

    private final int lineNumber;
    private final String errorMsg;
    private final String filename;
    private final String id;
    private final InspectionPriority priority;

    private boolean isFixed;
    private LocalDateTime lastUpdated;

    public Feedback(int lineNumber, String errorMsg, String filename, String id, InspectionPriority priority) {
        this.lineNumber = lineNumber;
        this.errorMsg = errorMsg;
        this.filename = filename;
        this.id = id;
        this.priority = priority;

        isFixed = false;
        lastUpdated = LocalDateTime.now();
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

    public String toHTMLString() {
        return "<div class=\"feedbackcontainer\" id=\"" + id + "\">\n" +
                "   <div style=\"border: " + getColour() + " solid 2px;\" id=\"feedback\">\n" +
                "       <p style=\"font-weight: 500;\"> " + errorMsg + " </p>\n" +
                "       <p> Line: " + getLineNumberFormatted() + " > Last Updated: " + getLastUpdatedFormatted() + " > Priority: " + priority.toString() + " </p>\n" +
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

    private String getLastUpdatedFormatted() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM/yy");
        return dtf.format(lastUpdated);
    }

    private String getColour() {
        return isFixed ? "green" : "red";
    }

    private String getIgnoreAdviceButton() {
        if (!isFixed) {
            return "<div style=\"margin-left: 2.5%;\">\n" +
                    "   <button class=\"ignorebutton\" id=\"" + id + "\" onclick=\"ignoreAdvice(id)\">\n" +
                    "       Ignore Advice\n" +
                    "   </button>\n" +
                    "</div>\n";
        }

        return "";
    }

    public boolean isFixed() {
        return isFixed;
    }

    public boolean setToFixed() {
        if (isFixed) {
            return false;
        }
        isFixed = true;
        lastUpdated = LocalDateTime.now();
        return true;
    }

    public InspectionPriority getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Feedback) {
            Feedback otherFeedback = (Feedback) obj;

            return lineNumber == otherFeedback.getLineNumber() &&
                    errorMsg.equals(otherFeedback.getErrorMsg()) &&
                    id.equals(otherFeedback.getId()) &&
                    filename.equals(otherFeedback.getFilename()) &&
                    isFixed == otherFeedback.isFixed();
        }

        return false;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getFilename() {
        return filename;
    }

    public String getId() {
        return id;
    }
}
