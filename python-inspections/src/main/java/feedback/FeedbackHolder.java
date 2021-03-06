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
        for (Map.Entry<Project, ProjectFeedbackHolder> project : projects.entrySet()) {
            ProjectFeedbackHolder projectFeedbackHolder = project.getValue();
            projectFeedbackHolder.updateReport();
        }
    }

    public void addFeedback(Project project, String filename, FeedbackIdentifier feedbackId, Feedback feedback) {
        ProjectFeedbackHolder projectFeedbackHolder = projects.get(project);

        if (projectFeedbackHolder == null) {
            projectFeedbackHolder = new ProjectFeedbackHolder(project);
            projects.put(project, projectFeedbackHolder);
        }

        projectFeedbackHolder.addFeedback(filename, feedbackId, feedback);
    }

    public void fixFeedback(Project project, String filename, FeedbackIdentifier feedbackId) {
        ProjectFeedbackHolder projectFeedbackHolder = projects.get(project);

        if (projectFeedbackHolder == null) {
            projectFeedbackHolder = new ProjectFeedbackHolder(project);
            projects.put(project, projectFeedbackHolder);
        }

        projectFeedbackHolder.fixFeedback(filename, feedbackId);
    }
}
