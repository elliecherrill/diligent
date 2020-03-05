package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeedbackHolder {

    //TODO: make this path within their project
    private static final String FILEPATH = "out/index.html";
    private final List<Feedback> feedback;

    public FeedbackHolder() {
        feedback = new ArrayList<>();
    }

    public void addFeedback(Feedback newFeedback) {
        feedback.add(newFeedback);
    }

    public void writeToFile() {
        File index = new File(FILEPATH);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(index));
            initialiseFile(bw);
            write(getFeedbackAsString(), bw);
            close(bw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFeedbackAsString() {
        StringBuffer sb = new StringBuffer();

        for (Feedback f : feedback) {
            sb.append(f.toString());
            sb.append(" <br> ");
        }

        return sb.toString();
    }

    private void initialiseFile(BufferedWriter bw) {
        String template = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Individual Project</title>\n" +
                "</head>\n" +
                "<body>\n";

        write(template, bw);
    }

    private void write(String str, BufferedWriter bw) {
        try {
            bw.write(str);
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void close(BufferedWriter bw) {
        String endStr =  "\n" +
                "</body>\n" +
                "</html>";
        try {
            write(endStr, bw);
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
