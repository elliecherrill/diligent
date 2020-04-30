package feedback;

import util.InspectionPriority;
import util.PsiStmtType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileFeedbackHolder {

    private final Map<FeedbackIdentifier, Feedback> feedback;
    private final String filename;
    private final String filepath;

    public FileFeedbackHolder(String filename) {
        feedback = new ConcurrentHashMap<>();
        this.filename = filename;
        this.filepath = filename.replace(".java", ".html");
    }

    public String getFilepath() {
        return filepath;
    }

    public boolean addFeedback(FeedbackIdentifier id, Feedback newFeedback) {
        if (id.getType() == PsiStmtType.LEFT_THIS_EXPR || id.getType() == PsiStmtType.RIGHT_THIS_EXPR) {
            for (Map.Entry entry : feedback.entrySet()) {
                FeedbackIdentifier entryId = (FeedbackIdentifier) entry.getKey();

                if (entryId.getType() == id.getType() && entryId.getInitialElement().equals(id.getInitialElement())) {
                    feedback.remove(entryId);
                }

            }
        }

        boolean feedbackIsNew = true;
        if (feedback.remove(id) != null) {
            feedbackIsNew = false;
        }

        feedback.put(id, newFeedback);

        return feedbackIsNew;
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

        for (Map.Entry entry : feedback.entrySet()) {
            Feedback f = (Feedback) entry.getValue();

            if (f.isFixed()) {
                continue;
            }

            FeedbackIdentifier feedbackId = (FeedbackIdentifier) entry.getKey();

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

        for (Map.Entry entry : feedback.entrySet()) {
            Feedback f = (Feedback) entry.getValue();
            if (priorities.contains(f.getPriority())) {
                sb.append(f.toHTMLString());
            }
        }

        return sb.toString();
    }

    public String toHTMLString() {
        return "<div onClick=\"location.href = './" + filepath + " '\" id=\"file\">\n" +
                "   <div id=\"filecontainer\">\n" +
                "       <div id=\"filetitle\">\n" +
                filename +
                "       </div>\n" +
                "       <div style=\"background-color: " + getStatus() + ";\" id=\"progress\"> </div>\n" +
                "   </div> " +
                "</div>\n";
    }

    public String toSelectedHTMLString() {
        return "<div onClick=\"location.href = './" + filepath + " '\" id=\"file\" style=\"font-weight: 550;border: 5px solid white;\">\n" +
                "   <div id=\"filecontainer\">\n" +
                "       <div id=\"filetitle\">\n" +
                filename +
                "       </div>\n" +
                "       <div style=\"background-color: " + getStatus() + ";\" id=\"progress\"> </div>\n" +
                "   </div> " +
                "</div>\n";
    }

    private String getStatus() {
        for (Map.Entry entry : feedback.entrySet()) {
            Feedback f = (Feedback) entry.getValue();
            if (!f.isFixed()) {
                return "red";
            }
        }

        return "green";
    }


}
