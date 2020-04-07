package util;

import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileFeedbackHolder {

    private final Map<String, Feedback> feedback;
    private final String filename;
    private final String filepath;

    public FileFeedbackHolder(String filename) {
        feedback = new HashMap<>();
        this.filename = filename;
        this.filepath = filename.replace(".java", "") + ".html";
    }

    public String getFilepath() {
        return filepath;
    }

    public void addFeedback(String id, Feedback newFeedback) {
        feedback.put(id, newFeedback);
    }

    public void fixFeedback(String id) {
        Feedback f = feedback.get(id);

        if (f == null) {
            return;
        }

        f.setToFixed();
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
                filename + "</div>\n";
    }


}
