package util;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

public class Notifier {

    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("diligent", NotificationDisplayType.BALLOON, true);

    public Notification notify(String title, String content) {
        return notify(null, title, content);
    }

    public Notification notify(Project project, String title, String content) {
        Notification notification = NOTIFICATION_GROUP.createNotification(title, content, NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER);
        notification.notify(project);
        return notification;
    }
}
