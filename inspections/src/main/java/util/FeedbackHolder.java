package util;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackHolder {

    //TODO: make this path within their project
    private static final String FILEPATH = "out/diligent.html";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final FeedbackHolder INSTANCE = new FeedbackHolder();
    private final Map<String, FileFeedbackHolder> files;

    private FeedbackHolder() {
        files = new HashMap<>();
    }

    public static FeedbackHolder getInstance() {
        return INSTANCE;
    }

    public void writeToFile() {
        try {
            String template = getOutputTemplate();
            template = template.replace("$files", getAllFilesAsHTMLString());
            template = template.replace("$feedback", getAllFeedbackAsHTMLString());
            File newHtmlFile = new File(FILEPATH);
            FileUtils.writeStringToFile(newHtmlFile, template, CHARSET);
        } catch (IOException | IndexOutOfBoundsException e) {
            System.err.println(e);
        }
    }

    private String getOutputTemplate() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\" style=\"height: 100%;\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"theme-color\" content=\"#34558b\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Roboto:300,400,500\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/icon?family=Material+Icons\">\n" +
                "    <title>Diligent</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0px;height: 100%;\">\n" +
                "<div id=\"root\" style=\"height: 100%;\">\n" +
                "    <div style=\"min-height: 64px;background-color: #34558b;display: flex;position: relative;align-items: center;\">\n" +
                "        <h1 style=\"font-family: Roboto, Helvetica, Arial, sans-serif;font-weight: 500;line-height: 1.6;letter-spacing: 0.0075em;color:white;margin: 0px;padding-left: 24px;\">\n" +
                "            Diligent\n" +
                "        </h1>\n" +
                "    </div>\n" +
                "    <div style=\"display: flex;flex-direction: row;height: 100%;\">\n" +
                "        <div style=\"width: 15%; background-color: #34558b; color: white;height: 100%;display: flex;flex-direction: column;align-items: center;\">\n" +
                "            $files " +
                "        </div>\n" +
                "        <div style=\"width:100%;height: 100%;display: flex;flex-direction: column;\">\n" +
                "            $feedback" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String getAllFilesAsHTMLString() {
        StringBuffer sb = new StringBuffer();

        for (Map.Entry file : files.entrySet()) {
            FileFeedbackHolder fileFeedbackHolder = (FileFeedbackHolder) file.getValue();
            sb.append(fileFeedbackHolder.toHTMLString());
        }

        return sb.toString();
    }

    private String getAllFeedbackAsHTMLString() {
        StringBuffer sb = new StringBuffer();

        for (Map.Entry file : files.entrySet()) {
            FileFeedbackHolder fileFeedbackHolder = (FileFeedbackHolder) file.getValue();
            sb.append(fileFeedbackHolder.getFeedbackAsHTMLString());
        }

        return sb.toString();
    }

    public void addFeedback(String filename, Feedback feedback) {
        FileFeedbackHolder fileFeedbackHolder = files.get(filename);
        if (fileFeedbackHolder == null) {
            fileFeedbackHolder = new FileFeedbackHolder(filename);
        }
        fileFeedbackHolder.addFeedback(feedback);
        files.put(filename, fileFeedbackHolder);
    }
}
