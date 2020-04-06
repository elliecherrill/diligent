package util;

import java.util.ArrayList;
import java.util.List;

public class FileFeedbackHolder {

    private final String filename;
    private final List<Feedback> feedback;

    public FileFeedbackHolder(String filename) {
        feedback = new ArrayList<>();
        this.filename = filename;
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
        return "<div style=\"border: white solid 2px; width: 80%; margin-top: 5%; border-radius: 5px;padding: 10px;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                filename + "</div>\n";
    }
}
