package feedback;

import util.FeedbackType;
import util.ReportLevel;

public class FeedbackSignature {

    private final FeedbackType feedbackType;
    private final ReportLevel reportLevel;
    private final String location;
    private final Feedback feedback;

    public FeedbackSignature(FeedbackType feedbackType, ReportLevel reportLevel, String location, Feedback feedback) {
        this.feedbackType = feedbackType;
        this.reportLevel = reportLevel;
        this.location = location;
        this.feedback = feedback;
    }

    public FeedbackType getFeedbackType() {
        return feedbackType;
    }

    public ReportLevel getReportLevel() {
        return reportLevel;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof FeedbackSignature) {
            FeedbackSignature other = (FeedbackSignature) obj;

            return other.getReportLevel() == reportLevel &&
                    other.getFeedbackType() == feedbackType &&
                    location.equals(other.location);
        }

        return false;
    }
}
