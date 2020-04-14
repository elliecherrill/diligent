package feedback;

import com.intellij.openapi.project.Project;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeedbackHolder {

    private static final FeedbackHolder INSTANCE = new FeedbackHolder();
    private final Map<Project, ProjectFeedbackHolder> projects;

    private FeedbackHolder() {
        projects = new ConcurrentHashMap<>();
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

    public void addFeedback(Project project, String filename, FeedbackIdentifier feedbackId, Feedback feedback) {
        ProjectFeedbackHolder projectFeedbackHolder = projects.get(project);

        if (projectFeedbackHolder == null) {
            projectFeedbackHolder = new ProjectFeedbackHolder(project);
        }

        projectFeedbackHolder.addFeedback(filename, feedbackId, feedback);
        projects.put(project, projectFeedbackHolder);
    }

    public void fixFeedback(Project project, String filename, FeedbackIdentifier feedbackId) {
        ProjectFeedbackHolder projectFeedbackHolder = projects.get(project);
        if (projectFeedbackHolder == null) {
            return;
        }
        projectFeedbackHolder.fixFeedback(filename, feedbackId);
    }
}
