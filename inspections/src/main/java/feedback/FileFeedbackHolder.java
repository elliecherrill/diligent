package feedback;

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

    public void addFeedback(FeedbackIdentifier id, Feedback newFeedback) {
        feedback.remove(id);
        feedback.put(id, newFeedback);
    }

    public void fixFeedback(FeedbackIdentifier id) {
        Feedback f = feedback.get(id);

        if (f == null) {
            return;
        }

        f.setToFixed();
    }

    public void updateDeleted() {
        for (Map.Entry entry : feedback.entrySet()) {
            Feedback f = (Feedback) entry.getValue();

            if (f.isFixed()) {
                continue;
            }

            FeedbackIdentifier feedbackId = (FeedbackIdentifier) entry.getKey();

            if (feedbackId.isDeleted()) {
                f.setToFixed();
            }
        }
    }

    public String getFeedbackAsHTMLString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry entry : feedback.entrySet()) {
            Feedback f = (Feedback) entry.getValue();
            sb.append(f.toHTMLString());
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
