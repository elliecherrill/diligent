package feedback;

import com.intellij.openapi.project.Project;
import util.TipType;

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

    public void addTip(Project project, TipType tipType, String filename) {
        ProjectFeedbackHolder projectFeedbackHolder = projects.get(project);

        if (projectFeedbackHolder == null) {
            projectFeedbackHolder = new ProjectFeedbackHolder(project);
            projects.put(project, projectFeedbackHolder);
        }

        projectFeedbackHolder.addTip(tipType, filename);
    }

    public void fixTip(Project project, TipType tipType, String filename) {
        ProjectFeedbackHolder projectFeedbackHolder = projects.get(project);

        if (projectFeedbackHolder == null) {
            return;
        }

        projectFeedbackHolder.fixTip(tipType, filename);
    }
}
