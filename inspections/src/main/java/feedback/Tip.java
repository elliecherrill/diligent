package feedback;

import util.TipType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tip {

    private final TipType tipType;
    private List<String> isSatisfiedFiles;

    public Tip(TipType tipType) {
        this.tipType = tipType;
        this.isSatisfiedFiles = Collections.synchronizedList(new ArrayList<>());

    }

    public String toHTMLString() {
        return "<div class=\"feedbackcontainer\" id=\"" + tipType + "\">\n" +
                "   <div style=\"border: " + getColour() + " solid 2px;\" id=\"feedback\">\n" +
                "       <p style=\"font-weight: 500;\"> " + getMessage() + " </p>\n" +
                "   </div>\n" +
                getIgnoreAdviceButton() +
                "</div>";
    }

    public boolean removeSatisfied(String filename) {
        return isSatisfiedFiles.remove(filename);
    }

    public boolean addSatisfied(String filename) {
        if (isSatisfiedFiles.contains(filename)) {
            return false;
        }

        isSatisfiedFiles.add(filename);
        return true;
    }

    private String getColour() {
        return isSatisfiedFiles.size() > 0 ? "green" : "red";
    }

    private String getIgnoreAdviceButton() {
        if (isSatisfiedFiles.size() == 0) {
            return "<div style=\"margin-left: 2.5%;\">\n" +
                    "   <button class=\"ignorebutton\" id=\"" + tipType + "\" onclick=\"ignoreAdvice(id)\">\n" +
                    "       Ignore Advice\n" +
                    "   </button>\n" +
                    "</div>\n";
        }

        return "";
    }

    private String getMessage() {
        if (isSatisfiedFiles.size() > 0) {
            return "Great job! You're" + tipType.getFixedMessage();
        }

        return tipType.getTipMessage() + "being used anywhere in this project.";
    }
}
