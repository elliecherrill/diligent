package util;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FeedbackHolder {

    //TODO: make this path within their project
    private static final String FILEPATH = "out/diligent.html";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final List<Feedback> feedback;

    public FeedbackHolder() {
        feedback = new ArrayList<>();
    }

    public void addFeedback(Feedback newFeedback) {
        feedback.add(newFeedback);
    }

    public void writeToFile() {
        try {
            String template = getOutputTemplate();
            File newHtmlFile = new File(FILEPATH);
            FileUtils.writeStringToFile(newHtmlFile, template, CHARSET);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        File index = new File(FILEPATH);
//        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter(index));
//            initialiseFile(bw);
//            write(getFeedbackAsString(), bw);
//            close(bw);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
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
                "            <div style=\"border: white solid 2px; width: 80%; margin-top: 5%; border-radius: 5px; padding: 10px;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "                Example File 1\n" +
                "            </div>\n" +
                "            <div style=\"border: white solid 2px; width: 80%; margin-top: 5%; border-radius: 5px;padding: 10px;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "                Example File 2\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        <div style=\"width:100%;height: 100%;display: flex;flex-direction: column;\">\n" +
                "            <div style=\"border: red solid 2px; width: 40%; margin-top: 2.5%; margin-left: 2.5%;border-radius: 5px; padding: 1%;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "                <p style=\"font-weight: 500;\"> TITLE </p>\n" +
                "                <p> EXPLANATION </p>\n" +
                "            </div>\n" +
                "            <div style=\"border: green solid 2px; width: 40%; margin-top: 2.5%; margin-left: 2.5%;border-radius: 5px;padding: 1%;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "                <p style=\"font-weight: 500;\"> TITLE </p>\n" +
                "                <p> EXPLANATION </p>\n" +
                "\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
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
