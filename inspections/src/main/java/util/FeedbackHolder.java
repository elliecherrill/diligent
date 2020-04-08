package util;

import java.util.HashMap;
import java.util.Map;

public class FeedbackHolder {

    private static final FeedbackHolder INSTANCE = new FeedbackHolder();
    private final Map<String, ProjectFeedbackHolder> projects;

    private FeedbackHolder() {
        projects = new HashMap<>();
    }

    public static FeedbackHolder getInstance() {
        return INSTANCE;
    }

    public void writeToFile() {
        for (Map.Entry project : projects.entrySet()) {
            ProjectFeedbackHolder projectFeedbackHolder = (ProjectFeedbackHolder) project.getValue();
            projectFeedbackHolder.updateReport();
        }
    }

    public void addFeedback(String projectPath, String filename, String feedbackId, Feedback feedback) {
        ProjectFeedbackHolder projectFeedbackHolder = projects.get(projectPath);

        if (projectFeedbackHolder == null) {
            projectFeedbackHolder = new ProjectFeedbackHolder(projectPath);
        }

        projectFeedbackHolder.addFeedback(filename, feedbackId, feedback);
        projects.put(projectPath, projectFeedbackHolder);
    }

    public void fixFeedback(String projectPath, String filename, String feedbackId) {
        ProjectFeedbackHolder projectFeedbackHolder = projects.get(projectPath);
        if (projectFeedbackHolder == null) {
            return;
        }
        projectFeedbackHolder.fixFeedback(filename, feedbackId);
    }
}
