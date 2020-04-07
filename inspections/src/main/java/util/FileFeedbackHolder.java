package util;

import java.util.ArrayList;
import java.util.List;

public class FileFeedbackHolder {

    private final List<Feedback> feedback;
    private final String filename;
    private final String filepath;

    public FileFeedbackHolder(String filename) {
        feedback = new ArrayList<>();
        this.filename = filename;
        this.filepath = filename.replace(".java", "") + ".html";
    }

    public String getFilepath() {
        return filepath;
    }

    public void addFeedback(Feedback newFeedback) {
        feedback.add(newFeedback);
    }

    public String getFeedbackAsHTMLString() {
        StringBuffer sb = new StringBuffer();

        for (Feedback f : feedback) {
            sb.append(f.toHTMLString());
        }

        return sb.toString();
    }

    private String getFeedbackAsString() {
        StringBuffer sb = new StringBuffer();

        for (Feedback f : feedback) {
            sb.append(f.toString());
            sb.append(" <br> ");
        }

        return sb.toString();
    }

    public String toHTMLString() {
        return "<div onClick=\"location.href = './" + filepath + " '\" id=\"file\">\n" +
                filename + "</div>\n";
    }


}
