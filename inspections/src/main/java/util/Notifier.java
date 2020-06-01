package util;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;

import java.util.List;

public class Notifier {

    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("diligent", NotificationDisplayType.BALLOON, true);

    public Notification notify(Project project, String title, String content) {
        Notification notification = NOTIFICATION_GROUP.createNotification(title, content, NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER);
        notification.notify(project);
        return notification;
    }

    public Notification notifyError(String title, String content) {
        return notifyError(null, title, content);
    }

    public Notification notifyError(Project project, String title, String content) {
        Notification notification = NOTIFICATION_GROUP.createNotification(title, content, NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER);
        notification.notify(project);
        return notification;
    }

    public Notification notifyErrorWithAction(Project project, String title, String content, List<AnAction> actions) {
        Notification notification = NOTIFICATION_GROUP.createNotification(title, content, NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER);
        for (AnAction a : actions) {
            notification.addAction(a);
        }
        notification.notify(project);
        return notification;
    }
}
