package feedback;

import util.InspectionPriority;
import util.Pair;
import util.PsiStmtType;
import util.ReportLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileFeedbackHolder {

    private final Map<FeedbackIdentifier, Feedback> feedback;
    private final String filename;
    private final String filepath;

    // 0 = high, 1 = medium, 2 = low (TRUE if error has *ever* occurred at that level)
    private boolean[] priorityErrors;

    public FileFeedbackHolder(String filename) {
        feedback = new ConcurrentHashMap<>();
        priorityErrors = new boolean[3];
        this.filename = filename;
        this.filepath = filename.replace(".py", ".html");
    }

    public String getFilepath() {
        return filepath;
    }

    public Pair<Boolean, Boolean> addFeedback(FeedbackIdentifier id, Feedback newFeedback) {
        // feedbackIsNew is used for priority counters in ProjectFeedbackHolder
        boolean feedbackIsNew = true;
        // isCurrent is used as a flag to update the file in ProjectFeedbackHolder
        boolean isCurrent = false;

        Feedback oldFeedback = feedback.remove(id);
        if (oldFeedback != null) {
            feedbackIsNew = oldFeedback.isFixed() && !newFeedback.isFixed();
            isCurrent = oldFeedback.equals(newFeedback);
            newFeedback.setHasBeenShown(oldFeedback.getHasBeenShown());
            newFeedback.setReportLevel(oldFeedback.getReportLevel());
            if (newFeedback.setAndIncrementReportCount(oldFeedback.getReportCount())) {
                isCurrent = false;
            }
        }

        feedback.put(id, newFeedback);
        priorityErrors[newFeedback.getPriority().getIndex()] = true;

        return new Pair<>(isCurrent, feedbackIsNew);
    }

    public InspectionPriority fixFeedback(FeedbackIdentifier id) {
        Feedback f = feedback.get(id);

        if (f == null) {
            return InspectionPriority.NONE;
        }

        if (f.setToFixed()) {
            return f.getPriority();
        }

        return InspectionPriority.NONE;
    }

    public Map<InspectionPriority, Integer> updateDeleted() {
        Map<InspectionPriority, Integer> changeToPriorities = new HashMap<>(
                Map.of(InspectionPriority.HIGH, 0,
                        InspectionPriority.MEDIUM, 0,
                        InspectionPriority.LOW, 0));

        for (Map.Entry<FeedbackIdentifier, Feedback> entry : feedback.entrySet()) {
            Feedback f = entry.getValue();

            if (f.isFixed()) {
                continue;
            }

            FeedbackIdentifier feedbackId = entry.getKey();

            if (feedbackId.isDeleted()) {
                f.setToFixed();

                int currCount = changeToPriorities.get(f.getPriority());
                currCount -= 1;
                changeToPriorities.put(f.getPriority(), currCount);
            }
        }

        return changeToPriorities;
    }

    public String getFeedbackAsHTMLString(List<InspectionPriority> priorities) {
        StringBuffer sb = new StringBuffer();

        sb.append(addPriorityPraise(InspectionPriority.HIGH, priorityErrors[InspectionPriority.HIGH.getIndex()], priorities));
        sb.append(addPriorityPraise(InspectionPriority.MEDIUM, priorityErrors[InspectionPriority.MEDIUM.getIndex()], priorities));
        sb.append(addPriorityPraise(InspectionPriority.LOW, priorityErrors[InspectionPriority.LOW.getIndex()], priorities));

        List<Feedback> feedbackToReport = getFeedbackToReport(priorities);

        for (Feedback f : feedbackToReport) {
            sb.append(f.toHTMLString(priorities));
        }

        return sb.toString();
    }

    private List<Feedback> getFeedbackToReport(List<InspectionPriority> priorities) {
        List<FeedbackSignature> alreadyReported = new ArrayList<>();
        List<Feedback> feedbackToReport = new ArrayList<>();

        for (Map.Entry<FeedbackIdentifier, Feedback> entry : feedback.entrySet()) {
            Feedback f = entry.getValue();
            if (f.isFixed()) {
                feedbackToReport.add(f);
                continue;
            }

            String location = "";
            ReportLevel reportLevel = f.getReportLevel();

            if (reportLevel == ReportLevel.CLASS) {
                location = f.getClassName();
            } else if (reportLevel == ReportLevel.METHOD) {
                location = f.getMethodName();
            } else if (reportLevel == ReportLevel.LINE) {
                location = String.valueOf(f.getLineNumber());
            }

            FeedbackSignature feedbackSignature = new FeedbackSignature(f.getFeedbackType(), reportLevel, location, f);

            if (!alreadyReported.contains(feedbackSignature)) {
                feedbackToReport.add(f);
                alreadyReported.add(feedbackSignature);
            } else {
                int index = alreadyReported.indexOf(feedbackSignature);
                Feedback feedback = alreadyReported.get(index).getFeedback();
                feedback.incrementCopyCount();
                if (priorities.contains(feedback.getPriority())) {
                    f.setHasBeenShown(true);
                }
            }
        }

        return feedbackToReport;
    }

    private String addPriorityPraise(InspectionPriority priority, boolean error, List<InspectionPriority> priorities) {
        if (!priorities.contains(priority)) {
            return "";
        }

        if (!error) {
            return "<div class=\"feedbackcontainer\" id=\"praise-" + priority + "\">\n" +
                    "   <div style=\"border: green solid 2px;\" id=\"feedback\">\n" +
                    "       <p style=\"font-weight: 500;\"> Your code is looking " + getPraiseMessage(priority) + "! You have no " + priority.getOutputString() + "-priority errors. </p>\n" +
                    "   </div>\n" +
                    "</div>";
        }

        int fixedCount = getPriorityFixedCount(priority);
        if (fixedCount > 0) {
            return "<div class=\"feedbackcontainer\" id=\"praise-" + priority + "\">\n" +
                    "   <div style=\"border: green solid 2px;\" id=\"feedback\">\n" +
                    "       <p style=\"font-weight: 500;\"> Your code is looking " + getPraiseMessage(priority) + "! You fixed " + fixedCount + " " + priority.getOutputString() + "-priority error" + (fixedCount > 1 ? "s" : "") + ". </p>\n" +
                    "   </div>\n" +
                    "</div>";
        }

        return "";
    }

    private String getPraiseMessage(InspectionPriority priority) {
        if (priority == InspectionPriority.HIGH) {
            return "good";
        }
        if (priority == InspectionPriority.MEDIUM) {
            return "very good";
        }
        if (priority == InspectionPriority.LOW) {
            return "great";
        }
        return "";
    }

    private int getPriorityFixedCount(InspectionPriority priority) {
        int count = 0;

        for (Map.Entry<FeedbackIdentifier, Feedback> entry : feedback.entrySet()) {
            Feedback f = entry.getValue();
            if (f.getPriority() == priority) {
                if (f.isFixed()) {
                    count++;
                } else {
                    return -1;
                }
            }
        }

        return count;
    }

    public String toHTMLString() {
        return "<div onClick=\"location.href = './" + filepath + " '\" id=\"file\">\n" +
                "   <div id=\"filecontainer\">\n" +
                "       <div id=\"filetitle\">\n" +
                filename +
                "       </div>\n" +
                "       <i class=\"material-icons\">" + getStatus() + "</i>\n" +
                "   </div> " +
                "</div>\n";
    }

    public String toSelectedHTMLString() {
        return "<div onClick=\"location.href = './" + filepath + " '\" id=\"file\" style=\"font-weight: 550;border: 5px solid white;\">\n" +
                "   <div id=\"filecontainer\">\n" +
                "       <div id=\"filetitle\">\n" +
                filename +
                "       </div>\n" +
                "       <i class=\"material-icons\">" + getStatus() + "</i>\n" +
                "   </div> " +
                "</div>\n";
    }

    private String getStatus() {
        for (Map.Entry<FeedbackIdentifier, Feedback> entry : feedback.entrySet()) {
            Feedback f = entry.getValue();
            if (!f.isFixed()) {
                return "error_outline";
            }
        }

        return "check_circle_outline";
    }


}
