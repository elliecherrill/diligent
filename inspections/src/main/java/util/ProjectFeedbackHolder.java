package util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ProjectFeedbackHolder {

    private static final String FILEPATH = "build/diligent/diligent.html";
    private static final String TEMPLATE_FILEPATH = "build/diligent/$filename";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Notifier NOTIFIER = new Notifier();

    private final Map<String, FileFeedbackHolder> files;
    private final String projectPath;

    private boolean isCurrent;

    public ProjectFeedbackHolder(String projectPath) {
        files = new HashMap<>();
        this.projectPath = projectPath;
        isCurrent = true;
    }

    public void updateReport() {
        if (isCurrent) {
            return;
        }

        try {
            String frameTemplate = getFrameTemplate();
            frameTemplate = frameTemplate.replace("$files", getAllFilesAsHTMLString());
            File frameHtmlFile = new File(projectPath + "/" + FILEPATH);
            FileUtils.writeStringToFile(frameHtmlFile, frameTemplate, CHARSET);

            for (Map.Entry file : files.entrySet()) {
                String filename = (String) file.getKey();
                FileFeedbackHolder fileFeedbackHolder = (FileFeedbackHolder) file.getValue();

                String template = getOutputTemplate();
                template = template.replace("$files", getAllFilesAsHTMLString(filename));
                template = template.replace("$feedback", fileFeedbackHolder.getFeedbackAsHTMLString());
                File newHtmlFile = new File(projectPath + "/" + TEMPLATE_FILEPATH.replace("$filename", fileFeedbackHolder.getFilepath()));
                FileUtils.writeStringToFile(newHtmlFile, template, CHARSET);
            }

            NOTIFIER.notify("Diligent", "Updated <a href=\"" + projectPath + "/" + FILEPATH + "\"> Diligent Feedback Report " + projectPath + "/" + FILEPATH + " </a>");


        } catch (IOException | IndexOutOfBoundsException e) {
            System.err.println(e);
        }

        isCurrent = true;
    }

    public void addFeedback(String filename, String feedbackId, Feedback feedback) {
        FileFeedbackHolder fileFeedbackHolder = files.get(filename);
        if (fileFeedbackHolder == null) {
            fileFeedbackHolder = new FileFeedbackHolder(filename);
        }
        fileFeedbackHolder.addFeedback(feedbackId, feedback);
        files.put(filename, fileFeedbackHolder);

        isCurrent = false;
    }

    public void fixFeedback(String filename, String feedbackId) {
        FileFeedbackHolder fileFeedbackHolder = files.get(filename);
        if (fileFeedbackHolder == null) {
            return;
        }
        fileFeedbackHolder.fixFeedback(feedbackId);

        isCurrent = false;
    }

    private String getAllFilesAsHTMLString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry file : files.entrySet()) {
            FileFeedbackHolder fileFeedbackHolder = (FileFeedbackHolder) file.getValue();
            sb.append(fileFeedbackHolder.toHTMLString());
        }

        return sb.toString();
    }

    private String getAllFilesAsHTMLString(String selectedFile) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry file : files.entrySet()) {
            String filename = (String) file.getKey();
            FileFeedbackHolder fileFeedbackHolder = (FileFeedbackHolder) file.getValue();

            if (filename.equals(selectedFile)) {
                sb.append(fileFeedbackHolder.toSelectedHTMLString());
            } else {
                sb.append(fileFeedbackHolder.toHTMLString());
            }
        }

        return sb.toString();
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
                "    <style>\n" +
                "        #topbar {\n" +
                "            min-height: 64px;\n" +
                "            background-color: #34558b;\n" +
                "            display: flex;\n" +
                "            position: relative;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        #topbartitle {\n" +
                "            font-family: Roboto, Helvetica, Arial, sans-serif;\n" +
                "            font-weight: 500;\n" +
                "            line-height: 1.6;\n" +
                "            letter-spacing: 0.0075em;\n" +
                "            color:white;\n" +
                "            margin: 0px;\n" +
                "            padding-left: 24px;\n" +
                "        }\n" +
                "        #page {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "        #filesidebar {\n" +
                "            width: 15%;\n" +
                "            background-color: #34558b;\n" +
                "            color: white;\n" +
                "            height: 100%;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        #file {\n" +
                "            cursor: pointer;\n" +
                "            border: white solid 2px;\n" +
                "            width: 80%;\n" +
                "            margin-top: 5%;\n" +
                "            border-radius: 5px;\n" +
                "            padding: 10px;\n" +
                "            font-family: Roboto, Helvetica, Arial, sans-serif;\n" +
                "        }\n" +
                "        #mainpage {\n" +
                "            width:100%;\n" +
                "            height: 100%;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "        }\n" +
                "        #feedbackcontainer {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "            align-items: center;\n" +
                "            margin-top: 2.5%;\n" +
                "            margin-left: 2.5%;\n" +
                "        }\n" +
                "        #feedback {\n" +
                "            width: 40%;\n" +
                "            border-radius: 5px;\n" +
                "            padding: 1%;\n" +
                "            font-family: Roboto, Helvetica, Arial, sans-serif;\n" +
                "        }\n" +
                "        #ignorebutton {\n" +
                "            background-color: #34558b;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            border-radius: 5px;\n" +
                "            font-family: Roboto, Helvetica, Arial, sans-serif;\n" +
                "        }\n" +
                "        #progress {\n" +
                "            border: white solid 2px;\n" +
                "            border-radius: 50%;\n" +
                "            height: 15px;\n" +
                "            width: 15px;\n" +
                "        }\n" +
                "        #filetitle {\n" +
                "            display: flex;\n" +
                "            flex-grow: 1;\n" +
                "        }\n" +
                "        #filecontainer {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body style=\"margin: 0px;height: 100%;\">\n" +
                "<div id=\"root\" style=\"height: 100%;\">\n" +
                "    <div id=\"topbar\">\n" +
                "        <h1 id=\"topbartitle\">\n" +
                "            Diligent\n" +
                "        </h1>\n" +
                "    </div>\n" +
                "    <div id=\"page\">\n" +
                "        <div id=\"filesidebar\">\n" +
                "            $files " +
                "        </div>\n" +
                "        <div id=\"mainpage\">\n" +
                "            $feedback\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String getFrameTemplate() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\" style=\"height: 100%;\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"theme-color\" content=\"#34558b\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Roboto:300,400,500\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/icon?family=Material+Icons\">\n" +
                "    <title>Diligent</title>\n" +
                "    <style>\n" +
                "        #topbar {\n" +
                "            min-height: 64px;\n" +
                "            background-color: #34558b;\n" +
                "            display: flex;\n" +
                "            position: relative;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        #topbartitle {\n" +
                "            font-family: Roboto, Helvetica, Arial, sans-serif;\n" +
                "            font-weight: 500;\n" +
                "            line-height: 1.6;\n" +
                "            letter-spacing: 0.0075em;\n" +
                "            color:white;\n" +
                "            margin: 0px;\n" +
                "            padding-left: 24px;\n" +
                "        }\n" +
                "        #page {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "        #filesidebar {\n" +
                "            width: 15%;\n" +
                "            background-color: #34558b;\n" +
                "            color: white;\n" +
                "            height: 100%;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        #file {\n" +
                "            cursor: pointer;\n" +
                "            border: white solid 2px;\n" +
                "            width: 80%;\n" +
                "            margin-top: 5%;\n" +
                "            border-radius: 5px;\n" +
                "            padding: 10px;\n" +
                "            font-family: Roboto, Helvetica, Arial, sans-serif;\n" +
                "        }\n" +
                "        #mainpage {\n" +
                "            width:100%;\n" +
                "            height: 100%;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "        }\n" +
                "        #progress {\n" +
                "            border: white solid 2px;\n" +
                "            border-radius: 50%;\n" +
                "            height: 15px;\n" +
                "            width: 15px;\n" +
                "        }\n" +
                "        #filetitle {\n" +
                "            display: flex;\n" +
                "            flex-grow: 1;\n" +
                "        }\n" +
                "        #filecontainer {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body style=\"margin: 0px;height: 100%;\">\n" +
                "<div id=\"root\" style=\"height: 100%;\">\n" +
                "    <div id=\"topbar\">\n" +
                "        <h1 id=\"topbartitle\">\n" +
                "            Diligent\n" +
                "        </h1>\n" +
                "    </div>\n" +
                "    <div id=\"page\">\n" +
                "        <div id=\"filesidebar\">\n" +
                "            $files " +
                "        </div>\n" +
                "        <div id=\"mainpage\">\n" +
                "            <div style=\"width: 40%; margin-top: 2.5%; margin-left: 2.5%;padding: 1%;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "                <p style=\"font-weight: 500;\"> Please select a file. </p>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }
}
