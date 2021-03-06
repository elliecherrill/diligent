package feedback;

import com.intellij.openapi.project.Project;
import org.apache.commons.io.FileUtils;
import util.InspectionPriority;
import util.Notifier;
import util.Pair;
import util.TipType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ProjectFeedbackHolder {

    private static final String FILEPATH = "build/diligent/diligent.html";
    private static final String TEMPLATE_FILEPATH = "build/diligent/$filename";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Notifier NOTIFIER = new Notifier();

    private final Map<String, FileFeedbackHolder> files;
    private final Map<InspectionPriority, Integer> priorityCount;
    private final String projectPath;
    private final Project project;
    private final TipHolder tipHolder;

    private final ReentrantLock updateTipsLock = new ReentrantLock();
    private final ReentrantLock updateFeedbackLock = new ReentrantLock();

    private boolean isCurrent;

    public ProjectFeedbackHolder(Project project) {
        this.project = project;

        files = new ConcurrentHashMap<>();
        priorityCount = new ConcurrentHashMap<>();
        initMap();
        projectPath = project.getBasePath();
        isCurrent = true;
        tipHolder = new TipHolder();
    }

    private void initMap() {
        priorityCount.put(InspectionPriority.HIGH, 0);
        priorityCount.put(InspectionPriority.MEDIUM, 0);
        priorityCount.put(InspectionPriority.LOW, 0);
    }

    public void updateReport() {
        updateTipsLock.lock();
        updateFeedbackLock.lock();

        try {

            updateDeleted();

            if (isCurrent) {
                return;
            }

            List<InspectionPriority> reportedPriorities = getReportedPriorities();

            try {
                String frameTemplate = getFrameTemplate();
                frameTemplate = frameTemplate.replace("$files", getAllFilesAsHTMLString());
                File frameHtmlFile = new File(projectPath + "/" + FILEPATH);
                FileUtils.writeStringToFile(frameHtmlFile, frameTemplate, CHARSET);

                if (!tipHolder.isCurrent()) {
                    String template = getOutputTemplate();
                    template = template.replace("$files", getAllFilesAsHTMLString("project-tips"));
                    template = template.replace("$feedback", tipHolder.getTipsAsHTMLString());
                    template = template.replace("$project", project.getName());
                    template = template.replace("$current_file", "project-tips");
                    File newHtmlFile = new File(projectPath + "/" + TEMPLATE_FILEPATH.replace("$filename", "project-tips.html"));
                    FileUtils.writeStringToFile(newHtmlFile, template, CHARSET);

                    tipHolder.update();
                }

                for (Map.Entry<String, FileFeedbackHolder> file : files.entrySet()) {
                    String filename = file.getKey();
                    FileFeedbackHolder fileFeedbackHolder = file.getValue();

                    String template = getOutputTemplate();
                    template = template.replace("$files", getAllFilesAsHTMLString(filename));
                    template = template.replace("$feedback", fileFeedbackHolder.getFeedbackAsHTMLString(reportedPriorities));
                    template = template.replace("$project", project.getName());
                    template = template.replace("$current_file", filename);
                    File newHtmlFile = new File(projectPath + "/" + TEMPLATE_FILEPATH.replace("$filename", fileFeedbackHolder.getFilepath()));
                    FileUtils.writeStringToFile(newHtmlFile, template, CHARSET);
                }

                String browserLink = "http://localhost:63342/" + project.getName() + "/" + FILEPATH;
                NOTIFIER.notify(project, "Diligent", "Updated Feedback Report. <a style=\"cursor: pointer;\" href=\"" + browserLink + "\">Click Here to View</a>");

                isCurrent = true;
            } catch (IOException | IndexOutOfBoundsException e) {
                System.err.println(e);
            }

        } finally {
            updateFeedbackLock.unlock();
            updateTipsLock.unlock();
        }
    }

    private void updateDeleted() {
        for (Map.Entry<String, FileFeedbackHolder> file : files.entrySet()) {
            FileFeedbackHolder fileFeedbackHolder = file.getValue();
            Map<InspectionPriority, Integer> changeToPriorities = fileFeedbackHolder.updateDeleted();

            if (!(changeToPriorities.get(InspectionPriority.HIGH) == 0 &&
                    changeToPriorities.get(InspectionPriority.MEDIUM) == 0 &&
                    changeToPriorities.get(InspectionPriority.LOW) == 0)) {
                isCurrent = false;
                updatePriorities(changeToPriorities);
            }
        }
    }

    private void updatePriorities(Map<InspectionPriority, Integer> changeToPriorities) {
        for (Map.Entry<InspectionPriority, Integer> p : changeToPriorities.entrySet()) {
            editCount(p.getKey(), p.getValue());
        }
    }

    private List<InspectionPriority> getReportedPriorities() {
        List<InspectionPriority> reportedPriorities = new ArrayList<>();

        reportedPriorities.add(InspectionPriority.HIGH);

        if (priorityCount.get(InspectionPriority.HIGH) == 0) {
            reportedPriorities.add(InspectionPriority.MEDIUM);

            if (priorityCount.get(InspectionPriority.MEDIUM) == 0) {
                reportedPriorities.add(InspectionPriority.LOW);
            }
        }

        return reportedPriorities;
    }

    public void addFeedback(String filename, FeedbackIdentifier feedbackId, Feedback feedback) {
        updateFeedbackLock.lock();
        try {
            FileFeedbackHolder fileFeedbackHolder = files.get(filename);
            if (fileFeedbackHolder == null) {
                fileFeedbackHolder = new FileFeedbackHolder(filename);
            }

            Pair<Boolean, Boolean> addFeedbackRes = fileFeedbackHolder.addFeedback(feedbackId, feedback);
            if (addFeedbackRes.getSecond()) {
                editCount(feedback.getPriority(), 1);
            }
            files.put(filename, fileFeedbackHolder);

            if (!addFeedbackRes.getFirst()) {
                isCurrent = false;
            }
        } finally {
            updateFeedbackLock.unlock();
        }
    }

    public void fixFeedback(String filename, FeedbackIdentifier feedbackId) {
        updateFeedbackLock.lock();
        try {
            FileFeedbackHolder fileFeedbackHolder = files.get(filename);
            if (fileFeedbackHolder == null) {
                files.put(filename, new FileFeedbackHolder(filename));
                isCurrent = false;
                return;
            }

            InspectionPriority priority = fileFeedbackHolder.fixFeedback(feedbackId);
            if (priority != InspectionPriority.NONE) {
                editCount(priority, -1);
                isCurrent = false;
            }
        } finally {
            updateFeedbackLock.unlock();
        }
    }

    private void editCount(InspectionPriority priority, int change) {
        if (change == 0) {
            return;
        }

        int currCount = priorityCount.get(priority);
        currCount += change;

        assert currCount >= 0 : "Count is negative for " + priority.toString();

        priorityCount.put(priority, currCount);
    }

    public void addTip(TipType tipType, String filename) {
        updateTipsLock.lock();
        try {
            if (!tipHolder.addTip(tipType, filename)) {
                isCurrent = false;
            }
        } finally {
            updateTipsLock.unlock();
        }
    }

    public void fixTip(TipType tipType, String filename) {
        updateTipsLock.lock();
        try {
            if (!tipHolder.fixTip(tipType, filename)) {
                isCurrent = false;
            }
        } finally {
            updateTipsLock.unlock();
        }
    }

    private String getAllFilesAsHTMLString() {
        StringBuffer sb = new StringBuffer();

        if (!tipHolder.isEmpty()) {
            sb.append(tipHolder.toHTMLString());
        }

        for (Map.Entry<String, FileFeedbackHolder> file : files.entrySet()) {
            FileFeedbackHolder fileFeedbackHolder = file.getValue();
            sb.append(fileFeedbackHolder.toHTMLString());
        }

        return sb.toString();
    }

    private String getAllFilesAsHTMLString(String selectedFile) {
        StringBuffer sb = new StringBuffer();

        if (!tipHolder.isEmpty()) {
            if (selectedFile.equals("project-tips")) {
                sb.append(tipHolder.toSelectedHTMLString());
            } else {
                sb.append(tipHolder.toHTMLString());
            }
        }

        for (Map.Entry<String, FileFeedbackHolder> file : files.entrySet()) {
            String filename = file.getKey();
            FileFeedbackHolder fileFeedbackHolder = file.getValue();

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
                "        .material-icons {\n" +
                "            font-family: 'Material Icons';\n" +
                "            font-weight: normal;\n" +
                "            font-style: normal;\n" +
                "            font-size: 24px;\n" +
                "            display: inline-block;\n" +
                "            line-height: 1;\n" +
                "            text-transform: none;\n" +
                "            letter-spacing: normal;\n" +
                "            word-wrap: normal;\n" +
                "            white-space: nowrap;\n" +
                "            direction: ltr;\n" +
                "            /* Support for all WebKit browsers. */\n" +
                "            -webkit-font-smoothing: antialiased;\n" +
                "            /* Support for Safari and Chrome. */\n" +
                "            text-rendering: optimizeLegibility;\n" +
                "            /* Support for Firefox. */\n" +
                "            -moz-osx-font-smoothing: grayscale;\n" +
                "            /* Support for IE. */\n" +
                "            font-feature-settings: 'liga';\n" +
                "        }\n" +
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
                "            flex-grow: 1;\n" +
                "        }\n" +
                "        #page {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "        #filesidebar {\n" +
                "            width: 20%;\n" +
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
                "        div.feedbackcontainer {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "            align-items: center;\n" +
                "            margin-top: 2.5%;\n" +
                "            margin-left: 2.5%;\n" +
                "        }\n" +
                "        #feedback {\n" +
                "            width: 50%;\n" +
                "            border-radius: 5px;\n" +
                "            padding: 1%;\n" +
                "            font-family: Roboto, Helvetica, Arial, sans-serif;\n" +
                "        }\n" +
                "        button.ignorebutton {\n" +
                "            background-color: #34558b;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            border-radius: 5px;\n" +
                "            font-family: Roboto, Helvetica, Arial, sans-serif;\n" +
                "            padding: 10%;\n" +
                "            cursor: pointer;\n" +
                "        }\n" +
                "        #filetitle {\n" +
                "            display: flex;\n" +
                "            flex-grow: 1;\n" +
                "        }\n" +
                "        #filecontainer {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        #homeicon {\n" +
                "            color: white;\n" +
                "            padding-right: 24px;\n" +
                "            cursor: pointer;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body style=\"margin: 0px;height: 100%;\">\n" +
                "<div id=\"root\" style=\"height: 100%;\">\n" +
                "    <div id=\"topbar\">\n" +
                "        <h1 id=\"topbartitle\">\n" +
                "            Diligent\n" +
                "        </h1>\n" +
                "        <i class=\"material-icons\" id=\"homeicon\" onClick=\"location.href = './diligent.html'\">home</i>\n" +
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
                "<script>\n" +
                "    window.onload = init();\n" +
                "    \n" +
                "    function init() {\n" +
                "        var fileMap = JSON.parse(localStorage.getItem(\"$project\"));\n" +
                "        if (fileMap !== null) {\n" +
                "            var hidden = fileMap[\"$current_file\"];\n" +
                "            for (var i = 0; i < hidden.length; i++) {\n" +
                "                var x = document.getElementById(hidden[i]);\n" +
                "                if (x.style.display === \"none\") {\n" +
                "                    x.style.display = \"block\";\n" +
                "                } else {\n" +
                "                    x.style.display = \"none\";\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    function ignoreAdvice(div_id) {\n" +
                "        var fileMap = JSON.parse(localStorage.getItem(\"$project\"));\n" +
                "        if (fileMap === null) {\n" +
                "            var hidden = [div_id];\n" +
                "            var newFileMap = {\"$current_file\" : hidden};\n" +
                "        } else {\n" +
                "            var hidden = fileMap[\"$current_file\"];\n" +
                "            hidden.push(div_id);\n" +
                "            var newFileMap = {...fileMap, \"$current_file\" : hidden};\n" +
                "        }\n" +
                "        localStorage.setItem(\"$project\", JSON.stringify(newFileMap));\n" +
                "        window.location.reload();\n" +
                "    }\n" +
                "</script> \n" +
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
                "        .material-icons {\n" +
                "            font-family: 'Material Icons';\n" +
                "            font-weight: normal;\n" +
                "            font-style: normal;\n" +
                "            font-size: 24px;\n" +
                "            display: inline-block;\n" +
                "            line-height: 1;\n" +
                "            text-transform: none;\n" +
                "            letter-spacing: normal;\n" +
                "            word-wrap: normal;\n" +
                "            white-space: nowrap;\n" +
                "            direction: ltr;\n" +
                "            /* Support for all WebKit browsers. */\n" +
                "            -webkit-font-smoothing: antialiased;\n" +
                "            /* Support for Safari and Chrome. */\n" +
                "            text-rendering: optimizeLegibility;\n" +
                "            /* Support for Firefox. */\n" +
                "            -moz-osx-font-smoothing: grayscale;\n" +
                "            /* Support for IE. */\n" +
                "            font-feature-settings: 'liga';\n" +
                "        }\n" +
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
                "            flex-grow: 1;\n" +
                "        }\n" +
                "        #page {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "        #filesidebar {\n" +
                "            width: 20%;\n" +
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
                "        #filetitle {\n" +
                "            display: flex;\n" +
                "            flex-grow: 1;\n" +
                "        }\n" +
                "        #filecontainer {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        #homeicon {\n" +
                "            color: white;\n" +
                "            padding-right: 24px;\n" +
                "            cursor: pointer;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body style=\"margin: 0px;height: 100%;\">\n" +
                "<div id=\"root\" style=\"height: 100%;\">\n" +
                "    <div id=\"topbar\">\n" +
                "        <h1 id=\"topbartitle\">\n" +
                "            Diligent\n" +
                "        </h1>\n" +
                "        <i class=\"material-icons\" id=\"homeicon\" onClick=\"location.href = './diligent.html'\">home</i>\n" +
                "    </div>\n" +
                "    <div id=\"page\">\n" +
                "        <div id=\"filesidebar\">\n" +
                "            $files " +
                "        </div>\n" +
                "        <div id=\"mainpage\">\n" +
                "            <div style=\"width: 40%; margin-top: 2.5%; margin-left: 2.5%;padding: 1%;font-family: Roboto, Helvetica, Arial, sans-serif;\">\n" +
                "                <p style=\"font-weight: 500;\"> Please select a file from the sidebar. </p>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }
}
