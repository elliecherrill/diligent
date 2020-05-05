package feedback;

import util.FeedbackType;
import util.InspectionPriority;
import util.ReportLevel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Feedback {

    private final int lineNumber;
    private final String filename;
    private final String className;
    private final String methodName;
    private final String id;
    private final InspectionPriority priority;
    private final FeedbackType feedbackType;

    private boolean isFixed;
    private boolean hasBeenShown;
    private LocalDateTime lastUpdated;
    private ReportLevel reportLevel;

    public Feedback(int lineNumber, String filename, String id, InspectionPriority priority, String className, String methodName, FeedbackType feedbackType) {
        this.lineNumber = lineNumber;
        this.filename = filename;
        this.className = className;
        this.methodName = methodName;
        this.id = id;
        this.priority = priority;
        this.feedbackType = feedbackType;

        isFixed = false;
        hasBeenShown = false;
        lastUpdated = LocalDateTime.now();
        reportLevel = ReportLevel.CLASS;
    }

    public Feedback(int lineNumber, String filename, String id, InspectionPriority priority, String className, FeedbackType feedbackType) {
        this(lineNumber, filename, id, priority, className, null, feedbackType);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(filename);
        sb.append(" > ");
        sb.append(lineNumber);
        sb.append(" : ");
        sb.append(feedbackType.getMessage());
        sb.append(" : ");
        sb.append(isFixed);

        return sb.toString();
    }

    public String toHTMLString(List<InspectionPriority> currentPriorities) {
        if (currentPriorities.contains(priority) || hasBeenShown) {
            hasBeenShown = true;
            return "<div class=\"feedbackcontainer\" id=\"" + id + "\">\n" +
                    "   <div style=\"border: " + getColour() + " solid 2px;\" id=\"feedback\">\n" +
                    "       <div style=\"display: flex; align-items: center;\">\n" +
                    "           <div style=\"flex-grow: 1;\">\n"  +
                    "               <p style=\"font-weight: 500; \"> " + feedbackType.getMessage().replace("$className", className) + " </p>\n" +
                    "           </div>\n" +
                    "           <div style=\"display: flex;\">\n" +
                                    getPriorityIcons(priority) +
                    "           </div>\n" +
                    "       </div>\n"+
                    "       <p> Last Updated: " + getLastUpdatedFormatted() + " </p>\n" +
                    "   </div>\n" +
                    getIgnoreAdviceButton() +
                    "</div>";
        }

        return "";
    }

    private String getLocation() {
        if (reportLevel == ReportLevel.CLASS) {
            return className;

        }
        if (reportLevel == ReportLevel.METHOD) {
            return methodName;

        }
        if (reportLevel == ReportLevel.LINE) {
            return String.valueOf(lineNumber);

        }
        return "";
    }

    private String getPriorityIcons(InspectionPriority priority) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < priority.getNumberOfIcons(); i++) {
            sb.append("<i class=\"material-icons\">priority_high</i>");
            sb.append("\n");
        }

        return sb.toString();
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
                    feedbackType == otherFeedback.getFeedbackType() &&
                    id.equals(otherFeedback.getId()) &&
                    filename.equals(otherFeedback.getFilename()) &&
                    isFixed == otherFeedback.isFixed();
        }

        return false;
    }

    public boolean getHasBeenShown() {
        return hasBeenShown;
    }

    public void setHasBeenShown(boolean hasBeenShown) {
        this.hasBeenShown = hasBeenShown;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFilename() {
        return filename;
    }

    public String getId() {
        return id;
    }

    public FeedbackType getFeedbackType() {
        return feedbackType;
    }
}
